package ase;

import android.app.Activity;
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

    /*
     * called by UI thread with the application context
     */
    public static void initiateScheduler(Activity act) {
        setTestParameters(act);

        runSchedulerThread();
    }

    /*
     * set the number of delays and inputs
     */
    private static void setTestParameters(Activity act) {
        int numDelays = 0, numInputs = 0;
        String smode = null;
        Intent intent = act.getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            smode = bundle.getString("mode");
            numDelays = Integer.parseInt(bundle.getString("numDelays"));
            numInputs = Integer.parseInt(bundle.getString("numInputs"));
        }
        Log.i("MyScheduler", String.format(
                "Parameters: numDelays: %d numInputs: %d mode: %s", numDelays,
                numInputs, smode));
        if (smode != null && smode.equalsIgnoreCase("record")) {
            mode = SchedulerMode.RECORD;
            Log.i("MyScheduler", "Running in record mode");
            scheduler = new RecordingScheduler(act);
        } else if (smode != null
                && (smode.equalsIgnoreCase("repeat") || smode
                        .equalsIgnoreCase("replay"))) {
            mode = SchedulerMode.REPEAT;
            Log.i("MyScheduler", "Running in repeat mode");
            scheduler = new RepeatingScheduler(numDelays, numInputs,
                    act.getApplicationContext(), act.getWindow().getDecorView()
                            .getRootView());
        } else {
            mode = SchedulerMode.NOP;
            Log.i("MyScheduler", "No Scheduler is used");
            scheduler = new NopScheduler();
        }
    }

    public static void runSchedulerThread() {
        scheduler.runScheduler();
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

}
