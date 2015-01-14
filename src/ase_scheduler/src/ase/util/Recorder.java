package ase.util;

import ase.event.AseEvent;

public interface Recorder {
    void record(AseEvent event);

    void clear();
}
