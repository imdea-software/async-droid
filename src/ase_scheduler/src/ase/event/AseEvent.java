package ase.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.app.Fragment;
import ase.AppRunTimeData;

public abstract class AseEvent {

    @SuppressWarnings("rawtypes")
    private static Map<EventType, Class> TYPE_MAP;
    
    public enum EventType {
        CLICK, CHECKBOX, ITEMCLICK, ACTIONBAR, ACTIONBARTAB, NAVIGATEUP
    }
    public final EventType type;
    public final int viewId;
    
    /*
     * If the view is inflated inside a fragment, keep the name of that fragment
     * An event is firable only if that fragment is visible 
     * (A view with the same id may exist in more than one fragments)
     */
    public final String fragmentName;

    protected AseEvent(EventType type, int viewId) {
        this.type = type;
        this.viewId = viewId;
        fragmentName = AppRunTimeData.getInstance().getFragmentNameByViewId(viewId);
    }
   
    protected AseEvent(EventType type, int viewId, int parentId) {
        this.type = type;
        this.viewId = viewId;
        fragmentName = AppRunTimeData.getInstance().getFragmentNameByViewId(viewId);
    }
    
    @SuppressWarnings("rawtypes")
    private static Map<EventType, Class> getTypeMap() {
        if (TYPE_MAP == null) {
            TYPE_MAP = new HashMap<EventType, Class>();
            TYPE_MAP.put(EventType.ACTIONBAR, AseActionBarEvent.class);
            TYPE_MAP.put(EventType.ACTIONBARTAB, AseActionBarTabEvent.class);
            TYPE_MAP.put(EventType.CHECKBOX, AseCheckBoxEvent.class);
            TYPE_MAP.put(EventType.CLICK, AseClickEvent.class);
            TYPE_MAP.put(EventType.ITEMCLICK, AseItemClickEvent.class);
            TYPE_MAP.put(EventType.NAVIGATEUP, AseNavigateUpEvent.class);
        }
        return TYPE_MAP;
    }
    
    @SuppressWarnings("rawtypes")
    public static Class getEventClass(String eventTypeStr) {
        EventType type = EventType.valueOf(eventTypeStr);
        return getTypeMap().get(type);
    }
    
    public boolean isFirable() {
        AppRunTimeData appData = AppRunTimeData.getInstance();
        if(appData.getActivityRootView() == null)
            return false;
        
        boolean fragmentOk = false;
        if(fragmentName != null) {
            fragmentOk = false;
            List<Fragment> fragments = appData.getFragments();
            for(Fragment f: fragments) { 
                // If the fragment is active and is visible: This means it: (1) has been added, (2) has its view attached to the window, and (3) is not hidden.
                if(f.getClass().getName().equalsIgnoreCase(fragmentName) && f.isVisible())
                    fragmentOk = true;
                    break;
            }      
        }
        return (fragmentName == null) || fragmentOk;
    }

    abstract public void injectEvent();


}
