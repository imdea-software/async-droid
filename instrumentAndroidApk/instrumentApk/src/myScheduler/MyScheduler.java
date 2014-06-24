package myScheduler;

import android.util.Log;

public class MyScheduler {

	private static PendingThreads threads = new PendingThreads();
	private static long scheduled = (long) 0; // id of the currently scheduled
												// thread (-1 for scheduler,
												// initially main thread is
												// enabled too)
	private static boolean initiated = false;

	private static ThreadData schedulerThread = new ThreadData(-1);

	public static void initiateScheduler() // called by UI thread
	{
		if (!initiated) {
			MyScheduler.schedule();
			MyScheduler.sendThreadInfo();
			MyScheduler.notifyScheduler();
			initiated = true;
		}
	}

	public static void schedule() {
		Thread t = new Thread(new Runnable() {

			public void run() {
				Log.i("MyScheduler", "Scheduler has started in thread: "
						+ Thread.currentThread().getName() + " Id: "
						+ Thread.currentThread().getId());

				// must wait until the main (UI) thread is added to the list
				gainControl();

				// In this draft version, next thread is selected in RR fashion
				// In the order they are registered to the Scheduler
				while (!threads.isEmpty()) {
					// no race condition on currentIndex, all other threads are
					// blocked
					threads.increaseWalker();
					notifyNext();
					Log.i("MyScheduler", "Scheduler is waiting for control");
					gainControl();
					Log.i("MyScheduler", "Scheduler has control");
				}

				// Does not complete in an app since UI thread does not
				// terminate
				Log.i("MyScheduler", "Scheduler has completed. Thread: "
						+ Thread.currentThread().getId());
			}
		});
		t.setName("MySchedulerThread");
		t.start();
	}

	// Send current thread info to the scheduler
	// Initial call bu UI thread
	// Then, called by threads running run, handleMessage and runInBackground
	// methods
	public static void sendThreadInfo() {
		long id = (long) Thread.currentThread().getId();
		if (!threads.capturedBefore(id)) {
			threads.addThread(new ThreadData(id));
			Log.i("MyScheduler", "I got " + Thread.currentThread().getName()
					+ " Id: " + Thread.currentThread().getId());
		}
	}

	// gainControl and waitMyTurn can be merged but I do not want to provide
	// parameters in the instrumentation
	public static void gainControl() {
		Log.i("MyScheduler", "Scheduler wants to gain control ");
		while (scheduled != -1) {
			schedulerThread.waitThread();
		}
		Log.i("MyScheduler", "Scheduler gained control ");
	}

	// worker waits for its signal to start/resume
	public static void waitMyTurn() {
		sendThreadInfo();
		ThreadData me = threads.getThreadById(Thread.currentThread().getId());

		Log.i("MyScheduler", "I am waiting: " + me.getId());

		while (scheduled != me.getId()) {

			me.waitThread();

		}

		Log.i("MyScheduler", "I am executing " + me.getId());
	}

	// notifySheduler and notifyNext can be merged but I do not want to provide
	// parameters in the instrumentation
	// a thread notifies scheduler that it has released CPU
	public static void notifyScheduler() {
		Log.i("MyScheduler", "I am releasing to scheduler. I am:"
				+ Thread.currentThread().getId());
		scheduled = (long) -1;
		schedulerThread.notifyThread();
	}

	// scheduler notifies the next task to be scheduled
	// if id == -1, then threads notify the scheduler
	private static void notifyNext() {
		ThreadData current = threads.getCurrentThread();
		scheduled = current.getId();
		Log.i("MyScheduler", "Scheduled thread id: " + scheduled);
		current.notifyThread();
	}

	public static void yield() {
		notifyScheduler();
		waitMyTurn();
	}

	// Threads notify scheduler when they are completed
	// This is also the case in message/runnable processing in a looper
	// In case no more messages arrive
	public static void notifyCompletion() {
		Log.i("MyScheduler", "Thread has completed. Id: " + scheduled);
		scheduled = (long) -1;
		if (Thread.currentThread().getId() != 1)
			threads.removeThreadById(Thread.currentThread().getId());
		schedulerThread.notifyThread();
	}

}

// scheduled and currentIndex are guaranteed to be not accessed by more than one
// threads concurrently
// either one of the application threads or the scheduler thread can access it
