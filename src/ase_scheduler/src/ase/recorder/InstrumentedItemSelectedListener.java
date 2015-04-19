package ase.recorder;

import java.util.List;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import ase.event.AseItemSelectedEvent;
import ase.util.IOFactory;
import ase.util.Recorder;
import ase.util.ViewUtils;

/**
 * Created by burcuozkan on 09/12/14.
 */
public class InstrumentedItemSelectedListener implements AdapterView.OnItemSelectedListener {
    private AdapterView.OnItemSelectedListener ownListener;
    private Recorder recorder;

    @SuppressWarnings("rawtypes")
    public InstrumentedItemSelectedListener(AdapterView parent, View view) {
        ownListener = parent.getOnItemSelectedListener();
        recorder = IOFactory.getRecorder();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void onItemSelected(AdapterView adapterView, View view, int pos, long id) {
        List<Integer> path = ViewUtils.logViewParents(view.getParent());
        Log.i("Path", path.toString());
                
        AseItemSelectedEvent event = new AseItemSelectedEvent(adapterView.getId(), path, pos, id);
        recorder.record(event);

        Log.i("Recorder", "Selected position: " + pos + " Long Id: " + id);
        if (ownListener != null) {
            ownListener.onItemSelected(adapterView, view, pos, id);
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void onNothingSelected(AdapterView adapterView) {
        if (ownListener != null) {
            ownListener.onNothingSelected(adapterView);
        }
    }
}