package myScheduler;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class DelayServiceConHandler {

	public static final int MSG_UNREGISTER_CLIENT = -1;
	public static final int MSG_REGISTER_CLIENT = 0;
	public static final int MSG_REQUEST_DELAYS = 1;
	public static final int MSG_RESPOND_DELAYS = 2;
	public static final int MSG_REG_OK = 5;
	
	private String delayService = "my.toy.service.START_SERVICE";
	ServiceConnection connection;
	private Messenger serviceMessenger = null;	
	private final Messenger messenger = new Messenger(new IncomingMessageHandler());
	boolean isBound;
	
	// get the context of the application to bind the service
	private Context context;
	
	public DelayServiceConHandler(Context c){
		context = c;
	}
	
	// Handle incoming messages from DelayService
	private static class IncomingMessageHandler extends Handler {		
		@Override
		public void handleMessage(Message msg) {
			Log.i("HERE","IncomingHandler:handleMessage" + msg.what);
			switch (msg.what) {
				case MSG_REG_OK:
					Log.i("MyScIPC", "Received Registration ACK"); 
					break;
				case MSG_RESPOND_DELAYS:
					int delays[] = msg.getData().getIntArray("delays");
					Log.i("MyScIPC", "Received delay info: " + delays[0] + delays[1] + delays[2] + delays.length); 
					break;
				default:
					Log.i("MyScIPC", "Unrecognized message from Delay Handler"); 
					break;
			}
		}
	}
	
	public void doStartService(){
		//Context context = getBaseContext();
		//Intent i= new Intent(context, DelayService.class);
		Intent i=new Intent(delayService);  
		context.startService(i); 
	}

	public void doBindService() {
		
		connection = new ServiceConnection() {		 
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				serviceMessenger = new Messenger(service);
				Log.i("MyScIPC", "Attached "); 
				try {
					Message msg = Message.obtain(null, MSG_REGISTER_CLIENT);
					msg.replyTo = messenger;
					serviceMessenger.send(msg);
				} 
				catch (RemoteException e) {
					// In this case the service has crashed before we could even do anything with it
				} 
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				// This is called when the connection with the service has been unexpectedly disconnected - process crashed.
				serviceMessenger = null;
				
			}
        };
        
		context.bindService(new Intent(delayService), connection, Context.BIND_AUTO_CREATE);
		isBound = true;
		Log.i("MyScIPC", "I am binding " + Thread.currentThread().getId()); 
	}

	public void doSendIPCMsg(){
		if (serviceMessenger != null) {
			try {
				int[] delays = {77, 88, 99};
				Message msg = Message.obtain(null, MSG_REQUEST_DELAYS);
				msg.replyTo = messenger;
				
				Bundle bResp = new Bundle();  //////added
	            bResp.putIntArray("oldDelays", delays); ///////added
	            msg.setData(bResp); ///////added
	            
				serviceMessenger.send(msg);        
	            
				Log.i("MyScIPC", "Service handler is NOTT null");
			} catch (RemoteException e) {
			}
		}
	}
	
	public void doUnbindService() {
		if (isBound) {
			// If we have received the service, and hence registered with it, then now is the time to unregister.
			if (serviceMessenger != null) {
				try {
					Message msg = Message.obtain(null, MSG_UNREGISTER_CLIENT);
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
