package ase.event;

import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.CheckBox;

import ase.AppRunTimeData;
import ase.util.ReflectionUtils;

public class AseCheckBoxEvent extends AseEvent {
    
    private int position;
    private int parentId;

    // parentId here is the component's listview id
    public AseCheckBoxEvent(int viewId, int parentId, int pos) {
        super(EventType.CHECKBOX, viewId, parentId);
        this.parentId = parentId;
        position = pos;
        
    }

    @Override
    public String toString() {
        return String.format("%s %d Position: %d In fragment: %s", type.name(), viewId, position, fragmentName);
    }

    @Override
    public boolean isFirable() {
        View view = AppRunTimeData.getInstance().getActivityRootView().findViewById(viewId);
        return super.isFirable() && (view != null);
    }
    
    @SuppressWarnings("rawtypes")
    @Override
    public void injectEvent() {
        //CheckBox view = (CheckBox) AseTestBridge.getAppData().getActivityRootView().findViewById(viewId);
        
        AdapterView parentView = (AdapterView) AppRunTimeData.getInstance().getActivityRootView().findViewById(parentId);
        CheckBox view = (CheckBox)parentView.getChildAt(position).findViewById(viewId);
        
        OnClickListener ownListener = ReflectionUtils.getOnClickListener(view);
        ownListener.onClick(view);
        
        Log.i("Repeater", "In viewgroup: " + parentId + " clicked checkbox: " + Integer.toHexString(view.getId()) + " Position: " + position + " Fragment: " + fragmentName);
    }

}
