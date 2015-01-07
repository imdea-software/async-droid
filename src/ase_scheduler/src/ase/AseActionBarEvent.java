package ase;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Created by burcuozkan on 10/12/14.
 */
public class AseActionBarEvent extends AseEvent {

    //public final int menuItemId;

    // viewId is the id of the ActionBAr
    // menuItemId is the id of the Menu item
    public AseActionBarEvent(int menuItemId) {

        super(EventType.ACTIONBAR, menuItemId);
        //this.menuItemId = menuItemId;
    }

    @Override
    public String toString() {
        return String.format("%s %d", type.name(), viewId);
    }

    @Override
    public boolean isFirable() {
        if (AseTestBridge.currentAct.hasWindowFocus()) {
            //AseTestBridge.currentAct.openOptionsMenu();
            Menu menu = AseTestBridge.actionBarMenu;
            if (menu != null) {
                MenuItem item = menu.findItem(viewId);
                return (item != null);
            }
        }
        return false;
    }

    @Override
    public void injectEvent() {
        //AseTestBridge.currentAct.openOptionsMenu();
        Menu menu = AseTestBridge.actionBarMenu;
        MenuItem item = menu.findItem(viewId);
        AseTestBridge.currentAct.onOptionsItemSelected(item);
        Log.i("Repeater", "Clicked action bar view: " + Integer.toHexString(viewId));

    }
}
