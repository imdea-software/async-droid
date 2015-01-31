package ase.event;

import android.util.Log;
import android.app.ActionBar;
import ase.AppRunTimeData;
import ase.util.ReflectionUtils;

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
    public boolean isFirable() {  
        Object actionBar = ReflectionUtils.getActionBarInstance(AppRunTimeData.getInstance().getCurrentAct());
        if(actionBar == null) return false;
        
        // ActionBar is an instance of android.app.ActionBar or android.support.v7.app.ActionBar
        // Check if it is showing and get tabCound using reflection    
        if (actionBar != null && ReflectionUtils.isActionBarShowing(actionBar)) {
            if(ReflectionUtils.getActionBarTabCount(actionBar) > tabItemIndex)
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
