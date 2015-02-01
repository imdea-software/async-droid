package ase.event;

import android.util.Log;
import android.view.View;
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
    public AseItemClickEvent(int viewId, int itemPos, long id) {
        super(EventType.ITEMCLICK, viewId);
        this.itemPos = itemPos;
        this.itemId = id;
    }

    @Override
    public String toString() {
        return String.format("%s %d %d %d In fragment: %s", type.name(), viewId, itemPos, itemId, fragmentName);
    }

    @Override
    public boolean isFirable() {
        View view = AppRunTimeData.getInstance().getActivityRootView().findViewById(viewId);
        return super.isFirable() && (view != null); 
    }

    @Override
    public void injectEvent() {
        View view = AppRunTimeData.getInstance().getActivityRootView().findViewById(viewId);
        
        Log.i("Repeater", "LOG: INjecting: " + toString());
        if(view instanceof ListView) {
            ((ListView) view).smoothScrollToPosition(itemPos);
            ((ListView) view).performItemClick(view, itemPos, itemId);
            Log.i("Repeater", "Clicked item view: " + Integer.toHexString(view.getId()) + " Position: " + itemPos);
        } else {
            Log.i("Repeater", "Cannot replay adapter views other than ListView");
        }
    }
}
