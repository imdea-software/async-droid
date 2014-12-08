package ase;

/**
 * Created by burcuozkan on 05/12/14.
 */
public class AseItemClickEvent extends AseEvent {

    public final int itemPos;
    public final long itemId;

    public AseItemClickEvent(int viewId, int itemPos, long id) {
        super(EventType.ITEMCLICK, viewId);
        this.itemPos = itemPos;
        this.itemId = id;
    }

    @Override
    public String toString() {
        return String.format("%s %d %d %d", type.name(), viewId, itemPos, itemId);
    }
}
