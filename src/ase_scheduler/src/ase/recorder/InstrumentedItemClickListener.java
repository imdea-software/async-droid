package ase.recorder;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import ase.AseItemClickEvent;
import ase.util.IOFactory;
import ase.util.Recorder;
import ase.util.ReflectionUtils;

/**
 * Created by burcuozkan on 05/12/14.
 */
public class InstrumentedItemClickListener implements AdapterView.OnItemClickListener {
    private AdapterView.OnItemClickListener ownListener;
    private Recorder recorder;

    @SuppressWarnings("rawtypes")
    public InstrumentedItemClickListener(AdapterView view, Context context) {
        ownListener = ReflectionUtils.getOnItemClickListener(view);
        recorder = IOFactory.getRecorder(context);
    }

    @Override
    public void onItemClick(AdapterView adapterView, View view, int i, long l) {

        AseItemClickEvent event = new AseItemClickEvent(adapterView.getId(), i, l);
        recorder.record(event);

        Log.i("Recorder", "Clicked position: " + i + " Long Id: " + l);
        if (ownListener != null) {
            ownListener.onItemClick(adapterView, view, i, l);
        }
    }
}
