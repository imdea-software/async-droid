package ase.scheduler;

import ase.AseTestBridge;
import ase.repeater.InputRepeater;
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

    /*
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

}
