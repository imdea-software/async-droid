package ase.scheduler;

public class NopScheduler implements Scheduler {
    PendingThreads threads = new PendingThreads();

    public NopScheduler() {

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
