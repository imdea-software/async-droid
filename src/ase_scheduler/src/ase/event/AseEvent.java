package ase.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.util.Log;
import android.view.View;
import ase.AppRunTimeData;
import ase.util.ViewUtils;

public abstract class AseEvent {

    @SuppressWarnings("rawtypes")
    private static Map<EventType, Class> TYPE_MAP;
    
    public enum EventType {
        CLICK, CHECKBOX, ITEMCLICK, ACTIONBAR, ACTIONBARTAB, NAVIGATEUP, BACK
    }
    public final EventType type;
    public final int viewId;
    // ids of the views on the full path to the event view 
    public final List<Integer> path;
    
    /*
     * If the view is inflated inside a fragment, keep the name of that fragment
     * An event is firable only if that fragment is visible 
     * (A view with the same id may exist in more than one fragments)
     */
    public final String fragmentName;

    protected AseEvent(EventType type, int viewId, List<Integer> path) {
        this.type = type;
        this.viewId = viewId;
        this.path = path;
        
        if(type == EventType.ACTIONBAR || type == EventType.ACTIONBARTAB)
            fragmentName = null;
        else
            fragmentName = AppRunTimeData.getInstance().getFragmentNameByViewId(viewId);
    }
   
    protected AseEvent(EventType type, int viewId, List<Integer> path, int parentId) {
        this.type = type;
        this.viewId = viewId;
        this.path = path;
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
        // check root view
        AppRunTimeData appData = AppRunTimeData.getInstance();
        if(appData.getActivityRootView() == null)
            return false;

        // check if the view id is loaded
        View curView = AppRunTimeData.getInstance().getActivityRootView().findViewById(viewId);
        if(curView == null)
            return false;
            
        // check if the full path of the recorded path match with current view path
        if(path.size() > 0) {
            List<Integer> curPath = ViewUtils.logViewParents(curView.getParent());
            if(path.size() != curPath.size()) 
                return false;
            
            for(int i=0; i<path.size(); i++) {
                if(!path.get(i).equals(curPath.get(i))) 
                    return false;
            }
        }
        
        // check the fragment
        String s = appData.getFragmentNameByViewId(viewId);
        if(fragmentName != null)
            Log.i("Event's", fragmentName);
        if(s != null)
            Log.i("Current", s);
        return (fragmentName == null) || fragmentName.equals(s);
    }

    abstract public void injectEvent();


}
