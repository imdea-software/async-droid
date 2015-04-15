package ase.event;

import java.util.List;

import org.json.JSONObject;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import ase.AppRunTimeData;

/**
 * Created by burcuozkan on 05/12/14.
 */
public class AseItemSelectedEvent extends AseEvent {

    public View itemView;
    public final int itemPos;
    public final long itemId;

    // viewId is the id of the AdapterView (parent of itemView)
    // pos is the position of the item in the AdapterView
    public AseItemSelectedEvent(int viewId, List<Integer> path, int itemPos, long id) {
        super(EventType.ITEMSELECTED, viewId, path);
        this.itemPos = itemPos;
        this.itemId = id;
    }

    public AseItemSelectedEvent(JSONObject jsonEvent) {
        super(EventType.ITEMSELECTED, jsonEvent);
        this.itemPos = jsonEvent.optInt("itemPos", -1);
        this.itemId = jsonEvent.optLong("itemId", -1);
    }

    @Override
    public String toString() {
        return String.format("%s %d %d %d In fragment: %s", type.name(), viewId, itemPos, itemId, fragmentName);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean isFirable() {
        if(!super.isFirable()) return false;

        View parent = AppRunTimeData.getInstance().getActivityRootView().findViewById(viewId);
        if(parent == null) return false;
        
        if (((AdapterView) parent).getChildCount() <= itemPos)
            return false;
        
        return true; 

    }

    @SuppressWarnings("rawtypes")
    @Override
    public void injectEvent() {
        View adapter = AppRunTimeData.getInstance().getActivityRootView().findViewById(viewId);
        
        if(adapter instanceof AdapterView) {   
            View itemView = ((AdapterView)adapter).getChildAt(itemPos);
            ((AdapterView)adapter).getOnItemSelectedListener().onItemSelected((AdapterView)adapter, itemView, itemPos, itemId);
            //((AdapterView)view).performItemClick(view, itemPos, itemId);
            Log.i("Repeater", "Selected item view: " + Integer.toHexString(adapter.getId()) + " Position: " + itemPos);
        } else {
            Log.i("Repeater", "Cannot replay selected views - AdapterView is null");
        }
    }
    
    @Override
    public JSONObject toJson() throws Exception {
        JSONObject json = super.toJson();
        json.put("itemPos", itemPos)
            .put("itemId", itemId);
        return json;
    }
}
