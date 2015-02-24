package ase.event;

import java.util.List;

import org.json.JSONObject;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;

import ase.AppRunTimeData;

public class AseCheckBoxEvent extends AseEvent {
    
    private int position;
    private int parentId;

    // parentId here is the component's listview id
    public AseCheckBoxEvent(int viewId, List<Integer> path, int parentId, int pos) {
        super(EventType.CHECKBOX, viewId, path, parentId);
        this.parentId = parentId;
        this.position = pos;      
    }

    public AseCheckBoxEvent(JSONObject jsonEvent) {
        super(EventType.CHECKBOX, jsonEvent);
        this.parentId = jsonEvent.optInt("parentId", -1);
        this.position = jsonEvent.optInt("position", -1);
    }

    @Override
    public String toString() {
        return String.format("%s %d Position: %d In fragment: %s", type.name(), viewId, position, fragmentName);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean isFirable() {
        if(!super.isFirable()) return false;

        View parent = AppRunTimeData.getInstance().getActivityRootView().findViewById(parentId);
        if(parent == null) return false;
            
        if(parent instanceof AdapterView) {
            //Log.i("Repeater", "an adapter view with child count: " + ((AdapterView) parent).getChildCount());
            if (((AdapterView) parent).getChildCount() <= position)
                return false;
        }
        
        return true;
    }
    
    @Override
    public void injectEvent() {
        ListView parentView = (ListView) AppRunTimeData.getInstance().getActivityRootView().findViewById(parentId);
        // In current version, only for Checkboxes in ListViews
        if(parentView == null) {
            Log.e("Repeater", "Checkbox not in listviews are not supported.");
        }
            
        int totalPos = position + parentView.getHeaderViewsCount();
        CheckBox view = (CheckBox)parentView.getChildAt(totalPos).findViewById(viewId);
        
        view.performClick();
        
        Log.i("Repeater", "In viewgroup: " + parentId + " clicked checkbox: " + Integer.toHexString(view.getId()) + " Position: " + totalPos + " Fragment: " + fragmentName);
    }

    @Override
    public JSONObject toJson() throws Exception {
        JSONObject json = super.toJson();
        json.put("parentId", parentId)
            .put("position", position);
        return json;
    }
}
