package ase.scheduler;

import android.content.Context;
import android.view.View;

public class RepeatingScheduler implements Scheduler {
    private static RepeatingSchedulerRunnable sch;

    public RepeatingScheduler(int delays, Context context, View view) {
        sch = new RepeatingSchedulerRunnable(delays, context); 
    }

    @Override
    public void runScheduler() {
        Thread t = new Thread(sch);
        t.setName("SchedulerThread");
        t.start();
        sch.wakeScheduler();
    }

    public void waitMyTurn() {
        Thread current = Thread.currentThread();
        sch.captureThread(current); // add thread to the scheduling list
        sch.waitMyTurn(current.getId());
    }

    public void yield() {
        sch.yield(Thread.currentThread().getId());
    }

    public void notifyScheduler() {
        sch.notifyScheduler();
    }

    public void enterMonitor() {
        sch.enterMonitor();
    }

    public void exitMonitor() {
        sch.exitMonitor();
    }
}
