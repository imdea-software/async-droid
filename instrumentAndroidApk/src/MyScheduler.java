import android.util.Log;

public class MyScheduler {
	private static int c = 0;


	public static void getThreadInfo() {
		String threadName = Thread.currentThread().getName();
		Log.i("MyScheduler", threadName + " runs.");
	}
	
	public static synchronized void increase(int howmany) {
		c += howmany;
	}


	public static synchronized void report() {
		System.err.println("counter : " + c);
	}

	public static synchronized String readCounter() {
		return Integer.toString(c);
	}
}
