package ase.scheduler;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.util.Log;
import ase.AppRunTimeData;
import ase.ExecutionModeType;
import ase.event.AseEvent;
import ase.repeater.InputRepeater;
import ase.util.IOFactory;
import ase.util.Reader;
import ase.util.log.LogManager;
import ase.util.log.Logger;

/*
 * Schedules the application threads using a particular number of delays
 */
public class RepeatingMode implements ExecutionMode, Runnable {
    
    private PendingThreads threads = new PendingThreads();
    private ThreadData schedulerThreadData = new ThreadData(ThreadData.SCHEDULER_ID, null);
    private InputRepeater inputRepeater;
    private Scheduler scheduler;

    private int numTasksToDispatch = 0;
    
    // Thread id of the currently scheduled thread
    private static long scheduled = 0L;    
    
    private final boolean schedulingLogs = true;
    private Logger fileLog;
    
    public RepeatingMode(int numDelays, Context context) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
        fileLog = LogManager.getFileLog(String.format("TestLogs-%s.log", dateFormat.format(new Date())));
        
        // event list will be read once and be fed into each inputRepeater
        Reader reader = IOFactory.getReader(context);
        List<AseEvent> eventsToRepeat = reader.read();
        
        if(eventsToRepeat.size() == 0) {
            Log.e("Repeater", "No inputs to repeat");
            AppRunTimeData.getInstance().finishCurrentActivity();
            
        } else {
            inputRepeater = new InputRepeater(eventsToRepeat, fileLog);
            scheduler = new RRScheduler(threads, inputRepeater, fileLog);
            scheduler.initiateScheduler(numDelays, eventsToRepeat.size()); 
        }
    }
    
    @Override
    public void runScheduler() {
        Thread t = new Thread(this);
        t.setName("SchedulerThread");
        t.start();
        // prevent threads to wait for dispatch before the scheduler has them in its list
        threads.captureAllThreads();
        wakeScheduler();
    }

    @Override
    public void run() {
        Log.i("AseScheduler", "Scheduler has started in thread: "
                + Thread.currentThread().getName() + " Id: "
                + Thread.currentThread().getId());

        // must wait until the main (UI) thread wakes it
        waitForDispatch(ThreadData.SCHEDULER_ID);

        while (scheduler.hasMoreTestCases()) {
            AppRunTimeData.getInstance().launchMainActivity(); 
            setUpTestCase();
            runTestCase();
            tearDownTestCase();
            fileLog.i("End", "End of test");
        }

        Log.i("AseScheduler", "All tests has completed.");
        Log.i("DelayInfo", "All tests has completed.");

        // TODO now the app closes but we still need to rearrange this
        // create a new activity - clear top and and get the activity reference
        //AseTestBridge.launchMainActivity();

        // sleep until the new activity is created
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // finish the created activity
        //AseTestBridge.finishCurrentActivity();
    }

    /*
     * Set up single test parameters
     */
    public void setUpTestCase() {       
        scheduler.setUpTestCase();
        inputRepeater.reset();
        Thread inputThread = new Thread(inputRepeater);
        inputThread.setName("InputRepeater");
        inputThread.start();
        threads.captureThread(inputThread); // Register this before scheduler runs since it may wait earlier
    }
    
    /*
     * A single test executing a particular thread schedule
     */
    public void runTestCase() {
        ThreadData current = null;

        while (!scheduler.isEndOfTestCase()) {
            //threads.captureAllThreads();
            scheduler.doOnPreScheduling();
            current = scheduler.selectNextThread();

            if (current == null) {
                Log.e("AseScheduler", "No thread is selected.");
                continue; // check if end of test
            }
            
            logSchedulingDecision(current);
            Log.i("RRScheduler", "SCheduled: " + current.getName() );
            notifyThread(current);
            waitForDispatch(ThreadData.SCHEDULER_ID);
        }

        Log.i("AseScheduler", "Test has completed.");
        ThreadData main = threads.getThreadById(1);
        notifyThread(main);
    }
    
    private void logSchedulingDecision(ThreadData current) {
        fileLog.i("Scheduled", "--- " + current.getName());
    }
    
    /*
     * Clean test case data
     */
    public void tearDownTestCase() {
        scheduler.tearDownTestCase();
        scheduled = 0L;
        threads.clearThreads();
    }
    
    /*
     * Worker (or scheduler) thread waits for its signal to execute
     */
    public void waitForDispatch(long threadId) {
        ThreadData me;
        if (threadId != ThreadData.SCHEDULER_ID) {
            me = threads.getThreadById(threadId);

            // ThreadData of waiting task should be in the list!!
            if (me == null) { // should not hit this statement:
                Log.e("AseScheduler", "THREAD WHAT WAITS ITS TURN IS NOT IN THE LIST!!! " + threadId);
                return;
            }

            // it can be suspended only if it is not in a monitor
            if (me.getCurrentMonitors() > 0) {
                // will not be blocked by scheduler and will not notify the scheduler after completion
                Log.v("AseScheduler", "Thread has acquired monitor(s), is not suspended.. Id:" + me.getId());
                me.pushWaitBlock(false); // corresponding notifyScheduler will not actually notify
                return;
            }

            // If thread is already in its block
            if (me.isWaiting()) {
                me.pushWaitBlock(false); // corresponding notifyScheduler will not actually notify
            } else {
                me.pushWaitBlock(true); // corresponding notifyScheduler WILL notify
                me.setIsWaiting(true); // further blocks will not notify
            }

        } else {
            me = schedulerThreadData;
        }

        if (schedulingLogs)
            fileLog.i("RepeatingMode", "    --- Waiting - ThreadId: " + threadId);

        while (scheduled != threadId) {
            me.setTaskNum(numTasksToDispatch++); // to be used by the scheduler
            me.waitThread();
        }

        if (schedulingLogs)
            fileLog.i("RepeatingMode", "    --- Executing - ThreadId: " + threadId);
    }
    
    public void waitForDispatch() {
        Thread current =  Thread.currentThread();
        threads.captureThread(current); // add thread to the scheduling list in case it executes before capturing
        waitForDispatch(current.getId());
    }

    /*
     * Scheduler notifies the next task to be scheduled
     */
    private void notifyThread(ThreadData current) {
        scheduled = current.getId();
        //if (schedulingLogs)
            //logStream += "Scheduled thread id: " + scheduled + " Index: "
            //        + threads.getWalkerIndex() + " NumUIBlocks:" + AseTestBridge.getNumUIBlocks()) + "\n";

        current.notifyThread();
    }

    /*
     * Threads notify scheduler when they are completed This is also the case in
     * message/runnable processing in a looper In case no more messages arrive
     */
    public void notifyDispatcher() {

        ThreadData me = threads.getThreadById(Thread.currentThread().getId());

        // if already notified the scheduler, me is null
        // I should not hit this statement:
        if (me == null) {
            Log.e("AseScheduler",
                    "THREAD NOTIFYING SCHEDULER NOT IN THE LIST!!!");
            return;
        }

        //if (schedulingLogs)
        fileLog.i("RepeatingMode", "    --- Completed - Thread Id: " + Thread.currentThread().getId());

        // A thread did not actually wait in corresponding waitMyTurn
        // (either it was already in block (nested wait stmts) or it had monitors)
        if (!me.popWaitBlock()) {
            Log.v("AseScheduler", "NOTT notifying - Thread Id: "
                            + Thread.currentThread().getId());
            return;
        }

        scheduled = ThreadData.SCHEDULER_ID;

        // thread consumes the notification block
        me.setIsWaiting(false);
        if (schedulingLogs)
            fileLog.i("RepeatingMode", "    --- Notifying - Thread Id: " + Thread.currentThread().getId());
        
        schedulerThreadData.notifyThread();
    }

    /*
     * To be called by UI thread in initiateScheduler
     * Enables scheduler thread to run
     */
    public void wakeScheduler() {
        scheduled = ThreadData.SCHEDULER_ID;
        Log.i("AseScheduler", "Waky waky!");
        schedulerThreadData.notifyThread();
    }

    public void yield() {
        // to be implemented
        // use the stack of wait blocks of a thread 
    }

    public void enterMonitor() {
        ThreadData me = threads.getThreadById(Thread.currentThread().getId());
        me.enteredMonitor();
    }

    public void exitMonitor() {
        ThreadData me = threads.getThreadById(Thread.currentThread().getId());
        me.exitedMonitor();
    }

    @Override
    public ExecutionModeType getExecutionModeType() {
        return ExecutionModeType.REPEAT;
    }

}

// scheduled and currentIndex are guaranteed to be not accessed by more than one threads concurrently
// either one of the application threads or the scheduler thread can access it
