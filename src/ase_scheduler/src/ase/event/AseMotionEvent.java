package ase.event;

import java.util.List;

import org.json.JSONObject;

import android.util.Log;
import android.view.MotionEvent;
import ase.AppRunTimeData;

//TODO: more MotionEvent attributes  
public class AseMotionEvent extends AseEvent {

    private long downTime;
    private long eventTime;
    private int action;
    private float x;
    private float y; 
    private int metaState;
    
    public AseMotionEvent(int viewId, List<Integer> path, long downTime, long eventTime, int action, float x, float y, int metaState) {
        super(EventType.MOTIONEVENT, viewId, path);
        this.downTime = downTime;
        this.eventTime = eventTime;
        this.action = action;
        this.x = x;
        this.y = y;
        this.metaState = metaState;
    }

    public AseMotionEvent(JSONObject jsonEvent) {
        super(EventType.MOTIONEVENT, jsonEvent);
        this.downTime = jsonEvent.optLong("downTime", -1);
        this.eventTime = jsonEvent.optLong("eventTime", -1);
        this.action = jsonEvent.optInt("action", -1);
        this.x = (float) jsonEvent.optDouble("x", -1);
        this.y = (float) jsonEvent.optDouble("y", -1);
        this.metaState = jsonEvent.optInt("metaState", -1);
    }

    @Override
    public String toString() {
        return String.format("%s x: %f y: %f Action: %d In fragment: %s", type.name(), x, y, action, fragmentName);
    }

    @Override
    public boolean isFirable() {
        return super.isFirable();
    }

    @Override
    public void injectEvent() {
        MotionEvent event = MotionEvent.obtain(downTime, eventTime, action, x, y, metaState);
        AppRunTimeData.getInstance().getActivityRootView().onTouchEvent(event);
        Log.i("Repeater", "Touch event: " + " x: " + x + " y: " + y + " Action: " + action);
    }
    
    @Override
    public JSONObject toJson() throws Exception {
        JSONObject json = super.toJson();
        json.put("downTime", downTime)
            .put("eventTime", eventTime)
            .put("action", action)
            .put("x", x)
            .put("y", y)
            .put("metaState", metaState);
        return json;
    }
}
