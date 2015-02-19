package ase.event;

import java.util.List;

import org.json.JSONObject;

import android.util.Log;
import android.view.View;
import ase.AppRunTimeData;

/**
 * Created by burcuozkan on 06/12/14.
 */
public class AseClickEvent extends AseEvent {

    public AseClickEvent(int viewId, List<Integer> path) {
        super(EventType.CLICK, viewId, path);
    }

    public AseClickEvent(JSONObject jsonEvent) {
        super(EventType.CLICK, jsonEvent);
    }

    @Override
    public String toString() {
        return String.format("%s %d In fragment: %s", type.name(), viewId, fragmentName);
    }

    @Override
    public boolean isFirable() {
        return super.isFirable();
    }

    @Override
    public void injectEvent() {
        View view = AppRunTimeData.getInstance().getActivityRootView().findViewById(viewId);
        //view.callOnClick();
        view.performClick();
        Log.i("Repeater", "Clicked view: " + Integer.toHexString(view.getId()));
    }
}
