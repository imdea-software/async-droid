package ase;

public abstract class AseEvent {

    public enum EventType {
        CLICK, ITEMCLICK
    }

    public final EventType type;
    public final int viewId;

    protected AseEvent(EventType type, int viewId) {
        this.type = type;
        this.viewId = viewId;
    }
}
