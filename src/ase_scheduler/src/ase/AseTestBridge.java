package ase;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import ase.recorder.ViewTraverser;
import ase.scheduler.RecordingScheduler;
import ase.scheduler.NopScheduler;
import ase.scheduler.RepeatingScheduler;
import ase.scheduler.Scheduler;
import ase.scheduler.SchedulerData;
import ase.util.IOFactory;

/*
 *  static interface class between the scheduler and the app under test
 */
public class AseTestBridge {

    private static Scheduler scheduler;
    private static SchedulerData schedulerData;
    private static boolean initiated = false;
    
    // application appContext to be used in utils and the scheduler
    private static Context appContext;
    public static Activity currentAct;  /////////////////
    public static Menu actionBarMenu;

    /*
     * called by UI thread in onCreate method of Activity or Application
     * with the application/activity instance as parameter
     */
    public static void initiateScheduler(Context context) {
        if (!initiated) {
            initiated = true;
            schedulerData = new SchedulerData();
            setTestParameters(context);
            scheduler.runScheduler();  
        }
        if(context instanceof Activity)
            currentAct = (Activity) context;
    }

    /**
     * Sets the scheduler mode and number of delays
     * Also sets the application context 
     * to be used to relaunch mainActivity after each test case
     */
    private static void setTestParameters(Context context) {
        appContext = context.getApplicationContext();
        Parameters parameters = IOFactory.getParameters(appContext);

        Log.i("MyScheduler", "Running in " + parameters.getMode() + " mode...");
        switch (parameters.getSchedulerMode()) {
            case RECORD:
                scheduler = new RecordingScheduler(context);
                break;
            case REPEAT:
                Log.i("MyScheduler", "Number of delays: " + parameters.getNumDelays());
                scheduler = new RepeatingScheduler(parameters.getNumDelays(), appContext);
                break;
            case NOP:
                scheduler = new NopScheduler();
        }
        Log.i("MyScheduler", "Scheduler initialized for mode " + parameters.getMode());
    }

    public static void setActivityViewTraverser(Activity act) {
        View v = act.getWindow().getDecorView().getRootView();
        if (scheduler.getSchedulerMode() == SchedulerMode.RECORD) {
            ViewTraverser.setViewViewerContext(act.getApplicationContext());
            ViewTraverser.setRootView(v);
            ViewTraverser.traverseViewIds(v.getRootView());
        } else if (scheduler.getSchedulerMode() == SchedulerMode.REPEAT) {
            ViewTraverser.setRootView(v);
        }
    }

    public static void setFragmentViewTraverser(View rootView) {
        if (scheduler.getSchedulerMode() == SchedulerMode.RECORD) {
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
        String packageName = appContext.getPackageName();
        Intent i = appContext.getPackageManager().getLaunchIntentForPackage(packageName);
        //clear the entire stack, except for the activity being launched
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        appContext.startActivity(i);
    }

    public static void finishCurrentActivity() {
        Log.i("MyScheduler", "Finishing activity.");
        currentAct.finish();
    }

    // to be used for replay
    public static void setActionBarMenu(Menu menu) {
        // Need the menu reference only in replay mode
        if(!(scheduler instanceof RepeatingScheduler))
            return;

        if(menu== null)
            Log.i("Repeater", "Menu is null");
        actionBarMenu = menu;
    }

    public static void setRecorderForActionBar(final MenuItem item) {
        // Recorder works only in record mode
        if(!(scheduler instanceof RecordingScheduler)) {
            Log.i("Recorder", "Not in record mode");
            return;
        }

        if(item == null) {
            Log.i("Repeater", "Item is null");
            return;
        }
        AseEvent event;
        if (item.getItemId() != android.R.id.home) {
             event = new AseActionBarEvent(item.getItemId());
        } else {
             event = new AseNavigateUpEvent(item.getItemId(), AseTestBridge.currentAct.getComponentName().flattenToString());
        }
        IOFactory.getRecorder(appContext).record(event);
    }

}
