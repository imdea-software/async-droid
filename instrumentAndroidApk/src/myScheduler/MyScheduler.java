package myScheduler;

import java.util.ArrayList;

import android.util.Log;

public class MyScheduler {

	private static ArrayList<ThreadData> threads = new ArrayList<ThreadData>();	
	private final static Object schedulerSignal = new Object();

	private static long scheduled = (long) 0; // id of the currently scheduled thread (-1 for scheduler, initially main thread is enabled too)
	private static int currentIndex = -1;  // index of the threadData to be scheduled next, updated when a thread terminates
	private static boolean initiated = false; 

	public static void initiateScheduler() // called by UI thread
	{
		if (!initiated){
			MyScheduler.schedule();
			MyScheduler.sendThreadInfo();
			MyScheduler.notifyScheduler();
			initiated = true;
		}
	}
	
	public static void schedule() 
	{
		Thread t = new Thread( new Runnable () {
			public void run(){
				Log.i("MyScheduler", "Scheduler has started in thread: "  + Thread.currentThread().getName() + " Id: " + Thread.currentThread().getId());

				// must wait until the main (UI) thread	is added to the list
				gainControl();
				
				// In this draft version, next thread is selected in RR fashion
				// In the order they are registered to the Scheduler
				while (!threads.isEmpty()) {	
					// no race condition on currentIndex, all other threads are blocked
					currentIndex = (currentIndex + 1) % threads.size();
					
					notifyNext();
					gainControl();	
				}

				// Does not complete in an app since UI thread does not terminate
				Log.i("MyScheduler", "Scheduler has completed. Thread: " + Thread.currentThread().getId());
			}
		});
		t.setName("MySchedulerThread");
		t.start();
	}
	
	// scheduler notifies the next task to be scheduled
	private static void notifyNext()
	{	
		ThreadData current;
		synchronized(threads){
			current = threads.get(currentIndex);			
		}

		scheduled = current.getId();
		Log.i("MyScheduler", "Scheduled thread id: " + scheduled);
		
		synchronized (current.getStartSignal()) {
			current.getStartSignal().notify();
		}
	}
	
	// scheduler waits for the current scheduled task to release CPU
	private static void gainControl()
	{
		synchronized (schedulerSignal) {
			while (scheduled != -1) {
				try {
					schedulerSignal.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	// worker waits for its signal to start/resume
	public static void waitMyTurn()  
	{	
		MyScheduler.sendThreadInfo();
		ThreadData me = getThreadById(Thread.currentThread().getId());
		
		if(me == null)
			Log.i("MyScheduler", "ThreadData not in the listtttt ");
		Object mySignal = me.getStartSignal();
		
		synchronized (mySignal) { 
			while (scheduled != me.getId()) {
				try {
					mySignal.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	// Send current thread info to the scheduler
	// Initial call bu UI thread
	// Then, called by threads running run, handleMessage and runInBackground methods 
	public static void sendThreadInfo()
	{
		synchronized(threads){
			long id = (long) Thread.currentThread().getId();
			if(!capturedBefore(id)){
				threads.add(new ThreadData(id));
				Log.i("MyScheduler", "I got " + Thread.currentThread().getName() + " Id: " + Thread.currentThread().getId());
			}
		}
	
	}
	
	private static boolean capturedBefore(long id)
	{
		synchronized(threads){
		for(ThreadData td: threads){
			if(td.getId() == id)
				return true;
		}
		}
		return false;
	}
	
	// a thread notifies scheduler that it has released CPU
	public static void notifyScheduler() 
	{	
		synchronized (schedulerSignal) {
			scheduled = (long)-1;
			schedulerSignal.notify();
		}
	}
	
	public static void yield()
	{
		notifyScheduler();  
		waitMyTurn();  
	}
	
	// Threads notify scheduler when they are completed
	// This is also the case in message/runnable processing in a looper
	// In case no more messages arrive 
	public static void notifyCompletion()
	{
		synchronized (schedulerSignal) {
			Log.i("MyScheduler", "Thread has completed. Id: " + scheduled);
			
			synchronized(threads){
				threads.remove(getThreadById(Thread.currentThread().getId()));				
				currentIndex --;  // the index of the thread to be scheduled
			                  	// no race condition on currentIndex, scheduler and other threads are blocked
			}
			scheduled = (long)-1;
			schedulerSignal.notify();
		}
	}
	
	public static ThreadData getThreadById(long id)
	{
		synchronized(threads){
		for(ThreadData td: threads){
			if(td.getId() == id){ /// equals removed here !!!!!!!!!!!
				return td;
			}
		}
		}
		
		return null;
	}

}


// scheduled and currentIndex are guaranteed to be not accessed by more than one threads concurrently
// either one of the application threads or the scheduler thread can access it
