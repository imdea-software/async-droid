package ase.repeater;

import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import ase.AseEvent;
import ase.AseTestBridge;
import ase.util.IOFactory;
import ase.util.Reader;

public class InputRepeater implements Runnable {

    // its thread data is created and added into scheduler list when it registers (sends its info) and waits for its turn
    private List<AseEvent> eventList;
    private Handler handlerToUI;
    private View rootView;
    
    private int repeatCount = 1;
    
    public InputRepeater(Context context, View root) {
        rootView = root;
        Reader reader = IOFactory.getReader(context);
        eventList = reader.read();
        handlerToUI = new Handler(Looper.getMainLooper());  
    }

    @Override
    public void run() {
        Log.i("Repeater", "In thread: " + Thread.currentThread().getName());
        Iterator<AseEvent> events = eventList.listIterator();
        Log.i("Repeater", "Repeating inputs. Count: " + repeatCount);
        if(!events.hasNext()){
            Log.i("Repeater", "No events to repeat.");
        }
        while(events.hasNext()){
            AseTestBridge.waitMyTurn();
            sendEventToApp(events.next());        
            AseTestBridge.notifyScheduler();
        } 
        Log.i("Repeater", "Completed inputs. Count: " + repeatCount++);
    }

    // for now, only clicks
    public void sendEventToApp(final AseEvent event){
        handlerToUI.post(new Runnable(){
            public void run(){
                View view = rootView.findViewById(event.viewId);
                view.callOnClick();
                Log.i("Repeater", "Clicked view: " + Integer.toHexString(view.getId()) + " " + view.isEnabled() + " " + view.isShown());
            }
        });
    }
    
}
