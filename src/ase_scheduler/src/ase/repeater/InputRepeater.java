package ase.repeater;

import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.ListView;
import ase.AseEvent;
import ase.AseItemClickEvent;
import ase.AseTestBridge;
import ase.recorder.ViewTraverser;


public class InputRepeater implements Runnable {

    // its thread data is created and added into scheduler list when it
    // registers (sends its info) and waits for its turn
    private List<AseEvent> eventList;
    private Handler handlerToUI;
    
    // to keep track of the status of InputRepeater thread's job
    // counts the number of posted event invoker runnables to UI
    // when >0, scheduler will schedule it (in its turn) even when it has not executed waitMyTurn()
    private int inputsToGo;
    
    // incremented when posted task to UI could invoke (view exists in current layout) the event
    // ensures the correct order of invocations when view is not in the layout
    private int inputsDispatched = 0;
    
    public InputRepeater(Context context, List<AseEvent> events) {
        eventList = events;
        handlerToUI = new Handler(Looper.getMainLooper());
        inputsToGo=eventList.size();
    }

    @Override
    public void run() {
        Log.i("Repeater", "In thread: " + Thread.currentThread().getName()
                + " " + Thread.currentThread().getId());

        // will loop only once
        // more efficient than if check and notify
        // each time root view is set in every onCreate
        while (ViewTraverser.CURRENT_ROOT_VIEW == null) {
            // do nothing
        }

        Log.i("Repeater", "Repeating inputs.");
        if (eventList.isEmpty()) {
            Log.i("Repeater", "No events to repeat.");
        }
        
        for (int i=0; i<eventList.size(); i++) {
            AseTestBridge.waitMyTurn();
            sendEventToApp();
            inputsToGo --;
            Log.i("Repeater", "Posted a click.. InputsToGo:" + inputsToGo);
            AseTestBridge.notifyScheduler();
        }

        Log.i("Repeater", "Completed posting inputs.");
    }
    
    // for now, only clicks
    public void sendEventToApp() {
        AseTestBridge.incNumUIBlocks();  // posts event (runnable to click) into UI thread
        handlerToUI.post(new Runnable() {
            public void run() {
                AseEvent event = eventList.get(inputsDispatched);
                AseTestBridge.waitMyTurn();
                View view = ViewTraverser.CURRENT_ROOT_VIEW.findViewById(event.viewId);

                if (view == null) {
                    handlerToUI.post(this);
                    Log.i("Repeater", "Sending again " + Integer.toHexString(event.viewId));
                    AseTestBridge.notifyScheduler();    
                    return;
                }
          
                // counter provides invoking the events in order
                incrementInputsDispatched();

                // call event listener
                injectEvent(event, view);

                AseTestBridge.decNumUIBlocks(); // runnable to click consumed
                AseTestBridge.notifyScheduler();
            }
        });
    }

    public void injectEvent(AseEvent event, View view) {
        if(event.type == AseEvent.EventType.CLICK) {
            //view.callOnClick();
            view.performClick();
            Log.i("Repeater", "Clicked view: " + Integer.toHexString(view.getId()));
        } else if(event.type == AseEvent.EventType.ITEMCLICK) {
            AseItemClickEvent clickEvent = (AseItemClickEvent) event;
            if(view instanceof ListView) {
                ((ListView) view).smoothScrollToPosition(clickEvent.itemPos);
                ((ListView) view).performItemClick(view, clickEvent.itemPos, clickEvent.itemId);
                Log.i("Repeater", "Clicked view: " + Integer.toHexString(view.getId()) + " Position: " + clickEvent.itemPos);
            } else {
                Log.i("Repeater", "Cannot replay adapter views other than ListView");
            }
        }
    }

    public void incrementInputsDispatched(){
        inputsDispatched ++;
    }

    public void reset() {
        inputsToGo=eventList.size();
        inputsDispatched = 0;
    }
    
    // no possible race on inputsToGo
    // scheduler thread calls it mutually exclusively
    public boolean hasMoreInputs() {
        return inputsToGo > 0;
    }
}
