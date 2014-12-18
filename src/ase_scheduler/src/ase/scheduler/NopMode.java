package ase.scheduler;

import ase.ExecutionModeType;

public class NopMode implements ExecutionMode {

    public NopMode() {
    }

    @Override
    public ExecutionModeType getExecutionModeType() {
        return ExecutionModeType.NOP;
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
    public void runScheduler() {
        // TODO Auto-generated method stub

    }
}
