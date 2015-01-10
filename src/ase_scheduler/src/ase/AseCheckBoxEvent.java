package ase;

import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import ase.AseEvent;
import ase.recorder.ViewTraverser;
import ase.util.ReflectionUtils;

public class AseCheckBoxEvent extends AseEvent {
    
    private int position;

    // viewID here is the component's listview id
    public AseCheckBoxEvent(int viewId, int pos) {
        super(EventType.CHECKBOX, viewId);
        position = pos;
    }

    @Override
    public String toString() {
        return String.format("%s %d Position: %d", type.name(), viewId, position);
    }

    @Override
    public boolean isFirable() {
        View view = ViewTraverser.CURRENT_ROOT_VIEW.findViewById(viewId);
        return view != null;
    }
    
// Problem here - only clicks and replays the first item's checkbox
    @Override
    public void injectEvent() {
        CheckBox view = (CheckBox) ViewTraverser.CURRENT_ROOT_VIEW.findViewById(viewId);
        
        OnClickListener ownListener = ReflectionUtils.getOnClickListener(view);
        ownListener.onClick(view);
        
        //view.onCheckedChanged((CompoundButton)v, ((CompoundButton)v).isChecked());
        //view.performClick();
        //Log.i("Repeater", "Clicked checkbox: " + Integer.toHexString(view.getId()));
        Log.i("Repeater", "Clicked checkbox: " + Integer.toHexString(view.getId()) + " Position: " + position);
    }

}
