package myScheduler;

// provides synchronization of its corresponding Thread

public class ThreadData {

	private long id;
	
	private int currentMonitors = 0;
	private boolean toBeScheduled = true; // true when it is suspended

	
	public ThreadData(long id)
	{
		this.id = id;
	}

	public long getId() {
		return id;
	}
	
	public synchronized void notifyThread(){
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

}
