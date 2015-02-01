package ase.scheduler;

import android.content.Context;
import ase.ExecutionModeType;
import ase.util.IOFactory;
import ase.util.Recorder;

public class RecordingMode implements ExecutionMode {

    public RecordingMode(Context context) {
        Recorder recorder = IOFactory.getRecorder(context);
        recorder.clear();
    }

    @Override
    public void waitForDispatch() {
        // TODO Auto-generated method stub
    }

    @Override
    public void yield() {
        // TODO Auto-generated method stub
    }

    @Override
    public void notifyDispatcher() {
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
    public ExecutionModeType getExecutionModeType() {
        return ExecutionModeType.RECORD;
    }

    @Override
    public void runScheduler() {
        // TODO Auto-generated method stub

    }
}
