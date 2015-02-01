package ase.repeater;

import java.util.List;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import ase.AppRunTimeData;
import ase.AseTestBridge;
import ase.event.AseEvent;
import ase.util.log.Logger;


public class InputRepeater implements Runnable {
    private List<AseEvent> eventList;
    private int numDispatchedEvents = 0;
    private static int numHandledEvents = 0;
    private int numAllEvents = 0;
    private static Logger fileLog;    
    
    private final int MAX_RETRIALS = 50;
    private boolean terminationFlag = false;
    
    private Handler handler = new InputInjectionHandler(Looper.getMainLooper());
    
    public InputRepeater(List<AseEvent> events, Logger file) {
        eventList = events;
        numAllEvents = eventList.size();
        fileLog = file;
    }
    
    @Override
    public void run() {
        Log.i("Repeater", "In thread: " + Thread.currentThread().getName()
                + " " + Thread.currentThread().getId());

        Log.i("Repeater", "Repeating inputs.");
        if (eventList.isEmpty()) {
            Log.i("Repeater", "No events to repeat.");
        }
        int retrials = 0;
        
        while (numDispatchedEvents < numAllEvents) {
            AseEvent event = eventList.get(numDispatchedEvents);
            AseTestBridge.waitForDispatch();
            if(retrials > MAX_RETRIALS) {
                Log.e("InputRepeater", "MAX_RETRIALS exceeded");
                terminationFlag = true;
                AseTestBridge.notifyDispatcher();
                return;
            }

            // Problem with ActionBar tabs loading list fragments 
            // If the prev event is ActionBar tab, allow it to run
            if(numDispatchedEvents > 0 && eventList.get(numDispatchedEvents-1).type.equals(AseEvent.EventType.ACTIONBARTAB)) {
                try{
                    Thread.sleep(500);
                } catch (Exception e){
                    //
                }        
            }
            
            if(event.isFirable()) {
                sendEventToApp(event);
                numDispatchedEvents ++;              
                Log.i("Repeater", "InputsToGo: " + (numAllEvents - numDispatchedEvents) + "Posted: " + event.toString()); 
                Log.i("Repeater", "InputsToHandle: " + (numAllEvents - numHandledEvents)); 
                retrials = 0;
            }  else {
                retrials ++; 
            }
            Log.i("InputRep", "Notifies the scheduler");
            AseTestBridge.notifyDispatcher();
        }
        Log.i("Repeater", "Completed posting inputs.");
    }
    
    public boolean readyToInjectInput() {
        if(numDispatchedEvents >= numAllEvents) 
            return false;
        return !terminationFlag && eventList.get(numDispatchedEvents).isFirable();
    }
    
    public void sendEventToApp(AseEvent event) {
        Message m = handler.obtainMessage();
        m.obj = event;
        handler.sendMessage(m);
    }

    public void reset() {
        terminationFlag = false;
        numDispatchedEvents = 0;
        numHandledEvents = 0;
        Log.i("Repeater", "Is reset");
    }

    public boolean hasMoreInputs() {
        return (numAllEvents > numDispatchedEvents) && !terminationFlag;
    }
    
    public int numInputsLeft() {
        return (numAllEvents - numDispatchedEvents);
    }
    
    /**
     * Returns true if there are posted events in main looper
     * waiting to be processed
     */
    public boolean hasEventsToHandle() {
        return numDispatchedEvents > numHandledEvents;
    }
    
    public int numEventsToHandle() {
        return numDispatchedEvents - numHandledEvents;
    }
    
    public static void increaseNumHandledEvents() {
        numHandledEvents ++;
    }
    
    private static class InputInjectionHandler extends Handler {
        
        public InputInjectionHandler(Looper mainLooper) {
            super(mainLooper);
        }

        @Override
        public void handleMessage(Message message) {
            AseTestBridge.waitForDispatch();  

            // execute pending fragment transactions (run by the UI thread)
            AppRunTimeData.getInstance().executeFragmentTransactions(); 
            
            AseEvent event = (AseEvent) message.obj;
            event.injectEvent();
            Log.i("Repeated", "" + event.toString());
            fileLog.i("Repeated", "" + event.toString());
            
            increaseNumHandledEvents();
            
            //execute asynchronous transactions that load fragments 
            //AppRunTimeData.getInstance().executeFragmentTransactions();         
            AseTestBridge.notifyDispatcher();
        }
        
        public String toString(){
            return "Ase Handler for Event Injection";
        }
    }
}
