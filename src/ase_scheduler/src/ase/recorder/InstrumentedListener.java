package ase.recorder;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import ase.AseClickEvent;
import ase.AseEvent;
import ase.util.IOFactory;
import ase.util.Recorder;
import ase.util.ReflectionUtils;

public class InstrumentedListener implements OnClickListener {

    private OnClickListener ownListener;
    private Recorder recorder;

    public InstrumentedListener(View view, Context context) {
        ownListener = ReflectionUtils.getOnClickListener(view);
        recorder = IOFactory.getRecorder(context);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == -1) {
            Log.i("ViewLogger", "Clicked event has id -1 and not recorded.");
            return;
        }

        AseEvent event = new AseClickEvent(id);
        recorder.record(event);

        if (ownListener != null)
            ownListener.onClick(v);
    }
}