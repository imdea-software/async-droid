package myScheduler;

import android.util.Log;

/* 
 * Schedules the application threads using given delay indices
 */
public class SchedulerRunnable implements Runnable {

	private DelayServiceConHandler delayCon;

	private static PendingThreads threads = new PendingThreads();
	private static ThreadData schedulerThreadData = new ThreadData(ThreadData.SCHEDULER_ID);

	private DelayGenerator delayGen;
	private int numDelays;
	private int minNumSegments;
 
    private int segmentToProcess = 1;
    
	// Thread id of the currently scheduled thread
	private static long scheduled = (long) 0;

	public SchedulerRunnable(DelayServiceConHandler delayCon) {
		this.delayCon = delayCon;
	}

	public void run() {
		Log.i("MyScheduler", "Scheduler has started in thread: "
				+ Thread.currentThread().getName() + " Id: "
				+ Thread.currentThread().getId());

		initiateTestParameters();
        
        // must wait until the main (UI) thread wakes it
        waitMyTurn(ThreadData.SCHEDULER_ID);
        
		boolean moreTests = true;
		
		while(moreTests){
		    Log.i("DelayInfo", "Current delay indices:" + delayGen.delayIndicesToString());
		
		    // run a single test with a sequence of delay indices
		    initiateTestUnit();		
		    runSingleTest();
		
		    // end of current test, get new delay indices 
		    Log.i("DelayInfo", "Updating delay indices for next test..");
		    moreTests = delayGen.updateDelayIndices(); /////// returns false when ended !!!!
		    
		    delayCon.doSendIPCMsg(DelayServiceConHandler.MSG_REPEAT_TEST);
		}
		
		delayCon.doUnbindService();
		delayCon.doStopService();
		
		Log.i("MyScheduler", "Test has completed.");
		Log.i("DelayInfo", "Test has completed.");
		return;
	}

	/*
	 * A single test following one delay sequence
	 */
	public void runSingleTest(){
	    while (!isEndOfCurrentTest()) {
            if(threads.isEmpty())
                continue;
            
            threads.increaseWalker();

            ThreadData current = threads.getCurrentThread();
            
            // if the current thread needs to be scheduled (is waiting and will notify)
            if (current.willNotifyEver()) {
                
                // check whether it will be delayed
                if (segmentToProcess == delayGen.getNextSegmentIndexToDelay()) {
                    Log.i("MyScheduler", "Delayed Thread Id: "
                            + current.getId() + " Last Processed: " + segmentToProcess);                  
                    Log.i("DelayInfo", "Consumed delay: " + segmentToProcess);
                    threads.increaseWalker(); // delay
                    delayGen.setNextDelayPoint(); //////
                }
                
                notifyNext(); 
                waitMyTurn(ThreadData.SCHEDULER_ID);        
            }
        }
	    
	}
	
	/*
	 * Get parameters via DelayServiceConnectionHandler
	 */
	public void initiateTestParameters(){
        delayCon.doStartService();
        delayCon.doBindService();       
        while(!delayCon.doSendIPCMsg(DelayServiceConHandler.MSG_START_TESTING));        
        while(!delayCon.isParametersSet()); // service messages are handled in UI thread

        minNumSegments = delayCon.getNumSegments();
        numDelays = delayCon.getNumDelays();
        
        delayGen = new DelayGenerator(minNumSegments, numDelays);
        Log.i("MyScheduler", "Test parameters: numDelays = " +  numDelays + "  numSegments= " + minNumSegments);
    }
    
	/*
	 * Reset single test parameters
	 */
    public void initiateTestUnit(){
        segmentToProcess = 1;
    }
    
    /*
     * True if the current single test with a delay sequence has completed
     */
    public boolean isEndOfCurrentTest() {
        if (delayGen.isEndOfCurrentDelaySequence() &&  segmentToProcess > minNumSegments)
           return true;
        return false;
    }

    /*
     * True if all tests for all possible delay sequences completed
     */
    public boolean isEndOfAllTests() {
        if (delayGen.isEndOfCurrentDelaySequence() &&  segmentToProcess > minNumSegments)
           return true;
        return false;
    }
	
	/*
	 *  Worker (or scheduler) thread waits for its signal to execute
	 */
	public void waitMyTurn(long threadId) {

		ThreadData me;
		if (threadId != ThreadData.SCHEDULER_ID) {
			me = threads.getThreadById(threadId);

			// ThreadData of waiting task should be in the list!!
			if(me == null){ // I should not hit this statement:
				Log.e("MyScheduler", "THREAD TO BE SCHEDULED IS NOT IN THE LIST!!!");
				return;
			}
				
			// it can be suspended only if it is not in a monitor
			if (me.getCurrentMonitors() > 0) {
				// since waiting is not incremented, will not notify the scheduler after completion
				Log.i("MyScheduler", "Thread has acquired monitor(s), is not suspended.. Id:" + me.getId());
				me.pushWaitBlock(false); // corresponding notifyScheduler will not actually notify
				return;
			}
			
			// If thread is already in its block
			if(me.willNotifyEver()){
				me.pushWaitBlock(false); // corresponding notifyScheduler will not actually notify
			}else{
				me.pushWaitBlock(true); // corresponding notifyScheduler WILL notify
				me.setWillNotifyEver(true); // further blocks will not notify
			}
			
		} else {
			me = schedulerThreadData;
		}
	
		Log.i("MyScheduler", "I am waiting. ThreadId: " + threadId);
		
		while (scheduled != threadId) {
			me.waitThread();
		}

		Log.i("MyScheduler", "I am executing. ThreadId: " + threadId);
	}

	/*
	 *  Send current thread info to the scheduler
	 */
	public void sendThreadInfo() {
		long id = (long) Thread.currentThread().getId();
		if (!threads.capturedBefore(id)) {
			threads.addThread(new ThreadData(id));
			Log.i("MyScheduler", "I got " + Thread.currentThread().getName()
					+ " Id: " + Thread.currentThread().getId());
		}
	}

	/*
	 *  Scheduler notifies the next task to be scheduled
	 */
	private void notifyNext() {
		ThreadData current = threads.getCurrentThread();
		scheduled = current.getId();
		Log.i("MyScheduler", "Scheduled thread id: " + scheduled);
		current.notifyThread();
	}

	/*
     *  Scheduler notifies all threads when the test is completed
     */
	/*private void notifyAllThreads(){
	       while(!threads.isEmpty()){
	            threads.increaseWalker();
	            notifyNext(); 
	        }
	}*/
	
	/* Threads notify scheduler when they are completed
	 * This is also the case in message/runnable processing in a looper
	 * In case no more messages arrive
	 */
	public void notifyScheduler() {

		ThreadData me = threads.getThreadById(Thread.currentThread().getId());
		
		// if already notified the scheduler, me is null
		// I should not hit this statement:
		if(me == null){
			Log.e("MyScheduler", "THREAD NOTIFYING SCHEDULER NOT IN THE LIST!!!");
			return;
		}
			
		
		Log.i("MyScheduler", "Block is finished. Thread Id: "
                + Thread.currentThread().getId() + " Last Processed: " + segmentToProcess);
		
		// A thread did not actually wait in corresponding waitMyTurn
		// (either it was already in block (nested wait stmts) or it had monitors)
		if(!me.popWaitBlock()){
			Log.i("MyScheduler", "I am NOTT notifying the scheduler. Thread Id: "
					+ Thread.currentThread().getId());
			return; 
		}
			
		scheduled = ThreadData.SCHEDULER_ID;		
		threads.removeThreadById(Thread.currentThread().getId());
		
//		synchronized(this){
			segmentToProcess ++;  // data race not critical here ?
//		}
		
		// thread consumes the notification block
		me.setWillNotifyEver(false); 
		Log.i("MyScheduler", "I am notifying the scheduler. Thread Id: "
				+ Thread.currentThread().getId());
		schedulerThreadData.notifyThread();
	}
	
	public void wakeScheduler() {
		scheduled = ThreadData.SCHEDULER_ID;
		schedulerThreadData.notifyThread();
	}

	public void yield(long threadId) {
		notifyScheduler();
		waitMyTurn(threadId);
	}

	public void enterMonitor(){
		ThreadData me = threads.getThreadById(Thread.currentThread().getId());
		me.enteredMonitor();
	}
	
	public void exitMonitor(){
		ThreadData me = threads.getThreadById(Thread.currentThread().getId());
		me.exitedMonitor();
	}
	
}


// scheduled and currentIndex are guaranteed to be not accessed by more than one
// threads concurrently
// either one of the application threads or the scheduler thread can access it