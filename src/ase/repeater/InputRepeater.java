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
import ase.recorder.ViewTraverser;
import ase.util.IOFactory;
import ase.util.Reader;

public class InputRepeater implements Runnable {

    // its thread data is created and added into scheduler list when it
    // registers (sends its info) and waits for its turn
    private List<AseEvent> eventList;
    private Handler handlerToUI;
    private int repeatCount = 1;

    public InputRepeater(Context context) {
        Reader reader = IOFactory.getReader(context);
        eventList = reader.read();
        handlerToUI = new Handler(Looper.getMainLooper());
    }

    @Override
    public void run() {
        Log.i("Repeater", "In thread: " + Thread.currentThread().getName()
                + " " + Thread.currentThread().getId());
        Iterator<AseEvent> events = eventList.listIterator();
        Log.i("Repeater", "Repeating inputs. Count: " + repeatCount);
        if (!events.hasNext()) {
            Log.i("Repeater", "No events to repeat.");
        }
        while (events.hasNext()) {
            AseTestBridge.waitMyTurn();
            sendEventToApp(events.next());
            AseTestBridge.notifyScheduler();
        }

        Log.i("Repeater", "Completed inputs. Count: " + repeatCount++);
    }

    // for now, only clicks
    public void sendEventToApp(final AseEvent event) {
        handlerToUI.post(new Runnable() {
            public void run() {
                AseTestBridge.waitMyTurn();
                View view = ViewTraverser.CURRENT_ROOT_VIEW
                        .findViewById(event.viewId);

                /*
                 while(view == null){ // if null, always null.. no time to inflate new (?) 
                     AseTestBridge.notifyScheduler();
                     AseTestBridge.waitMyTurn(); 
                     view = ViewTraverser.CURRENT_ROOT_VIEW.findViewById(event.viewId); 
                     }
                 */
                if (view == null) {
                    handlerToUI.post(this); // temporary solution, disrupts
                                            // order!!
                    Log.i("Repeater",
                            "Sending again "
                                    + Integer.toHexString(event.viewId));
                    return;
                }
                view.callOnClick();
                Log.i("Repeater",
                        "Clicked view: " + Integer.toHexString(view.getId())
                                + " " + view.isEnabled() + " " + view.isShown()
                                + " In Thread: "
                                + Thread.currentThread().getName());

                AseTestBridge.notifyScheduler();
            }
        });
    }

}
