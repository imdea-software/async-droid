package myScheduler;

import android.content.Context;



// static interface class between the scheduler and the app
public class MyScheduler {

	private static boolean initiated = false;

	//private static int MIN_NUM_PROCESSED = 10;
	//private static int numDelays = 3;
	//private static int[] numIndices = {3, 5, 8};

	private static SchedulerRunnable sch;
	private static DelayServiceConHandler delayCon;

	// called by UI thread with the application context
	public static void initiateScheduler(Context context) 
	{
		if (!initiated) {
			delayCon = new DelayServiceConHandler(context); // for IPC with DelayService
			sch = new SchedulerRunnable(delayCon); // to perform scheduling
			Thread t = new Thread(sch);
			t.setName("MySchedulerThread");
			t.start();
			sch.wakeScheduler();
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


