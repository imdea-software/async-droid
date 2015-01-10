package ase;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

//import com.android.internal.*;

import android.util.Log;
import android.app.ActionBar;

public class AseActionBarTabEvent extends AseEvent {

    public final int tabItemIndex;
    private static int prevSelectedTabIndex = 0;

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
    public boolean isFirable() { // check if ActionBArActivity and check tab index
       
        ActionBar actionBar = AseTestBridge.getCurrentActivity().getActionBar();
        if (actionBar != null && actionBar.isShowing()) {
            if(actionBar.getTabAt(tabItemIndex) != null)
                return true;
        }
        
        Log.i("Repeater", "Cannot find ActionBar tab at index: " + Integer.toHexString(tabItemIndex) );
        return false;
    }

    /*
     * TODO Not complete - Missing tasks in injecting tab event
     * When we select new tab programatically,it does not execute onTabUnselected
     * (prev tab highlighted to be selected and more jobs might remain unhandled) 
     * We cannot call tab.onTabUnselected, as we cannot read ActionBar.Tab listener, it is in internal code
     */
    @Override
    public void injectEvent() {
        ActionBar actionBar = AseTestBridge.getCurrentActivity().getActionBar();
        

        
        // bu classi oku, onTabUnselected methodunu cagir 
//       ((com.android.internal.app.ActionBarImpl.TabImpl)(actionBar.getTabAt(tabItemIndex))).
        //actionBar.getSelectedTab().
        //mSelectedTab.getCallback().onTabUnselected(mSelectedTab, trans);
        
        
        // (2) Call tabSelected for the new one
        //actionBar.selectTab(actionBar.getTabAt(tabItemIndex));
        actionBar.setSelectedNavigationItem(tabItemIndex);
        Log.i("Repeater", "Clicked action bar tab at index: " + Integer.toHexString(tabItemIndex));
  
    }

}
