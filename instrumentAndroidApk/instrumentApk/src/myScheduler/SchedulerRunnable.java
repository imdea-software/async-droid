package myScheduler;

import android.util.Log;

//schedules the app threads using given delay indices
public class SchedulerRunnable implements Runnable {

	private int numDelays;
	private int[] numIndices;
	private int currentIndexToDelay = 0;
	private int numProcessed = 0;
	private int MIN_NUM_PROCESSED = 0;

	private static PendingThreads threads = new PendingThreads();
	private static ThreadData schedulerThreadData = new ThreadData(-1);

	// id of the currently scheduled thread 
	// -1 for scheduler, initially UI thread is enabled too
	private static long scheduled = (long) 0;

	public SchedulerRunnable(int numDelays, int[] numIndices, int limit) {
		this.numDelays = numDelays;
		this.numIndices = numIndices;
		MIN_NUM_PROCESSED = limit;
	}

	public int getNextDelayPoint() {
		if (currentIndexToDelay < numDelays)
			return numIndices[currentIndexToDelay];
		else
			return -1;
	}

	public void setNextDelayPoint() {
		currentIndexToDelay++;
	}

	public boolean isEndOfTest() {
		if (currentIndexToDelay >= numDelays && numProcessed == MIN_NUM_PROCESSED)
			return true;
		return false;
	}

	public void run() {
		Log.i("MyScheduler", "Scheduler has started in thread: "
				+ Thread.currentThread().getName() + " Id: "
				+ Thread.currentThread().getId());

		// must wait until the main (UI) thread is added to the list
		// gainControl();
		waitMyTurn(-1);

		while (!threads.isEmpty() /*&& !isEndOfTest()*/) {
			threads.increaseWalker();

			if (numProcessed == getNextDelayPoint()) {
				Log.i("MyScheduler", "Delayed: "
						+ Thread.currentThread().getName() + " Id: "
						+ Thread.currentThread().getId() + " NumProcessed: " + numProcessed);
				threads.increaseWalker(); // delay
				setNextDelayPoint();
			}

			ThreadData current = threads.getCurrentThread();

			if (current.willBeScheduled()) {
				// give ThreadData as parameter not to repeat getting this data
				notifyNext(current); 
				// gainControl();
				waitMyTurn(-1);
			}
		}

		Log.i("MyScheduler", "Test has completed with delays: ");
		///// must end the test !!!!
		return;
	}

	// threaddata of waiting task should be in the list!!
	// worker (or scheduler) thread waits for its signal to execute
	public void waitMyTurn(long threadId) {

		ThreadData me;
		if (threadId != -1) {
			me = threads.getThreadById(threadId);

			if(me == null)
				Log.e("MyScheduler",
						"THREAD TO BE SCHEDULED IS NOT IN THE LIST!!!");
				
			// it can be suspended only if it is not in a monitor
			if (me.getCurrentMonitors() > 0) {
				// do not notify the scheduler after completion
				// no synchronization for that block
				me.setToBeScheduled(false);
				Log.i("MyScheduler",
						"Thread has acquired monitor(s), is not suspended.. Id:"
								+ me.getId());
				return;
			}
			
		} else {
			me = schedulerThreadData;
		}

		Log.i("MyScheduler", "I am waiting: " + threadId);

		while (scheduled != threadId) {
			me.waitThread();
		}

		Log.i("MyScheduler", "I am executing " + threadId);
	}

	// Send current thread info to the scheduler
	// Initial call bu UI thread
	public void sendThreadInfo() {
		long id = (long) Thread.currentThread().getId();
		if (!threads.capturedBefore(id)) {
			threads.addThread(new ThreadData(id));
			Log.i("MyScheduler", "I got " + Thread.currentThread().getName()
					+ " Id: " + Thread.currentThread().getId());
		}

	}

	// scheduler notifies the next task to be scheduled
	private void notifyNext(ThreadData current) {
		// scheduler notifies a next thread:
		scheduled = current.getId();
		Log.i("MyScheduler", "Scheduled thread id: " + scheduled);
		current.notifyThread();
	}

	// Threads notify scheduler when they are completed
	// This is also the case in message/runnable processing in a looper
	// In case no more messages arrive
	public void notifyScheduler() {

		ThreadData me = threads.getThreadById(Thread.currentThread().getId());
		
		// if already notified the scheduler, return
		if(me.didNotifyScheduler())
			return;
		
		// it can be resumed only if it is suspended before
		// (it may not have been if it had monitors)
		if (!me.willBeScheduled()) { 
			// in the next segment, it will be scheduled by default
			me.setToBeScheduled(true); 
			return;
		}

		Log.i("MyScheduler", "Thread has completed. Id: "
				+ Thread.currentThread().getId() + " NumProcessed: " + numProcessed);
		scheduled = (long) -1;
		if (Thread.currentThread().getId() != 1)
			threads.removeThreadById(Thread.currentThread().getId());
//		synchronized(this){
			numProcessed ++;  // data race not critical here ?
//		}
			
		me.setNotifiedScheduler(true);
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
	
	// gainControl and waitMyTurn are merged
	/*
	 * public void gainControl() { Log.i("MyScheduler",
	 * "Scheduler wants to gain control "); while (scheduled != -1) {
	 * schedulerThreadData.waitThread(); } Log.i("MyScheduler",
	 * "Scheduler gained control "); }
	 */
	
}


// scheduled and currentIndex are guaranteed to be not accessed by more than one
// threads concurrently
// either one of the application threads or the scheduler thread can access it