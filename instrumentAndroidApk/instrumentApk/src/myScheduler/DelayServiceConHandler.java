package myScheduler;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

/* 
 * handles connection of Scheduler to the DelayService
 */
public class DelayServiceConHandler {

    public static final int MSG_ENDED_ACK = -2;
    public static final int MSG_END_TESTING = -1;
    public static final int MSG_START_TESTING = 0;
    public static final int MSG_STARTED_ACK = 1;
    public static final int MSG_NUMDELAYS_REQUEST = 2;
    public static final int MSG_NUMDELAYS_RESPONSE = 3;
    public static final int MSG_REPEAT_TEST = 5;
	   
	private String delayService = "my.apktester.test.START_SERVICE";
	private ServiceConnection connection;
	private Messenger serviceMessenger = null;	
	private final Messenger messenger = new Messenger(new IncomingMessageHandler());
	private boolean isBound;
	
	public int NUM_DELAYS; // will be received from DelayService
	public int NUM_INPUTS; // will be received from DelayService
	
	// get the context of the application to bind the service
	private Context context;
	
	public DelayServiceConHandler(Context c){
		context = c;
	}
	
	public int getNumDelays(){
	    return NUM_DELAYS;
	}
	
	public int getNumInputs(){
	    return NUM_INPUTS;
	}

	/*
	 *  Handle incoming messages from DelayService
	 */
	private class IncomingMessageHandler extends Handler {		
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MSG_STARTED_ACK:
					Log.i("MyScIPC", "Received test stated ACK"); 
	                   Log.i("MyScIPC", "Handler in scheduler: " + Thread.currentThread().getName() + " " + Thread.currentThread().getId()); 
					break;
				case MSG_NUMDELAYS_RESPONSE:
				    NUM_DELAYS = msg.getData().getInt("numDelays");
				    NUM_INPUTS = msg.getData().getInt("numInputs");
					Log.i("MyScIPC", "Received delay info: " + NUM_DELAYS); 
					Log.i("MyScIPC", "Received input info: " + NUM_INPUTS); 
					break;
				case MSG_ENDED_ACK:
                    Log.i("MyScIPC", "Received test ended ACK"); 
                    break;
				default:
					Log.i("MyScIPC", "Unrecognized message is received from DelayService"); 
					break;
			}
		}
	}
	
	public void doStartService(){
		Intent i=new Intent(delayService);  
		context.startService(i); 
	}

	public void doBindService() {
		
		connection = new ServiceConnection() {		 
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				serviceMessenger = new Messenger(service);
				Log.i("MyScIPC", "Scheduler is attached to DelayService"); 
				try {
					Message msg = Message.obtain(null, MSG_START_TESTING);
					msg.replyTo = messenger;
					serviceMessenger.send(msg);
				} 
				catch (RemoteException e) {
					// the service has crashed before we could even do anything with it
				} 
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				// the connection with the service has been unexpectedly disconnected - process crashed.
				serviceMessenger = null;
				
			}
        };
        
		context.bindService(new Intent(delayService), connection, Context.BIND_AUTO_CREATE);
		isBound = true;
		Log.i("MyScIPC", "I am binding " + Thread.currentThread().getId()); 
	}

	public boolean doSendIPCMsg(int msgType){
		if (serviceMessenger != null) {
			try {
				Message msg = Message.obtain(null, msgType);
				msg.replyTo = messenger;
				//Bundle bResp = new Bundle();
	            //bResp.putIntArray("prevDelays", delays);
	            //msg.setData(bResp); 
				serviceMessenger.send(msg);        
	            return true;			
			} catch (RemoteException e) {
			    Log.i("MyScIPC", "Could not send IPC message");
			}
		}
		return false;
	}
	
	public void doUnbindService() {
		if (isBound) {
			// If we have received the service, and hence registered with it, then now is the time to unregister.
			if (serviceMessenger != null) {
				try {
					Message msg = Message.obtain(null, MSG_END_TESTING);
					msg.replyTo = messenger;
					serviceMessenger.send(msg);
				} catch (RemoteException e) {
					// There is nothing special we need to do if the service has crashed.
				}
			}
			// Detach our existing connection.
			context.unbindService(connection);
			isBound = false;
			Log.i("MyScIPC", "Unbinding"); 
		}
	}
	
	public void doStopService(){
		context.stopService(new Intent(delayService));
		Log.i("MyScIPC", "Service is stopped.");
	}

}
