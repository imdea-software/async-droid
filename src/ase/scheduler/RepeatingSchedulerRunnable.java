package ase.scheduler;

import java.util.Iterator;

import android.content.Context;
import android.util.Log;
import ase.repeater.InputRepeater;

/* 
 * Schedules the application threads using a particular number of delays
 */
public class SchedulerRunnable implements Runnable {

    private PendingThreads threads = new PendingThreads();
    private ThreadData schedulerThreadData = new ThreadData(
            ThreadData.SCHEDULER_ID, null);
    private DelaySequence delaySeq;

    // Thread id of the currently scheduled thread
    private static long scheduled = 0L;
    private int segmentToProcess = 1;

    private Context appContext;
    Thread inputThread;
    private boolean schedulingLogs = true;

    public SchedulerRunnable(int numDelays, int numInputs, Context context) {
        appContext = context;
        delaySeq = new DelaySequence(numDelays, numInputs);
        delaySeq = new DelaySequence(0, 20);
    }

    public void run() {
        Log.i("MyScheduler", "Scheduler has started in thread: "
                + Thread.currentThread().getName() + " Id: "
                + Thread.currentThread().getId());

        // must wait until the main (UI) thread wakes it
        waitMyTurn(ThreadData.SCHEDULER_ID); // notify is called by UI in wake
                                             // scheduler

        boolean moreTests = true;

        while (moreTests) {
            Log.i("DelayInfo", "Current delay indices:" + delaySeq.toString());

            // run a single test with a sequence of delay indices
            initiateSingleTest();
            runSingleTest();

            // end of current test, get new delay indices
            Log.i("DelayInfo", "Updating delay indices for next test..");
            moreTests = delaySeq.getNextDelaySequence(); // ///// returns false
                                                         // when ended
        }

        Log.i("MyScheduler", "Test has completed.");
        Log.i("DelayInfo", "Test has completed.");
        return;
    }

    /*
     * Reset single test parameters
     */
    public void initiateSingleTest() {
        InputRepeater inputRepeater = new InputRepeater(appContext);
        inputThread = new Thread(inputRepeater);
        inputThread.setName("InputRepeater");
        inputThread.start();
        segmentToProcess = 1;
    }

    /*
     * A single test following one delay sequence
     */
    public void runSingleTest() {
        do {
            // if(threads.isEmpty())
            // continue;

            // add current user threads into list!! do not wait for them to
            // register!!
            Iterator<Thread> allThreads = Thread.getAllStackTraces().keySet()
                    .iterator();
            while (allThreads.hasNext()) {
                Thread t = allThreads.next();
                if (!t.getName().equalsIgnoreCase("SchedulerThread")
                        && isUserThread(t)
                        && !threads.capturedBefore(t.getId()))
                    // threads.addThread(new ThreadData(t.getId(), t));
                    sendThreadInfo(t); // / no need to send info from other
                                       // threads???
            }

            // Log.v("MyScheduler", threads.toString());
            threads.increaseWalker();
            ThreadData current = threads.getCurrentThread();

            // NOTE: additional guard to isWaiting added: hasMsgToHandle
            // isWaiting: if the current thread needs to be scheduled (executed
            // waitMyTurn() and will notify (was not in monitor))
            // hasMsgToHandle: if the current thread has a non-empty looper but
            // not yet executed waitMyTurn()
            // if hasMsgToHandle is true but isWaiting is false:
            // notifyNext sets the scheduled thread to that thread
            // in order the scheduler to continue, that thread should proceed
            // and notify the scheduler
            // hence, enforces scheduling that thread before others

            // NOTE: problem with hasMsgToHandle
            // if you allow anytime when UI has sth in it
            // it has sth internal and does not notify the scheduler, all
            // threads do wait!!
            if (current.isWaiting() /* || current.hasMsgToHandle() */) {
                // check whether it will be delayed
                if (segmentToProcess == delaySeq.getNextDelayIndex()) {
                    Log.i("MyScheduler",
                            "Delayed Thread Id: " + current.getId()
                                    + " Last Processed: " + segmentToProcess);
                    Log.i("DelayInfo", "Consumed delay: " + segmentToProcess);
                    threads.increaseWalker(); // delay
                    delaySeq.spendCurrentDelayIndex(); // ////
                }

                notifyNext();
                waitMyTurn(ThreadData.SCHEDULER_ID);
            }
        } while (!isEndOfCurrentTest());

    }

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
        // main thread is considered as user thread
    }

    /*
     * True if the current single test with a delay sequence has completed
     */
    public boolean isEndOfCurrentTest() {
        if (delaySeq.isEndOfCurrentDelaySequence() && segmentToProcess == 500) // to
                                                                               // be
                                                                               // updated
            return true;
        return false;
    }

    // //////////////////TODO here to determine the end of the test!!!!!
    /*
     * True if all tests for all possible delay sequences completed
     */
    public boolean isEndOfAllTests() {
        if (delaySeq.isEndOfCurrentDelaySequence() /*
                                                    * && segmentToProcess >
                                                    * numInputs
                                                    */)
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

            // ThreadData of waiting task should be in the list!!
            if (me == null) { // I should not hit this statement:
                Log.e("MyScheduler",
                        "THREAD TO BE SCHEDULED IS NOT IN THE LIST!!! "
                                + threadId);
                return;
            }

            // it can be suspended only if it is not in a monitor
            if (me.getCurrentMonitors() > 0) {
                // since waiting is not incremented, will not notify the
                // scheduler after completion
                Log.v("MyScheduler",
                        "Thread has acquired monitor(s), is not suspended.. Id:"
                                + me.getId());
                me.pushWaitBlock(false); // corresponding notifyScheduler will
                                         // not actually notify
                return;
            }

            // If thread is already in its block
            if (me.isWaiting()) {
                me.pushWaitBlock(false); // corresponding notifyScheduler will
                                         // not actually notify
            } else {
                me.pushWaitBlock(true); // corresponding notifyScheduler WILL
                                        // notify
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
        long id = (long) thisThread.getId();
        if (!threads.capturedBefore(id)) {
            threads.addThread(new ThreadData(id, thisThread));

            if (schedulingLogs)
                Log.v("MyScheduler", "I got " + thisThread.getName() + " Id: "
                        + thisThread.getId());
        }
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
                Log.v("MyScheduler", "I got " + thisThread.getName() + " Id: "
                        + thisThread.getId());
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
                    + threads.getWalkerIndex());
        current.notifyThread();
    }

    /*
     * Scheduler notifies all threads
     */
    /*
     * private void notifyAllThreads(){ while(!threads.isEmpty()){
     * threads.increaseWalker(); notifyNext(); } }
     */

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
            Log.i("MyScheduler", "Block is finished. Thread Id: "
                    + Thread.currentThread().getId() + " Last Processed: "
                    + segmentToProcess);

        // A thread did not actually wait in corresponding waitMyTurn
        // (either it was already in block (nested wait stmts) or it had
        // monitors)
        if (!me.popWaitBlock()) {
            Log.v("MyScheduler",
                    "I am NOTT notifying the scheduler. Thread Id: "
                            + Thread.currentThread().getId());
            return;
        }

        scheduled = ThreadData.SCHEDULER_ID;
        threads.removeThreadById(Thread.currentThread().getId());

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
     * To be called by UI thread in initiateScheduler Enables scheduler thread
     * to run
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

// scheduled and currentIndex are guaranteed to be not accessed by more than one
// threads concurrently
// either one of the application threads or the scheduler thread can access it