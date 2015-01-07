package ase;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings( {"unchecked", "rawtypes"} )
public abstract class AseEvent {

    private static Map<EventType, Class> TYPE_MAP;
    
    public enum EventType {
        CLICK, CHECKBOX, ITEMCLICK, ACTIONBAR, ACTIONBARTAB, NAVIGATEUP
    }

    public final EventType type;
    public final int viewId;

    protected AseEvent(EventType type, int viewId) {
        this.type = type;
        this.viewId = viewId;
    }

    abstract public boolean isFirable();

    abstract public void injectEvent();
    
    
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

    public static Class getEventClass(String eventTypeStr) {
        EventType type = EventType.valueOf(eventTypeStr);
        return getTypeMap().get(type);
    }
}
