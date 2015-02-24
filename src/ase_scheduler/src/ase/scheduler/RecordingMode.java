package ase.scheduler;

import ase.ExecutionModeType;
import ase.util.IOFactory;
import ase.util.Recorder;

public class RecordingMode implements ExecutionMode {

    public RecordingMode() {
        Recorder recorder = IOFactory.getRecorder();
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
