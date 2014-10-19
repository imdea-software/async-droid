package ase.scheduler;

import android.app.Activity;
import ase.util.IOFactory;
import ase.util.Recorder;

public class NopScheduler implements Scheduler {
     
    public NopScheduler(Activity act){
        Recorder recorder = IOFactory.getRecorder(act.getApplicationContext());
        recorder.clear();
    }

    @Override
    public void waitMyTurn() {
        // TODO Auto-generated method stub
    }
    
    @Override
    public void yield() {
        // TODO Auto-generated method stub    
    }
    
    @Override
    public void notifyScheduler() {
        // TODO Auto-generated method stub        
    }

    @Override
    public void enterMonitor() {
        // TODO Auto-generated method stub
    }

    @Override
    public void exitMonitor() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void runScheduler() {
        // TODO Auto-generated method stub
        
    }
    
}



