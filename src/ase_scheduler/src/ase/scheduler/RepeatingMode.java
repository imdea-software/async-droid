package ase.scheduler;

import java.util.List;

import android.app.Application;
import android.util.Log;
import ase.AppRunTimeData;
import ase.ExecutionModeType;
import ase.event.AseEvent;
import ase.repeater.InputRepeater;
import ase.util.FileUtils;
import ase.util.IOFactory;
import ase.util.LooperReader;
import ase.util.Reader;
import ase.util.Logger;
import ase.util.ReflectionUtils;

/*
 * Schedules the application threads using a particular number of delays
 */
public class RepeatingMode implements ExecutionMode, Runnable {

    private PendingThreads threads = new PendingThreads();
    private ThreadData schedulerThreadData = new ThreadData(ThreadData.SCHEDULER_ID, null);
    private InputRepeater inputRepeater;
    private Scheduler scheduler;

    private final boolean schedulingLogs = false;

    // Thread id of the currently scheduled thread
    private static long scheduled = 0L;
    // max number of idle thread schedules
    private static int MAX_TRIALS = 20; 

    public RepeatingMode(int numDelays) {
        // event list will be read once and be fed into each inputRepeater
        Reader reader = IOFactory.getReader();
        List<AseEvent> eventsToRepeat = reader.read();

        if (eventsToRepeat.size() == 0) {
            Log.e("Repeater", "No inputs to repeat");
            AppRunTimeData.getInstance().finishCurrentActivity();

        } else {
            inputRepeater = new InputRepeater(eventsToRepeat);
            scheduler = new RRScheduler(threads, inputRepeater);
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
        Log.i("AseScheduler", "Scheduler has started in thread: " + Thread.currentThread().getName() + " Id: "
                + Thread.currentThread().getId());

        // must wait until the main (UI) thread wakes it
        waitForDispatch(ThreadData.SCHEDULER_ID);

        while (scheduler.hasMoreTestCases()) {
            AppRunTimeData.getInstance().launchMainActivity();
            setUpTestCase();
            runTestCase();
            tearDownTestCase();
            Logger.i("End", "End of test");
        }

        Logger.i("AseScheduler", "All tests has completed.");
        Logger.i("DelayInfo", "All tests has completed.");

        // TODO now the app closes but we still need to rearrange this
        // create a new activity - clear top and and get the activity reference
        // AseTestBridge.launchMainActivity();

        // sleep until the new activity is created
        /*try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/

        // finish the created activity
        AppRunTimeData.getInstance().finishCurrentActivity();
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

        // scheduling decisions at each wait/notify point
        int numSchedulingDecisions = 0;
        int trials = 0;

        while (!scheduler.isEndOfTestCase() && trials < MAX_TRIALS) {
            // threads.captureAllThreads();
            scheduler.doOnPreScheduling();
            current = scheduler.selectNextThread();

            if (current == null) {
                trials++;
                Log.e("AseScheduler", "No thread is selected.");
                continue; // check if end of test
            }

            numSchedulingDecisions++;
            notifyThread(current);
            waitForDispatch(ThreadData.SCHEDULER_ID);

            scheduler.doOnPostScheduling();
        }

        FileUtils.appendLine(IOFactory.STATS_FILE, "{ \"numSchedulingDecisions\":" + numSchedulingDecisions + " }");
        
        Logger.i("Scheduler", "Test has completed.");
        scheduler.logThreads(current); // //////

    }

    /*
     * Clean test case data
     */
    public void tearDownTestCase() {
        final Application app = AppRunTimeData.getInstance().getCurrentAct().getApplication();
        
        // allow the main thread to run (in case it is in blocking runnables)
        /* synchronized(this) {
            scheduled = 1L;
        }*/
        ThreadData main = threads.getThreadById(1);
        notifyThread(main);
        
        // Call the programmer implemented test case finalizer in the app
        // Run it in the main thread in case it has view components
        AppRunTimeData.getInstance().getCurrentAct().runOnUiThread( new Runnable() {
            @Override
            public void run() {
                ReflectionUtils.callMethod((Object)app, "finalizeTestCase");               
            }            
        });
 
        while(!LooperReader.getInstance().hasEmptyLooper(main.getThread())) {
         // when a blocking UI thread message completes, it notifies back the scheduler and sets scheduled = -1
            notifyThread(main); 
            try {
                Thread.sleep(500);
            } catch (Exception e){
                
            }
            Log.i("RepeatMode", "Waiting to empty the main thread Scheduled: " + scheduled);
            Log.i("Contents", LooperReader.getInstance().dumpQueue(main.getThread()));
        }
            
        // tear down test case, that also calls programmer-implemented tear down
        // (e.g. restores app state for a new test, removes callbacks, etc)
        scheduler.tearDownTestCase();
        synchronized(this) {
            scheduled = 0L;
        }
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
                me.pushWaitBlock(false); // corresponding notifyScheduler will // not actually notify
                return;
            }

            // If thread is already in its block
            if (me.isWaiting()) {
                me.pushWaitBlock(false); // corresponding notifyScheduler will // not actually notify
            } else {
                me.pushWaitBlock(true); // corresponding notifyScheduler WILL // notify
                me.setIsWaiting(true); // further blocks will not notify
            }

        } else {
            me = schedulerThreadData;
        }

        if (schedulingLogs)
            Logger.i("RepeatingMode", "    --- Waiting - ThreadId: " + threadId);

        synchronized(me) {  //////////////
        while (scheduled != threadId) {
            // me.waitThread();
            try {
                me.wait();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        } /////////////////
        
        if (schedulingLogs)
            Logger.i("RepeatingMode", "    --- Executing - ThreadId: " + threadId);
    }

    public void waitForDispatch() {
        Thread current = Thread.currentThread();
        threads.captureThread(current); // add thread to the scheduling list in case it executes before capturing
        waitForDispatch(current.getId());
    }

    /*
     * Scheduler notifies the next task to be scheduled
     */
    private void notifyThread(ThreadData current) {
        synchronized(current) {
            scheduled = current.getId();
            //current.notifyThread(); /////
            current.notify();
        }
        // if (schedulingLogs)
        // logStream += "Scheduled thread id: " + scheduled + " Index: " + threads.getWalkerIndex() + " NumUIBlocks:" + AseTestBridge.getNumUIBlocks()) + "\n";   
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
            Log.e("AseScheduler", "THREAD NOTIFYING SCHEDULER NOT IN THE LIST!!!");
            return;
        }

        if (schedulingLogs)
            Logger.i("RepeatingMode", "    --- Completed - Thread Id: " + Thread.currentThread().getId());

        // A thread did not actually wait in corresponding waitMyTurn
        // (either it was already in block (nested wait stmts) or it had monitors)
        if (!me.popWaitBlock()) {
            Log.v("AseScheduler", "NOTT notifying - Thread Id: " + Thread.currentThread().getId());
            return;
        }

        //scheduled = ThreadData.SCHEDULER_ID;  //////

        // thread consumes the notification block
        me.setIsWaiting(false);
        if (schedulingLogs)
            Logger.i("RepeatingMode", "    --- Notifying - Thread Id: " + Thread.currentThread().getId());

        //schedulerThreadData.notifyThread(); /////
        notifyThread(schedulerThreadData);
    }

    /*
     * To be called by UI thread in initiateScheduler Enables scheduler thread to run
     */
    public void wakeScheduler() {
        //scheduled = ThreadData.SCHEDULER_ID; /////
        Log.i("AseScheduler", "Waky waky!");
        //schedulerThreadData.notifyThread(); /////
        notifyThread(schedulerThreadData);
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

    /*public static synchronized long getScheduled() {
        return scheduled;
    }*/
}

