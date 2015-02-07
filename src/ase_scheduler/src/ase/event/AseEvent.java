package ase.event;

import java.util.HashMap;
import java.util.Map;
import android.util.Log;
import ase.AppRunTimeData;

public abstract class AseEvent {

    @SuppressWarnings("rawtypes")
    private static Map<EventType, Class> TYPE_MAP;
    
    public enum EventType {
        CLICK, CHECKBOX, ITEMCLICK, ACTIONBAR, ACTIONBARTAB, NAVIGATEUP, BACK
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
        
        if(type == EventType.ACTIONBAR || type == EventType.ACTIONBARTAB)
            fragmentName = null;
        else
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
            TYPE_MAP.put(EventType.BACK, AseBackButtonEvent.class);
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

        String s = appData.getFragmentNameByViewId(viewId);
        //if(s != null) Log.i("Firable", "Current: " + s);
        if(fragmentName != null)
            Log.i("Event's", fragmentName);
        if(s != null)
            Log.i("Current", s);
        return (fragmentName == null) || fragmentName.equals(s);
    }

    abstract public void injectEvent();


}
