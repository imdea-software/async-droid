package ase.event;

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
    public AseCheckBoxEvent(int viewId, int parentId, int pos) {
        super(EventType.CHECKBOX, viewId, parentId);
        this.parentId = parentId;
        this.position = pos;      
    }

    @Override
    public String toString() {
        return String.format("%s %d Position: %d In fragment: %s", type.name(), viewId, position, fragmentName);
    }

    @Override
    public boolean isFirable() {
        View view = AppRunTimeData.getInstance().getActivityRootView().findViewById(viewId);
        if(view == null) return false;

        View parent = AppRunTimeData.getInstance().getActivityRootView().findViewById(parentId);
        if(parent != null) {
            if(parent instanceof AdapterView)
                //Log.i("Repeater", "an adapter view with child count: " + ((AdapterView) parent).getChildCount());
            if (((AdapterView) parent).getChildCount() <= position)
                return false;
        }
        return super.isFirable();
    }
    
    @SuppressWarnings("rawtypes")
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

}
