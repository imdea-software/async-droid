package ase;

import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import ase.AseEvent;
import ase.util.ReflectionUtils;

public class AseCheckBoxEvent extends AseEvent {
    
    private int position;

    // viewID here is the component's listview id
    public AseCheckBoxEvent(int viewId, int pos, String fragment) {
        super(EventType.CHECKBOX, viewId, fragment);
        position = pos;
    }

    @Override
    public String toString() {
        return String.format("%s %d Position: %d In fragment: %s", type.name(), viewId, position, fragmentName);
    }

    @Override
    public boolean isFirable() {
        View view = AseTestBridge.getAppData().getActivityRootView().findViewById(viewId);
        return super.isFirable() && (view != null);
    }
    
// Problem here - only clicks and replays the first item's checkbox
    @Override
    public void injectEvent() {
        CheckBox view = (CheckBox) AseTestBridge.getAppData().getActivityRootView().findViewById(viewId);
        
        OnClickListener ownListener = ReflectionUtils.getOnClickListener(view);
        ownListener.onClick(view);
        
        //view.onCheckedChanged((CompoundButton)v, ((CompoundButton)v).isChecked());
        //view.performClick();
        //Log.i("Repeater", "Clicked checkbox: " + Integer.toHexString(view.getId()));
        Log.i("Repeater", "Clicked checkbox: " + Integer.toHexString(view.getId()) + " Position: " + position);
    }

}
