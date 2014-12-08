package ase;

/**
 * Created by burcuozkan on 06/12/14.
 */
public class AseClickEvent extends AseEvent {

    public AseClickEvent(int viewId) {
        super(EventType.CLICK, viewId);
    }

    @Override
    public String toString() {
        return String.format("%s %d", type.name(), viewId);
    }
}
