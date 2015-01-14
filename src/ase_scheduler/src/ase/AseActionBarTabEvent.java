package ase;

import android.util.Log;
import android.app.ActionBar;


public class AseActionBarTabEvent extends AseEvent {

    public final int tabItemIndex;

    // viewId is the id of the ActionBar
    // menuItemId is the id of the Menu item
    public AseActionBarTabEvent(int actionBarId, int tabItemIndex) {

        super(EventType.ACTIONBARTAB, actionBarId);
        this.tabItemIndex = tabItemIndex;
        
    }

    @Override
    public String toString() {
        return String.format("%s at index %d", type.name(), tabItemIndex);
    }

    @Override
    public boolean isFirable() { // check if ActionBarActivity and check tab index    
        ActionBar actionBar = AppRunTimeData.getInstance().getCurrentAct().getActionBar();
        if (actionBar != null && actionBar.isShowing()) {
            if(actionBar.getTabCount() > tabItemIndex)
                return true;
        }
        
        Log.i("Repeater", "Cannot find ActionBar tab at index: " + Integer.toHexString(tabItemIndex) );
        return false;
    }


    @Override
    public void injectEvent() {
        ActionBar actionBar = AppRunTimeData.getInstance().getCurrentAct().getActionBar();
        actionBar.setSelectedNavigationItem(tabItemIndex);
        Log.i("Repeater", "Clicked action bar tab at index: " + Integer.toHexString(tabItemIndex));
  
    }

}
