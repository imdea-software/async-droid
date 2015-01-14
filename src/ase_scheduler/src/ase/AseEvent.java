package ase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Fragment;
import ase.util.ReflectionUtils;

@SuppressWarnings( {"unchecked", "rawtypes"} )
public abstract class AseEvent {

    private static Map<EventType, Class> TYPE_MAP;
    
    public enum EventType {
        CLICK, CHECKBOX, ITEMCLICK, ACTIONBAR, ACTIONBARTAB, NAVIGATEUP
    }
    public final EventType type;
    public final int viewId;
    public final String fragmentName;

    protected AseEvent(EventType type, int viewId) {
        this.type = type;
        this.viewId = viewId;
        this.fragmentName = null;
    }
    
    protected AseEvent(EventType type, int viewId, String fragmentName) {
        this.type = type;
        this.viewId = viewId;
        this.fragmentName = fragmentName;
    }
    
    protected AseEvent(EventType type, int viewId, int parentId, String fragmentName) {
        this.type = type;
        this.viewId = viewId;
        this.fragmentName = fragmentName;
    }
    
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
    
    // TODO Use support library getSupportFragmentManager().getFragments();
    // Find the fragment with the same name and check visibility
    public boolean isFirable() {
        boolean fragmentOk = false;
        
        if(fragmentName != null) {
            fragmentOk = false;
            List<Fragment> fragments = ReflectionUtils.getFragments(AseTestBridge.getAppData().getCurrentAct());
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

    public static Class getEventClass(String eventTypeStr) {
        EventType type = EventType.valueOf(eventTypeStr);
        return getTypeMap().get(type);
    }
}
