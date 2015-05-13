package ase.recorder;

import java.util.List;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import ase.event.AseItemClickEvent;
import ase.util.IOFactory;
import ase.util.Recorder;
import ase.util.ViewUtils;

/**
 * Created by burcuozkan on 05/12/14.
 */
public class InstrumentedItemClickListener implements AdapterView.OnItemClickListener {
    private AdapterView.OnItemClickListener ownListener;
    private Recorder recorder;

    @SuppressWarnings("rawtypes")
    public InstrumentedItemClickListener(AdapterView view) {
        ownListener = view.getOnItemClickListener();
        recorder = IOFactory.getRecorder();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void onItemClick(AdapterView adapterView, View view, int pos, long id) {
        // save path to the adapterview - AseEvent checks it up to the viewId of the adapter view
        List<Integer> path = ViewUtils.logViewParents(adapterView.getParent());
        Log.i("Path", path.toString());
        Log.e("Path", "Heyo  " + path.toString());
        AseItemClickEvent event = new AseItemClickEvent(adapterView.getId(), path, pos, id);
        recorder.record(event);
        
        Log.i("Recorder", "Recorded item click at position: " + pos + " Long Id: " + id);
        
        if (ownListener != null) {
            ownListener.onItemClick(adapterView, view, pos, id);
        }
    }
}
