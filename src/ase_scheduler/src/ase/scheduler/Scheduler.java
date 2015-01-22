package ase.scheduler;

import java.util.List;

import android.os.Message;
import ase.repeater.InputRepeater;
import ase.scheduler.PendingThreads.ThreadType;
import ase.util.LooperReader;
import ase.util.log.Logger;

public abstract class Scheduler {

    protected PendingThreads threads;
    protected InputRepeater inputRepeater;    
    protected ThreadData scheduledThread;
    protected Logger logger;

    
    public Scheduler (PendingThreads threads, InputRepeater inputRepeater, Logger logger) {
        this.threads = threads;
        this.inputRepeater = inputRepeater;
        this.logger = logger;
    }
    
    public abstract void initiateScheduler(int bound, int numInputs);
    
    public abstract void setUpTestCase();
    
    public abstract void tearDownTestCase();

    public abstract boolean isEndOfTestCase();
    
    public abstract boolean hasMoreTestCases();
    
    public abstract ThreadData selectNextThread();

    /**
     * Check if the currently visited thread will be scheduled:
     *  - isWaiting:  (executed waitMyTurn() and will notify (was not in monitor))
     *  - is InputRepeater and has inputs to post
     *  - hasMsgToHandle: has a non-empty message queue
     * Keep the number of blocks for the main thread instead of hasMsgToHandle
     * (if you allow anytime when UI has sth in its message queue
     * it has sth internal and does not notify the scheduler, all threads do wait!!)
     */
    protected boolean okToSchedule(ThreadData current){
        if(current == null) return false;
        
        // if already went into waitMyTurn() and will notify (was not in monitor)
        if(current.isWaiting())
            return true;
        // if an event is sent to main thread (user input, publishProgress or postExecute)
        if(current.getId() == 1 && ((numInputsInMainLooper() > 0) || ( numAsyncTasksInMainLooper() > 0)))
            return true;
        // if the InputRepeater has input to post to main
       if(current.getName().equalsIgnoreCase("InputRepeater") && inputRepeater.hasMoreInputs()) 
            return true;
        // if a looper thread has a non-empty message queue (may not yet executed waitMyTurn())
        if(current.getId() != 1 && current.hasMsgToHandle())
            return true;

        // add stmt to AsyncTasks - if executor has tasks or thread is active
        return false;
    }

    public boolean hasAvailableThreads() {
        for(int i=0; i<threads.getSize(); i++) {
            if(okToSchedule(threads.getThreadByIndex(i, ThreadType.ANY)))
                    return true;
        }
        return false;
    }
    
    protected int numInputsInMainLooper() {
        List<Message> messages = LooperReader.getInstance().getMessages(threads.getThreadById(1).getThread());
        int count = 0;
        for(Message m: messages) {
            if(m.getTarget() != null && m.getTarget().getClass().getName().startsWith("ase.repeater.InputRepeater"))
                count ++;
        }
        return count;
    }

    protected int numAsyncTasksInMainLooper() {
        List<Message> messages = LooperReader.getInstance().getMessages(threads.getThreadById(1).getThread());
        int count = 0;
        // Main looper has an asynctask message - PostResult(onPostExecute or onCancalled) or PostProgress(onPublishProgress) 
        for(Message m: messages) {
            if(m.getTarget() != null && m.getTarget().getClass().getName().startsWith("android.os.AsyncTask") && (m.what == 1 || m.what == 2))
                count ++;
        }
        return count;
    }
}
