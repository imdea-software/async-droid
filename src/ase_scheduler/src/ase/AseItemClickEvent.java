package ase;

import android.util.Log;
import android.view.View;
import android.widget.ListView;
import ase.recorder.ViewTraverser;

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
        return String.format("%s %d %d %d", type.name(), viewId, itemPos, itemId);
    }

    @Override
    public boolean isFirable() {
        View view = ViewTraverser.CURRENT_ROOT_VIEW.findViewById(viewId);
        return view != null;
    }

    @Override
    public void injectEvent() {
        View view = ViewTraverser.CURRENT_ROOT_VIEW.findViewById(viewId);
        if(view instanceof ListView) {
            ((ListView) view).smoothScrollToPosition(itemPos);
            ((ListView) view).performItemClick(view, itemPos, itemId);
            Log.i("Repeater", "Clicked item view: " + Integer.toHexString(view.getId()) + " Position: " + itemPos);
        } else {
            Log.i("Repeater", "Cannot replay adapter views other than ListView");
        }
    }
}
