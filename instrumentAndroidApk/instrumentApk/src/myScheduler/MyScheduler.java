package myScheduler;


// static interface class between the scheduler and the app
public class MyScheduler {

	private static boolean initiated = false;

	private static int MIN_NUM_PROCESSED = 10;
	private static int numDelays = 3;
	private static int[] numIndices = {3, 5, 8};

	private static SchedulerRunnable sch;

	public static void initiateScheduler() // called by UI thread
	{
		if (!initiated) {
			sch = new SchedulerRunnable(numDelays, numIndices, MIN_NUM_PROCESSED); // pass parameters by IPC
			Thread t = new Thread(sch);
			t.setName("MySchedulerThread");
			t.start();
			sch.sendThreadInfo();
			sch.notifyScheduler();
			initiated = true;
		}
	}

	// app thread waits for its signal to start/resume
	public static void waitMyTurn() {	
		sch.sendThreadInfo(); // add thread to the scheduling list
		sch.waitMyTurn(Thread.currentThread().getId());
	}
	
	// app thread yields
	public static void yield() {
		sch.yield(Thread.currentThread().getId());
	}
		
	// app thread notify scheduler when they are completed
	public static void notifyScheduler() {			
		sch.notifyScheduler();
	}
	
	// app thread enters in a monitor
	public void enterMonitor(){
		sch.enterMonitor();
	}
	
	// app thread exits a monitor
	public void exitMonitor(){
		sch.exitMonitor();
	}

}


