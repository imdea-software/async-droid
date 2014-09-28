package ase;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import ase.scheduler.NopScheduler;
import ase.scheduler.RepeatingScheduler;
import ase.scheduler.Scheduler;

/*
 *  static interface class between the scheduler and the app under test
 */
public class AseTestBridge {

    private static Scheduler scheduler;
     
    /*
     *  called by UI thread with the application context
     */
    public static void initiateScheduler(Activity act) 
    {
        setTestParameters(act);
        runSchedulerThread();
    }
    /*
     *  set the number of delays and inputs
     */
    private static void setTestParameters(Activity act){
        int numDelays=0, numInputs=0;
        String mode = null;
        Intent intent = act.getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            mode = bundle.getString("mode");
            numDelays = bundle.getInt("numDelays");
            numInputs = bundle.getInt("numInputs");
        }   
        Log.i("MyScheduler", String.format("Parameters: numDelays: %d numInputs: %d mode: %s", numDelays, numInputs, mode));
        if(mode != null && mode.equalsIgnoreCase("record")){
            scheduler = new NopScheduler(act);

        }else{    
            scheduler = new RepeatingScheduler(numDelays, numInputs, act.getApplicationContext(), act.getWindow().getDecorView().getRootView()); 
        }
    }
    
    public static void runSchedulerThread(){
        scheduler.runScheduler();
    }

    /*
     *  application thread waits for its signal to start/resume
     */
    public static void waitMyTurn() {    
        scheduler.waitMyTurn();
    }
    
    /*
     *  application thread yields
     */
    public static void yield() {
        scheduler.yield();
    }
        
    /*
     *  application thread notify scheduler when they are completed
     */
    public static void notifyScheduler() {            
        scheduler.notifyScheduler();
    }
    
    /*
     *  application thread enters in a monitor
     */
    public void enterMonitor(){
        scheduler.enterMonitor();
    }
    
    /*
     *  application thread exits a monitor
     */
    public void exitMonitor(){
        scheduler.exitMonitor();
    }
    
}




