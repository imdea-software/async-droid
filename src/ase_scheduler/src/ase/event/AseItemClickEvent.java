package ase.event;

import java.util.List;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import ase.AppRunTimeData;

/**
 * Created by burcuozkan on 05/12/14.
 */
public class AseItemClickEvent extends AseEvent {

    public final int itemPos;
    public final long itemId;

    // viewId is the id of the AdapterView
    // pos is the position of the item in the AdapterView
    public AseItemClickEvent(int viewId, List<Integer> path, int itemPos, long id) {
        super(EventType.ITEMCLICK, viewId, path);
        this.itemPos = itemPos;
        this.itemId = id;
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

    @Override
    public void injectEvent() {
        View view = AppRunTimeData.getInstance().getActivityRootView().findViewById(viewId);
        
        if(view instanceof ListView) {
            ((ListView) view).smoothScrollToPosition(itemPos);
            ((ListView) view).performItemClick(view, itemPos, itemId);
            Log.i("Repeater", "Clicked item view: " + Integer.toHexString(view.getId()) + " Position: " + itemPos);
        } else {
            Log.i("Repeater", "Cannot replay adapter views other than ListView");
        }
    }
}
