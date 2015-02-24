package ase.recorder;

import java.util.List;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import ase.event.AseCheckBoxEvent;
import ase.event.AseEvent;
import ase.util.IOFactory;
import ase.util.Recorder;
import ase.util.ReflectionUtils;
import ase.util.ViewUtils;

public class InstrumentedCheckBoxClickListener implements View.OnClickListener {

    private View.OnClickListener ownListener;
    private Recorder recorder;
    private int pos;
    private int id;
    private int parentId; // id of the AdapterView that contains the checkbox
    //private List<Integer> path;

    public InstrumentedCheckBoxClickListener(CheckBox view, ViewGroup parent, int pos) {
        ownListener = ReflectionUtils.getOnClickListener(view);
        recorder = IOFactory.getRecorder();
        this.pos = pos;
        this.id = view.getId(); // get the id of the containing view
        this.parentId = parent.getId();
    }

    @Override
    public void onClick(View v) {
        List<Integer> path = ViewUtils.logViewParents(v.getParent());
        Log.i("Path", path.toString());
        
        //AdapterView parent = (AdapterView) AppRunTimeData.getInstance().getActivityRootView().findViewById(parentId);
        //parent.getChildAt(pos).
        
        
        AseEvent event = new AseCheckBoxEvent(id, path, parentId, pos);
        recorder.record(event);
        
        if (ownListener != null)
            ownListener.onClick(v);
    }
}