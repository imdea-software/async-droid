package my.apktester.test;

import java.util.ArrayList;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.view.View;

import com.robotium.solo.Solo;

@SuppressWarnings("unchecked")
public class TestApk extends ActivityInstrumentationTestCase2 {

	private static final String LAUNCHER_ACTIVITY_FULL_CLASSNAME = "my.example.HelloWorld.MainActivity";
	private static Class launcherActivityClass;	
	private static DelayServiceConHandler serviceHandler;
	
	// to wait/notify the end of test:
	private static Object testCompleted = new Object();
	private static boolean isTestCompleted = false;
	
	static {

		try {
			launcherActivityClass = Class
					.forName(LAUNCHER_ACTIVITY_FULL_CLASSNAME);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public TestApk() throws ClassNotFoundException {
		super(launcherActivityClass);
	}

	private Solo solo;

	@Override
	protected void setUp() throws Exception {
	    serviceHandler = new DelayServiceConHandler(getInstrumentation().getContext());
	    serviceHandler.doBindService();
	    while(!serviceHandler.doSendIPCMsg(DelayServiceConHandler.MSG_REGISTER_TESTER));        
		solo = new Solo(getInstrumentation(), getActivity());
	}

	public void testApk() {
	
	    // repeat inputs if:
	    // (1) repetition is requested
	    // (2) current test (min # of segments) requires more inputs
	    while(serviceHandler.repeat == true || !isTestCompleted){
	        if(serviceHandler.repeat == true)
                Log.i("Tester", "Repeat is true..");
	        serviceHandler.repeat = false;
	        Log.i("Tester", "Giving inputs");
	        giveInputs(); // provide the set of inputs
	    }
		
		// wait for the last set of inputs to be completed
	    // and ending message sent by MyScheduler
		synchronized(testCompleted){
    		while(!isTestCompleted){
    		    try {
                    testCompleted.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
    		}
		}		
		Log.i("Tester", "Test completed, terminating.");
	}
	
	public void giveInputs(){
	    ArrayList<View> myViews = solo.getViews();

        for(View v: myViews){
            Log.i("Tester", "view: " + v.toString());
            if(v.isClickable())
                solo.clickLongOnView(v);
        }
	}

	@Override
	public void tearDown() throws Exception {
	    Log.i("Tester", "Test at tear down.");
		solo.finishOpenedActivities();
	}
	

	public class DelayServiceConHandler {

	    public static final int MSG_REPEAT_TEST = 5;
	    public static final int MSG_REGISTER_TESTER = 2;
	    public static final int MSG_UNREGISTER_TESTER = 3;
	    public static final int MSG_END_TESTING = -1;

	    private String delayService = "my.apktester.test.START_SERVICE";
	    private ServiceConnection connection;
	    private Messenger serviceMessenger = null;
	    
	    private boolean isBound;

	    // get the context of the application to bind the service
	    private Context context;
	    private HandlerThread ht;
	    private final Messenger messenger;
	    private boolean repeat = false;

	    public DelayServiceConHandler(Context c) {
	        context = c;
	        ht = new HandlerThread("TesterMessageHandler");
	        ht.start();
	        messenger = new Messenger(new IncomingMessageHandler(ht.getLooper()));
	    }
	    
	    public void repeatTest(){
	        repeat = true;
	    }
	    
	    /*
	     * Handle incoming messages from DelayService
	     */
	    private class IncomingMessageHandler extends Handler {
	        public IncomingMessageHandler(Looper looper) {
	            super(looper);
	          }
	        @Override
	        public void handleMessage(Message msg) {
	            switch (msg.what) {
	            case MSG_REPEAT_TEST:
	                Log.i("MyScIPC", "Tester is repeating the test.");
	                repeatTest();
	                break;
	            case MSG_END_TESTING:
                    Log.i("MyScIPC", "Tester received the request to end.");
                    synchronized(testCompleted){
                        testCompleted.notify();
                    }
                    isTestCompleted = true;
                    break;
	            default:
	                Log.i("MyScIPC", "Unrecognized message is received from DelayService");
	                break;
	            }
	        }
	    }

	    public void doStartService() {
	        Intent i = new Intent(delayService);
	        context.startService(i);
	    }

	    public void doBindService() {

	        connection = new ServiceConnection() {
	            @Override
	            public void onServiceConnected(ComponentName name, IBinder service) {
	                serviceMessenger = new Messenger(service);
	                Log.i("MyScIPC", "Tester is attached to DelayService");
	            }

	            @Override
	            public void onServiceDisconnected(ComponentName name) {
	                // the connection with the service has been unexpectedly
	                // disconnected - process crashed.
	                serviceMessenger = null;
	            }
	        };

	        context.bindService(new Intent(delayService), connection,
	                Context.BIND_AUTO_CREATE);
	        isBound = true;
	        Log.i("MyScIPC", "I am binding " + Thread.currentThread().getId());
	    }

	    public boolean doSendIPCMsg(int msgType) {
	        if (isBound && serviceMessenger != null) {
	            try {
	                Message msg = Message.obtain(null, msgType);
	                msg.replyTo = messenger;
	                serviceMessenger.send(msg);
	                return true;
	            } catch (RemoteException e) {
	                Log.i("MyScIPC", "Could not send IPC message");
	            }
	        }	     
	        return false;
	    }

	    public void doUnBindService() {
	        if (isBound) {
	            if (serviceMessenger != null) {
	                try {
	                    Message msg = Message.obtain(null, MSG_UNREGISTER_TESTER);
	                    msg.replyTo = messenger;
	                    serviceMessenger.send(msg);
	                } catch (RemoteException e) {
	                    // the service has crashed.
	                }
	            }
	            // Detach our existing connection.
	            context.unbindService(connection);
	            isBound = false;
	            Log.i("MyScIPC", "Unbinding");
	        }
	    }

	    public void doStopService() {
	        context.stopService(new Intent(delayService));
	        Log.i("MyScIPC", "Service is stopped.");
	    }

	}
}

