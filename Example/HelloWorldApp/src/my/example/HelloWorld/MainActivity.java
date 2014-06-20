package my.example.HelloWorld;

import android.app.Activity;
import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MainActivity extends Activity {

	private HelloHandlerThread handlerWorker;
	private Looper looper;
	private MyHandler handler;

	private int UIEvents = 0;
	private int messages = 0;
	private int runnables = 0;
	private int asyncTasks = 0;
	private int asyncTasksExec = 0;
	private int threads = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		
		// create a background looper thread
		handlerWorker = new HelloHandlerThread("HelloHandlerThread");
		looper = handlerWorker.getLooper();
	    handler = new MyHandler(looper);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}
	
	// create a simple worker thread that runs in an endless loop
	public void processUI(View view) {
		// Release CPU and allow other threads to run
		// To be moved to UIEvents thread later
		TextView text = (TextView) findViewById(R.id.edit_message1);
		text.setText("Event #" + ++UIEvents);
	}
	
	// send message to a background looper thread
	public void sendMessage(View view) {
		TextView text = (TextView) findViewById(R.id.edit_message2);
		text.setText("Taken #" + ++messages);
    	Message msg = handler.obtainMessage();
    	msg.what = messages;
        handler.sendMessage(msg);
	}
	
	// post runnable to a background looper thread
	public void postRunnable(View view) {
		TextView text = (TextView) findViewById(R.id.edit_message3);
		text.setText("Taken #" + ++runnables);
		handlerWorker.postSleepAndPrint();
	}
	
	public void createAsyncTask(View view) {
		TextView text = (TextView) findViewById(R.id.edit_message4);
		text.setText("Created #" + ++asyncTasks);
		AsyncTask<Integer, Integer, Void> asyncTask = new HelloAsyncTask();
		asyncTask.execute(asyncTasks);
	}
	
	public void createAsyncTaskOnExecutor(View view) {
		TextView text = (TextView) findViewById(R.id.edit_message5);
		text.setText("Created #" + ++asyncTasksExec);
		AsyncTask<Integer, Integer, Void> asyncTask = new HelloAsyncTask();
		asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, asyncTasks);
	}
	
	// create a simple worker thread that runs in an endless loop
	public void createThread(View view) {
		TextView text = (TextView) findViewById(R.id.edit_message6);
		text.setText("Created #" + ++threads);
		Thread worker = new HelloThread(threads);
		worker.start();
	}
	
	private class HelloThread extends Thread {
		
		private final int id;
		
		public HelloThread(int id){
			this.id = id;
		}
		public void run() {
			
	        	try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
	        	
	        	Log.i("HelloThread", "Worker instance #" + id + " is running on thread " + Thread.currentThread().getId());

		}
	}
	
	private final class MyHandler extends Handler {
	    public MyHandler(Looper looper) {
	        super(looper);
	    }
	 
	    @Override
	    public void handleMessage(Message msg) {
	    	try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	        Log.i("HandlerThread", "Message #" + msg.what + " is processed in Thread " + Thread.currentThread().getId());    
	    }
	}
	
	private static class HelloHandlerThread extends HandlerThread {
	    Handler mHandler = null;

	    HelloHandlerThread(String name) {
	        super(name);
	        start();
	        mHandler = new Handler(getLooper());
	    }

	    void postSleepAndPrint() {
	        mHandler.post(new Runnable() {
	            @Override
	            public void run() {
	    	        try {
	    				Thread.sleep(2000);
	    			} catch (InterruptedException e) {
	    				e.printStackTrace();
	    			}
	    	        Log.i("HandlerThread", "Runnable" + " is processed in Thread " + Thread.currentThread().getId()); 
	            }
	        });

	    }
	    
	}
	
	private class HelloAsyncTask extends AsyncTask <Integer, Integer, Void>{
		@Override
		protected Void doInBackground(Integer... id) {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        	Log.i("AsycTask", "Background job of AsyncTask #" + id.toString() + " is running on thread " + Thread.currentThread().getId());
        	publishProgress(id);
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        	return null;
  
		}

		@Override
		protected void onPreExecute() {
			Log.i("AsyncTask", "Preexecuting.... " + Thread.currentThread().getId());
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(Void result) {
			Log.i("AsyncTask", "Postexecuting.... " + Thread.currentThread().getId());
			super.onPostExecute(result);
		}

		@Override
		protected void onProgressUpdate(Integer... id) {
			Log.i("AsyncTask", "Progress update job of AsyncTask #" + id.toString() + " is running on thread " + Thread.currentThread().getId());
			super.onProgressUpdate(id);
		}
		
	}

}
