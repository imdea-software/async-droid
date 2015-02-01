package ase.recorder;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import ase.event.AseCheckBoxEvent;
import ase.event.AseEvent;
import ase.util.IOFactory;
import ase.util.Recorder;
import ase.util.ReflectionUtils;

public class InstrumentedCheckBoxClickListener implements View.OnClickListener {

    private View.OnClickListener ownListener;
    private Recorder recorder;
    private int pos;
    private int id;
    private int parentId;

    public InstrumentedCheckBoxClickListener(CheckBox view, ViewGroup parent, int pos, Context context) {
        ownListener = ReflectionUtils.getOnClickListener(view);
        //ownListener = new InstrumentedOnCheckedChangeListener(view, pos, getContext());
        recorder = IOFactory.getRecorder(context);
        this.pos = pos;
        this.id = view.getId(); // get the id of the containing view
        this.parentId = parent.getId();
    }

    @Override
    public void onClick(View v) {
        AseEvent event = new AseCheckBoxEvent(id, parentId, pos);
        recorder.record(event);

        if (ownListener != null)
            //ownListener.onCheckedChanged((CompoundButton)v, ((CompoundButton)v).isChecked());
            ownListener.onClick(v);
    }
}