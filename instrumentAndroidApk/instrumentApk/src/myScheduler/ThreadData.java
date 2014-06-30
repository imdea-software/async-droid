package myScheduler;

// provides synchronization of its corresponding Thread

public class ThreadData {

	private long id;
	
	private int currentMonitors = 0;
	private boolean toBeScheduled = true; 
	// true: when it can be suspended - can wait for its turn
	// false: when it has monitors
	private boolean notifiedScheduler = false; 
	// true: it notified scheduler after its execution
	// false: it is scheduled but still not released to scheduler
	// added to disable sending more than one notifications to the scheduler
	// (instrumentation might add more than one notify statements)

	
	public ThreadData(long id)
	{
		this.id = id;
	}

	public long getId() {
		return id;
	}
	
	public synchronized void notifyThread(){
		notifiedScheduler = false; // needs to notify scheduler after its execution
		this.notify();
	}
	
	public synchronized void waitThread(){
		try {
			this.wait();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void enteredMonitor(){
		currentMonitors ++;
	}
	
	public void exitedMonitor(){
		currentMonitors --;
	}
	
	public int getCurrentMonitors(){
		//Log.i("MyScheduler", "Monitors: " + currentMonitors );
		return currentMonitors;
	}

	public boolean willBeScheduled() {
		return toBeScheduled;
	}

	public void setToBeScheduled(boolean b) {
		this.toBeScheduled = b;
	}
	
	public boolean didNotifyScheduler() {
		return notifiedScheduler;
	}

	public void setNotifiedScheduler(boolean notifiedScheduler) {
		this.notifiedScheduler = notifiedScheduler;
	}

}
