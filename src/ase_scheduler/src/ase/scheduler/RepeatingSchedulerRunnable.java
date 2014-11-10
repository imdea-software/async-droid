package ase.scheduler;

import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.os.Process;
import android.util.Log;
import ase.AseEvent;
import ase.AseTestBridge;
import ase.repeater.InputRepeater;
import ase.util.IOFactory;
import ase.util.Reader;

/* 
 * Schedules the application threads using a particular number of delays
 */
public class RepeatingSchedulerRunnable implements Runnable {

    private PendingThreads threads = new PendingThreads();
    private ThreadData schedulerThreadData = new ThreadData(ThreadData.SCHEDULER_ID, null);
    private DelaySequence delaySeq;
    private InputRepeater inputRepeater;
    
    
    // Thread id of the currently scheduled thread
    private static long scheduled = 0L;
    private int segmentToProcess = 1;

    private Context appContext;
    
    private boolean schedulingLogs = true;

    public RepeatingSchedulerRunnable(int numDelays, Context context) {
        appContext = context;
        // event list will be read once and be fed into each inputRepeater
        Reader reader = IOFactory.getReader(context);
        List<AseEvent> eventsToRepeat = reader.read();
        inputRepeater = new InputRepeater(appContext, eventsToRepeat);        
        // use numInputs to generate the delay sequences
        delaySeq = new DelaySequence(numDelays, eventsToRepeat.size());
    }

    public void run() {
        Log.i("MyScheduler", "Scheduler has started in thread: "
                + Thread.currentThread().getName() + " Id: "
                + Thread.currentThread().getId());

        // must wait until the main (UI) thread wakes it
        waitMyTurn(ThreadData.SCHEDULER_ID); 

        boolean moreTests = true;

        while (moreTests) {
//            AseTestBridge.launchMainActivity();
            Log.i("DelayInfo", "Current delay indices:" + delaySeq.toString());

            // run a single test with a sequence of delay indices
            initiateSingleTest();
            runSingleTest();

            Log.i("MyScheduler", "Test has completed.");
            // end of current test, get new delay indices
            Log.i("DelayInfo", "Updating delay indices for next test..");
            moreTests = delaySeq.getNextDelaySequence(); // ///// returns false when ended
            AseTestBridge.launchMainActivity();
        }

        Log.i("MyScheduler", "All tests has completed.");
        Log.i("DelayInfo", "All tests has completed.");
    }

    /*
     * Reset single test parameters
     */
    public void initiateSingleTest() {
        inputRepeater.reset();
        Thread inputThread = new Thread(inputRepeater);
        inputThread.setName("InputRepeater");
        inputThread.start();        
        segmentToProcess = 1; 
        threads.clear();  // If comes after InputRepeater is registered, problematic!!!
        sendThreadInfo(inputThread); // Register this before scheduler runs since it may wait earlier
        // (initiation of myScheduler by UIthread, that waits when click)
        // (InputRepeater immediately wants to be scheduled, it needs to be in the list)
    }

    /*
     * A single test following one delay sequence
     */
    public void runSingleTest() {
        int idleSteps = 0;
        do {
            // add current user threads into list!! do not wait for them to register!!
            getAllUserThreads();
            // walker keeps the index of the thread to be scheduled
            threads.increaseWalker();
            
            ThreadData current = threads.getCurrentThread();
            Log.v("Scheduled", threads.toString());
            Log.v("Scheduled", "Current: " + current.getName() + " Walker Index: " + threads.getWalkerIndex());
            
            if(okToSchedule(current)){
                // check whether the thread will be delayed
                if (segmentToProcess == delaySeq.getNextDelayIndex()) {
                    Log.i("MyScheduler", "Delayed Thread Id: " + current.getId() + " Last Processed: " + segmentToProcess);
                    Log.i("DelayInfo", "Consumed delay: " + segmentToProcess);
                    
                    threads.increaseWalker(); // delay (what if this is not ok to schedule??????) //// DETECTED BUG!!!!
                    current = threads.getCurrentThread();
                    while(!okToSchedule(current)) {  /// not infinite loop, at least one of them is ok (the delayed one)
                        threads.increaseWalker();   /// go for a ready-to-run thread 
                        current = threads.getCurrentThread(); 
                    }
                    
                    delaySeq.spendCurrentDelayIndex(); 
                }

                idleSteps = 0;
                notifyNext();
                waitMyTurn(ThreadData.SCHEDULER_ID);
            }else{
                idleSteps++;  // scheduled thread has no message to execute
            }
        } while (!(delaySeq.isEndOfCurrentDelaySequence() && idleSteps == threads.getSize()));
        /* end of current test if:
         *  - current delay sequence has completed (all delays are executed) and
         *  - all threads are idle in a loop (no threads are okToSchedule) ensures:
         *      - main has executed posted blocks, inputRepeater has no left inputs to repeat
         *      - looper threads are idle and no one is waiting to be scheduled
         */
           
    }
    
    /*
     * Collect application's user threads
     */
    private void getAllUserThreads(){
        Iterator<Thread> allThreads = Thread.getAllStackTraces().keySet().iterator();
        while (allThreads.hasNext()) {
            Thread t = allThreads.next();
            if (!t.getName().equalsIgnoreCase("SchedulerThread")
                    && isUserThread(t)
                    && !threads.capturedBefore(t.getId()))
                sendThreadInfo(t); // no need to send info from other threads?
        }
    }
    
    /*
     * false for default application threads (except for the main thread)
     */
    private static boolean isUserThread(Thread t) {
        String name = t.getName();
        if (name.equalsIgnoreCase("GC")
                || name.equalsIgnoreCase("Signal Catcher")
                || name.equalsIgnoreCase("JDWP")
                || name.equalsIgnoreCase("Compiler")
                || name.equalsIgnoreCase("ReferenceQueueDaemon")
                || name.equalsIgnoreCase("FinalizerDaemon")
                || name.equalsIgnoreCase("FinalizerWatchdogDaemon")
                || name.startsWith("Binder"))
            return false;
        return true;
    }
    
    /*
     * Check if the currently visited thread will be scheduled: 
     *  - isWaiting:  (executed waitMyTurn() and will notify (was not in monitor))
     *  - is InputRepeater and has inputs to post
     *  - hasMsgToHandle: has a non-empty message queue
     * Keep the number of blocks for the main thread instead of hasMsgToHandle
     * (if you allow anytime when UI has sth in its message queue
     * it has sth internal and does not notify the scheduler, all threads do wait!!)
     */
    private boolean okToSchedule(ThreadData current){
        // if already went into waitMyTurn() and will notify (was not in monitor)
        if(current.isWaiting())
            return true;
        // if an event is sent to main thread (user input, publishProgress or postExecute)
        if(current.getId() == 1 && AseTestBridge.getNumUIBlocks() >=1 ) 
            return true;
        // if the InputRepeater has input to post to main
        if(current.getName().equalsIgnoreCase("InputRepeater") && inputRepeater.hasMoreInputs()) 
            return true;
        // if a looper thread has a non-empty message queue (may not yet executed waitMyTurn())
        if(current.getId() != 1 && current.hasMsgToHandle())
            return true;
        
        return false;
    }

    /*
     * Worker (or scheduler) thread waits for its signal to execute
     */
    public void waitMyTurn(long threadId) {

        ThreadData me;
        if (threadId != ThreadData.SCHEDULER_ID) {
            me = threads.getThreadById(threadId);

            //TODO: Is it still required? Should I notify when I waited when I was not in the list?
            // ThreadData of waiting task should be in the list!!
            if (me == null) { // I should not hit this statement:
                Log.e("MyScheduler", "THREAD WHAT WAITS ITS TURN IS NOT IN THE LIST!!! " + threadId);
                return;
            }

            // it can be suspended only if it is not in a monitor
            if (me.getCurrentMonitors() > 0) {
                // will not be blocked by scheduler and will not notify the scheduler after completion
                Log.v("MyScheduler", "Thread has acquired monitor(s), is not suspended.. Id:" + me.getId());
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
            Log.v("MyScheduler", "I am waiting. ThreadId: " + threadId);

        while (scheduled != threadId) {
            me.waitThread();
        }

        if (schedulingLogs)
            Log.v("MyScheduler", "I am executing. ThreadId: " + threadId);
    }

    /*
     * Send current thread info to the scheduler This method is called by a
     * thread itself to be registered
     */
    public void sendThreadInfo() {
        Thread thisThread = Thread.currentThread();
        sendThreadInfo(thisThread);
    }

    /*
     * Send current thread info to the scheduler This method is called by the
     * scheduler to register existing threads
     */
    public void sendThreadInfo(Thread thisThread) {
        long id = (long) thisThread.getId();
        if (!threads.capturedBefore(id)) {
            threads.addThread(new ThreadData(id, thisThread));

            if (schedulingLogs)
                Log.v("MyScheduler", "I got " + thisThread.getName() + " Id: "  + thisThread.getId());
        }
    }

    /*
     * Scheduler notifies the next task to be scheduled
     */
    private void notifyNext() {
        ThreadData current = threads.getCurrentThread();
        scheduled = current.getId();
        if (schedulingLogs)
            Log.i("Scheduled", "Scheduled thread id: " + scheduled + " Index: "
                    + threads.getWalkerIndex() + " NumUIBlocks:" + AseTestBridge.getNumUIBlocks());
        
        if(scheduled == 1)
            AseTestBridge.decNumUIBlocks();
        current.notifyThread();
    }

    /*
     * Threads notify scheduler when they are completed This is also the case in
     * message/runnable processing in a looper In case no more messages arrive
     */
    public void notifyScheduler() {

        ThreadData me = threads.getThreadById(Thread.currentThread().getId());

        // if already notified the scheduler, me is null
        // I should not hit this statement:
        if (me == null) {
            Log.e("MyScheduler",
                    "THREAD NOTIFYING SCHEDULER NOT IN THE LIST!!!");
            return;
        }

        if (schedulingLogs)
            Log.v("MyScheduler", "Block is finished. Thread Id: "
                    + Thread.currentThread().getId() + " Last Processed: "
                    + segmentToProcess);

        // A thread did not actually wait in corresponding waitMyTurn
        // (either it was already in block (nested wait stmts) or it had monitors)
        if (!me.popWaitBlock()) {
            Log.v("MyScheduler", "I am NOTT notifying the scheduler. Thread Id: "
                            + Thread.currentThread().getId());
            return;
        }

        scheduled = ThreadData.SCHEDULER_ID;
        
        // synchronized(this){
        segmentToProcess++; // data race not critical here ?
        // }

        // thread consumes the notification block
        me.setIsWaiting(false);
        if (schedulingLogs)
            Log.v("MyScheduler", "I am notifying the scheduler. Thread Id: "
                    + Thread.currentThread().getId());
        schedulerThreadData.notifyThread();
    }

    /*
     * To be called by UI thread in initiateScheduler 
     * Enables scheduler thread to run
     */
    public void wakeScheduler() {
        scheduled = ThreadData.SCHEDULER_ID;
        Log.i("MyScheduler", "Waky waky!");
        schedulerThreadData.notifyThread();
    }

    public void yield(long threadId) {
        notifyScheduler();
        waitMyTurn(threadId);
    }

    public void enterMonitor() {
        ThreadData me = threads.getThreadById(Thread.currentThread().getId());
        me.enteredMonitor();
    }

    public void exitMonitor() {
        ThreadData me = threads.getThreadById(Thread.currentThread().getId());
        me.exitedMonitor();
    }

}

// scheduled and currentIndex are guaranteed to be not accessed by more than one threads concurrently
// either one of the application threads or the scheduler thread can access it