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

/*
 *  static interface class between the scheduler and the app under test
 */
public class AseTestBridge {

    public enum SchedulerMode {
        NOP, RECORD, REPEAT
    };

    private static Scheduler scheduler;
    private static SchedulerMode mode;
    private static boolean initiated = false;
    
    // application context to be used in utils and the scheduler
    private static Context context;
    
    // UI blocks inserted by inputRepeater, onPublishProgress or onPostExecute
    // keeps track of UI thread status
    private static int numUIBlocks = 0;

    /*
     * called by UI thread with the application context
     */
    public static void initiateScheduler(Activity act) {
        if (!initiated) {
            setTestParameters(act);
            scheduler.runScheduler();
            initiated = true;
        }
    }

    /*
     * set the number of delays and inputs
     */
    private static void setTestParameters(Activity act) {
        context = act.getApplicationContext(); // used to resume main activity
        
        int numDelays = 0;
        String smode = null;
        Intent intent = act.getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            smode = bundle.getString("mode");
            numDelays = Integer.parseInt(bundle.getString("numDelays"));
        }
        Log.i("MyScheduler", String.format("Parameters: mode: %s numDelays: %d", smode, numDelays));
        if (smode != null && smode.equalsIgnoreCase("record")) {
            mode = SchedulerMode.RECORD;
            Log.i("MyScheduler", "Running in record mode");
            scheduler = new RecordingScheduler(act);
        } else if (smode != null && (smode.equalsIgnoreCase("repeat") || smode.equalsIgnoreCase("replay"))) {
            mode = SchedulerMode.REPEAT;
            Log.i("MyScheduler", "Running in repeat mode");
            scheduler = new RepeatingScheduler(numDelays, act.getApplicationContext(), act.getWindow().getDecorView().getRootView());
        } else {
            mode = SchedulerMode.NOP;
            Log.i("MyScheduler", "No Scheduler is used");
            scheduler = new NopScheduler();
        }
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
            ViewTraverser.setRootView(rootView);
            ViewTraverser.traverseViewIds(rootView);
        } else if (mode == SchedulerMode.REPEAT) {
            ViewTraverser.setRootView(rootView);
        }
    }

    /*
     * application thread sends its info to the scheduler
     * (necessary when it runs before added into scheduler's list)
     */
    public static void sendThreadInfo() {
        scheduler.sendThreadInfo();
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
    public void enterMonitor() {
        scheduler.enterMonitor();
    }

    /*
     * application thread exits a monitor
     */
    public void exitMonitor() {
        scheduler.exitMonitor();
    }
    
    public static synchronized void incNumUIBlocks() {
        numUIBlocks ++;
    }
    
    public static synchronized void decNumUIBlocks() {
        numUIBlocks --;
    }
    
    public static synchronized int getNumUIBlocks() {
        return numUIBlocks;
    }
    
    public static void resumeMainActivity(){
        String packageName = context.getPackageName();
        Intent i = context.getPackageManager().getLaunchIntentForPackage(packageName);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(i);
    }

}
