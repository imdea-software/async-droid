package ase.util;

import ase.AseEvent;

public interface Recorder {
    void record(AseEvent event);

    void clear();
}
