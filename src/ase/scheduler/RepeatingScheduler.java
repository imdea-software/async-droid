package ase.scheduler;

import android.content.Context;
import android.view.View;

public class RepeatingScheduler implements Scheduler {
    private static SchedulerRunnable sch;

    public RepeatingScheduler(int delays, int inputs, Context context, View view) {
        sch = new SchedulerRunnable(delays, inputs, context, view); // to perform scheduling 
    }

    @Override
    public void runScheduler() {
        Thread t = new Thread(sch);
        t.setName("MySchedulerThread");
        t.start();
        sch.wakeScheduler();
    }
    
    public void waitMyTurn() {    
        sch.sendThreadInfo(); // add thread to the scheduling list
        sch.waitMyTurn(Thread.currentThread().getId());
    }
    
    public void yield() {
        sch.yield(Thread.currentThread().getId());
    }
        
    public void notifyScheduler() {            
        sch.notifyScheduler();
    }
    
    public void enterMonitor(){
        sch.enterMonitor();
    }
    
    public void exitMonitor(){
        sch.exitMonitor();
    }
}



