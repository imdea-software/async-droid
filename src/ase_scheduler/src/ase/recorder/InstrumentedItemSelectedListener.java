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
 * Created by burcuozkan on 09/12/14.
 */
public class InstrumentedItemSelectedListener implements AdapterView.OnItemSelectedListener {
    private AdapterView.OnItemSelectedListener ownListener;
    private Recorder recorder;

    public InstrumentedItemSelectedListener(AdapterView view, Context context) {
        ownListener = ReflectionUtils.getOnItemSelectedListener(view);
        recorder = IOFactory.getRecorder(context);
    }

    @Override
    public void onItemSelected(AdapterView adapterView, View view, int i, long l) {

        AseItemClickEvent event = new AseItemClickEvent(adapterView.getId(), i, l);
        recorder.record(event);

        Log.i("Recorder", "Selected position: " + i + " Long Id: " + l);
        if (ownListener != null) {
            ownListener.onItemSelected(adapterView, view, i, l);
        }
    }


    @Override
    public void onNothingSelected(AdapterView adapterView) {
        if (ownListener != null) {
            ownListener.onNothingSelected(adapterView);
        }
    }
}