package ase.event;

import java.util.ArrayList;

import android.util.Log;
import ase.AppRunTimeData;

public class AseBackButtonEvent extends AseEvent {

    protected AseBackButtonEvent(EventType type, int viewId) {
        super(type, viewId, new ArrayList<Integer>());
        // TODO Auto-generated constructor stub
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
