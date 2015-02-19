package ase.recorder;

import java.util.List;

import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import ase.event.AseClickEvent;
import ase.event.AseEvent;
import ase.util.IOFactory;
import ase.util.Recorder;
import ase.util.ReflectionUtils;
import ase.util.ViewUtils;

public class InstrumentedListener implements OnClickListener {

    private OnClickListener ownListener;
    private Recorder recorder;

    public InstrumentedListener(View view) {
        ownListener = ReflectionUtils.getOnClickListener(view);
        recorder = IOFactory.getRecorder();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == -1) {
            Log.i("ViewLogger", "Clicked event has id -1 and not recorded.");
            return;
        }
        
        List<Integer> path = ViewUtils.logViewParents(v.getParent());
        Log.i("Path", path.toString());
        
        AseEvent event = new AseClickEvent(id, path);
        recorder.record(event);


        if (ownListener != null)
            ownListener.onClick(v);
    }
}