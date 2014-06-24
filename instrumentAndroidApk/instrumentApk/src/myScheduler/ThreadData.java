package myScheduler;



// provides synchronization of its corresponding Thread

public class ThreadData {

//	private final Object signal = new Object();
	private long id;
	
	public ThreadData(long id)
	{
		this.id = id;
	}

//	public Object getStartSignal() {
//		return signal;
//	}

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
}
