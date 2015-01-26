package ase.event;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import ase.AppRunTimeData;
import ase.util.ReflectionUtils;

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
        // Do not use Activity.getActionBar as it initializes if the action bar is null
        Object actionBar = ReflectionUtils.getActionBarInstance(AppRunTimeData.getInstance().getCurrentAct());       
        if(actionBar == null) return false;
        else return true;
        
    }

    @Override
    public void injectEvent() {
        AppRunTimeData appData = AppRunTimeData.getInstance();
        Menu menu = appData.getActionBarMenu();
        MenuItem item = menu.findItem(viewId);
        appData.getCurrentAct().onOptionsItemSelected(item);
        Log.i("Repeater", "Clicked action bar view: " + Integer.toHexString(viewId));
    }
}
