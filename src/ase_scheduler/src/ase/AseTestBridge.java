package ase;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import ase.recorder.ViewTraverser;
import ase.scheduler.RecordingScheduler;
import ase.scheduler.NopScheduler;
import ase.scheduler.RepeatingScheduler;
import ase.scheduler.Scheduler;
import ase.scheduler.SchedulerData;


/*
 *  static interface class between the scheduler and the app under test
 */
public class AseTestBridge {

    public enum SchedulerMode {
        NOP, RECORD, REPEAT
    };

    private static Scheduler scheduler;
    private static SchedulerData schedulerData;
    private static SchedulerMode mode;
    private static boolean initiated = false;
    
    // application context to be used in utils and the scheduler
    private static Context context;
   

    /*
     * called by UI thread with the application context
     */
    public static void initiateScheduler(Activity act) {
        if (!initiated) {
            initiated = true;
            schedulerData = new SchedulerData();
            setTestParameters(act);
            scheduler.runScheduler();  
        }
    }

    /**
     * Sets the scheduler mode and number of delays
     * Also sets the application context 
     * to be used to relaunch mainActivity after each test case
     */
    private static void setTestParameters(Activity act) {
        context = act.getApplicationContext(); 

        Intent intent = act.getIntent();
        Bundle bundle = intent.getExtras();
        
        if (intent.hasExtra("mode")) {
            String smode = bundle.getString("mode");
            
            if (smode.equalsIgnoreCase("record")) {
                mode = SchedulerMode.RECORD;
                Log.i("MyScheduler", "Running in record mode");
                scheduler = new RecordingScheduler(act);
                return;
            }
            if ((smode.equalsIgnoreCase("repeat") || smode.equalsIgnoreCase("replay"))) {
                mode = SchedulerMode.REPEAT;
                int numDelays = 0;
                if (intent.hasExtra("numDelays")) {
                    numDelays = Integer.parseInt(bundle.getString("numDelays"));
                    Log.i("MyScheduler", "Running in repeat mode with delay bound " + numDelays);
                } else {
                    Log.i("MyScheduler", "Running in repeat mode with delay bound 0 (default setting)");
                }
                scheduler = new RepeatingScheduler(numDelays, act.getApplicationContext());
                return;
            }
            Log.i("MyScheduler", "Scheduler mode cannot be identified.");
        }
         
        mode = SchedulerMode.NOP;
        Log.i("MyScheduler", "No Scheduler is used");
        scheduler = new NopScheduler();   
    }

    public static void setActivityViewTraverser(Activity act) {
        View v = act.getWindow().getDecorView().getRootView();
        if (mode == SchedulerMode.RECORD) {
            ViewTraverser.setViewViewerContext(act.getApplicationContext());
            ViewTraverser.setRootView(v);
            ViewTraverser.traverseViewIds(v.getRootView());
        } else if (mode == SchedulerMode.REPEAT) {
            ViewTraverser.setRootView(v);
        }
    }

    public static void setFragmentViewTraverser(View rootView) {
        if (mode == SchedulerMode.RECORD) {
            ViewTraverser.traverseViewIds(rootView);
        }
    }
    
    /*
     * application thread waits for its signal to start/resume
     */
    public static void waitMyTurn() {
        scheduler.waitMyTurn();
    }

    /*
     * application thread yields
     */
    public static void yield() {
        scheduler.yield();
    }
    

    /*
     * application thread notify scheduler when they are completed
     */
    public static void notifyScheduler() {
        scheduler.notifyScheduler();
    }

    /*
     * application thread enters in a monitor
     */
    public static void enterMonitor() {
        scheduler.enterMonitor();
    }

    /*
     * application thread exits a monitor
     */
    public static void exitMonitor() {
        scheduler.exitMonitor();
    }
    
    public static void incNumUIBlocks() {
        Log.v("MyScheduler", "Incremented numUIBlocks by: " + Thread.currentThread().getName());
        schedulerData.incNumUIBlocks();
    }
    
    public static void decNumUIBlocks() {
        Log.v("MyScheduler", "Decremented numUIBlocks by: " + Thread.currentThread().getName());
        schedulerData.decNumUIBlocks();
    }
    
    public static int getNumUIBlocks() {
        return schedulerData.getNumUIBlocks();
    }
    
    public static void launchMainActivity() {
        String packageName = context.getPackageName();
        Intent i = context.getPackageManager().getLaunchIntentForPackage(packageName);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(i);
    }

}
