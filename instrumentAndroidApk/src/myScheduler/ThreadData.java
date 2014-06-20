package myScheduler;


// provides synchronization of its corresponding Thread

public class ThreadData {

	private final Object signal = new Object();
	private long id;
	
	public ThreadData(long id)
	{
		this.id = id;
	}

	public Object getStartSignal() {
		return signal;
	}

	public long getId() {
		return id;
	}
	
}
