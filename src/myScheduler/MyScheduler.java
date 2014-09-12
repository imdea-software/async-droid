package myScheduler;

import android.content.Context;

/*
 *  static interface class between the scheduler and the app under test
 */
public class MyScheduler {

	private static SchedulerRunnable sch;
	private static DelayServiceConHandler delayCon;

	/*
	 *  called by UI thread with the application context
	 */
	public static void initiateScheduler(Context context) 
	{
		if (delayCon == null) {
			delayCon = new DelayServiceConHandler(context); // for IPC with DelayService
			sch = new SchedulerRunnable(delayCon); // to perform scheduling
			Thread t = new Thread(sch);
			t.setName("MySchedulerThread");
			t.start();
			sch.wakeScheduler();
		}
	}

	/*
	 *  application thread waits for its signal to start/resume
	 */
	public static void waitMyTurn() {	
		sch.sendThreadInfo(); // add thread to the scheduling list
		sch.waitMyTurn(Thread.currentThread().getId());
	}
	
	/*
	 *  application thread yields
	 */
	public static void yield() {
		sch.yield(Thread.currentThread().getId());
	}
		
	/*
	 *  application thread notify scheduler when they are completed
	 */
	public static void notifyScheduler() {			
		sch.notifyScheduler();
	}
	
	/*
	 *  application thread enters in a monitor
	 */
	public void enterMonitor(){
		sch.enterMonitor();
	}
	
	/*
	 *  application thread exits a monitor
	 */
	public void exitMonitor(){
		sch.exitMonitor();
	}
	
	

}

