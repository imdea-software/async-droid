package ase.repeater;

import java.util.List;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import ase.*;
import ase.util.log.Logger;


public class InputRepeater implements Runnable {
    private List<AseEvent> eventList;
    private int numDispatchedEvents = 0;
    private int numAllEvents = 0;
    private static Logger fileLog;    
    private boolean postpone = false;
    
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
        
        while (numDispatchedEvents < numAllEvents) {
            AseEvent event = eventList.get(numDispatchedEvents);
            if(event.isFirable()) {
                AseTestBridge.waitForDispatch();
                
                //// To reproduce the bug (to be removed)
                if(event.type.equals(AseEvent.EventType.CHECKBOX) && postpone) {
                    postpone = false;
                    AseTestBridge.notifyDispatcher();
                    continue;
                }
                if(event.type.equals(AseEvent.EventType.CHECKBOX)) {
                    postpone = true;
                }
                ////////
                
                sendEventToApp(event);
                numDispatchedEvents ++;
                
                Log.i("Repeater", "Posted a click.. InputsToGo:" + (numAllEvents - numDispatchedEvents));
                AseTestBridge.notifyDispatcher();
            }   
        }
        Log.i("Repeater", "Completed posting inputs.");
    }
    
    public boolean readyToInjectInput() {
        if(numDispatchedEvents >= numAllEvents) 
            return false;
        return eventList.get(numDispatchedEvents).isFirable();
    }
    
    public void sendEventToApp(AseEvent event) {
        Message m = handler.obtainMessage();
        m.obj = event;
        handler.sendMessage(m);
    }

    public void reset() {
        numDispatchedEvents = 0;
    }

    public boolean hasMoreInputs() {
        return numAllEvents > numDispatchedEvents;
    }
    
    private static class InputInjectionHandler extends Handler {
        
        public InputInjectionHandler(Looper mainLooper) {
            super(mainLooper);
        }

        @Override
        public void handleMessage(Message message) {
            AseTestBridge.waitForDispatch();
            
            // execute asynchronous transactions that load fragments 
            AppRunTimeData.getInstance().executeFragmentTransactions();
            
            AseEvent event = (AseEvent) message.obj;
            event.injectEvent();
            fileLog.i("Repeated", "" + event.toString());
            AseTestBridge.notifyDispatcher();
        }
        
        public String toString(){
            return "Ase Handler for Event Injection";
        }
    }
}
