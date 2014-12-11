package ase;

import android.util.Log;
import android.view.View;
import ase.recorder.ViewTraverser;

/**
 * Created by burcuozkan on 06/12/14.
 */
public class AseClickEvent extends AseEvent {

    public AseClickEvent(int viewId) {
        super(EventType.CLICK, viewId);
    }

    @Override
    public String toString() {
        return String.format("%s %d", type.name(), viewId);
    }

    @Override
    public boolean isFirable() {
        View view = ViewTraverser.CURRENT_ROOT_VIEW.findViewById(viewId);
        return view != null;
    }

    @Override
    public void injectEvent() {
        View view = ViewTraverser.CURRENT_ROOT_VIEW.findViewById(viewId);
        //view.callOnClick();
        view.performClick();
        Log.i("Repeater", "Clicked view: " + Integer.toHexString(view.getId()));
    }
}
