package ase.event;

import java.util.ArrayList;

import org.json.JSONObject;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import ase.AppRunTimeData;
import ase.util.ReflectionUtils;

/**
 * Created by burcuozkan on 10/12/14.
 */
public class AseActionBarEvent extends AseEvent {

    // viewId is the menu item id
    public AseActionBarEvent(int menuItemId) {
        super(EventType.ACTIONBAR, menuItemId, new ArrayList<Integer>());
    }

    public AseActionBarEvent(JSONObject jsonEvent) {
        super(EventType.ACTIONBAR, jsonEvent);
    }

    @Override
    public String toString() {
        return String.format("%s %d", type.name(), viewId);
    }

    @Override
    public boolean isFirable() {
        Object actionBar = ReflectionUtils.getActionBarInstance(AppRunTimeData.getInstance().getCurrentAct());       
        return actionBar != null; 
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
