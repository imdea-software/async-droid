package ase.event;

import java.util.ArrayList;

import org.json.JSONObject;

import android.util.Log;
import ase.AppRunTimeData;

public class AseBackButtonEvent extends AseEvent {

    public AseBackButtonEvent(int viewId) {
        super(EventType.BACK, viewId, new ArrayList<Integer>());
    }

    public AseBackButtonEvent(JSONObject jsonEvent) {
        super(EventType.BACK, jsonEvent);
    }

    @Override
    public boolean isFirable() {
        // TODO add more conditions using the recorded state?
        return true;
    }
    
    @Override
    public void injectEvent() {
        AppRunTimeData.getInstance().getCurrentAct().onBackPressed();
        Log.i("repeater", "Moved to back");
    }
    
    @Override
    public String toString() {
        return String.format("%s", type.name());
    }

}
