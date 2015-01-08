package ase;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import ase.recorder.ViewTraverser;
import ase.scheduler.RecordingMode;
import ase.scheduler.NopMode;
import ase.scheduler.RepeatingMode;
import ase.scheduler.ExecutionMode;
import ase.scheduler.ExecutionData;
import ase.util.IOFactory;

/*
 *  static interface class between the scheduler and the app under test
 */
public class AseTestBridge {

    private static ExecutionMode executionMode;
    private static ExecutionData executionData;
    private static boolean initiated = false;
    
    // application appContext to be used in utils and the scheduler
    private static Context appContext;
    private static Activity currentAct;
    private static Menu actionBarMenu;

    /**
     * called by UI thread in onCreate method of Activity or Application
     * with the application/activity instance as parameter
     */
    public static void initiateTesting(Context context) {
        if (!initiated) {
            initiated = true;
            executionData = new ExecutionData();
            setTestParameters(context);
        }
        if(context instanceof Activity)
            currentAct = (Activity) context;
    }

    /**
     * Sets the execution mode and bound parameter (number of delays)
     * Also sets the application context 
     * to be used to relaunch mainActivity after each test case
     */
    private static void setTestParameters(Context context) {
        appContext = context.getApplicationContext();
        Parameters parameters = IOFactory.getParameters(appContext);

        Log.i("AsyncDroid", "Running in " + parameters.getMode() + " mode...");
        switch (parameters.getSchedulerMode()) {
            case RECORD:
                executionMode = new RecordingMode(context);
                break;
            case REPEAT:
                Log.i("AsyncDroid", "Number of delays: " + parameters.getNumDelays());
                executionMode = new RepeatingMode(parameters.getNumDelays(), appContext);
                executionMode.runScheduler();  
                break;
            case NOP:
                executionMode = new NopMode();
        }
        Log.i("AsyncDroid", "Scheduler initialized for mode " + parameters.getMode());
    }

    public static void setActivityViewTraverser(Activity act) {
        View v = act.getWindow().getDecorView().getRootView();
        if (executionMode.getExecutionModeType() == ExecutionModeType.RECORD) {
            ViewTraverser.setViewViewerContext(act.getApplicationContext());
            ViewTraverser.setRootView(v);
            ViewTraverser.traverseViewIds(v.getRootView());
        } else if (executionMode.getExecutionModeType() == ExecutionModeType.REPEAT) {
            ViewTraverser.setRootView(v);
        }
    }

    public static void setFragmentViewTraverser(View rootView) {
        if (executionMode.getExecutionModeType() == ExecutionModeType.RECORD) {
            ViewTraverser.traverseViewIds(rootView);
        }
    }
    
    /*
     * application thread waits for its signal to start/resume
     */
    public static void waitForDispatch() {
        executionMode.waitForDispatch();
    }

    /*
     * application thread yields
     */
    public static void yield() {
        executionMode.yield();
    }
    

    /*
     * application thread notify scheduler when they are completed
     */
    public static void notifyDispatcher() {
        executionMode.notifyDispatcher();
    }

    /*
     * application thread enters in a monitor
     */
    public static void enterMonitor() {
        executionMode.enterMonitor();
    }

    /*
     * application thread exits a monitor
     */
    public static void exitMonitor() {
        executionMode.exitMonitor();
    }
    
    public static void incNumUIBlocks() {
        Log.v("AsyncDroid", "Incremented numUIBlocks by: " + Thread.currentThread().getName());
        executionData.incNumUIBlocks();
    }
    
    public static void decNumUIBlocks() {
        Log.v("AsyncDroid", "Decremented numUIBlocks by: " + Thread.currentThread().getName());
        executionData.decNumUIBlocks();
    }
    
    public static int getNumUIBlocks() {
        return executionData.getNumUIBlocks();
    }
    
    public static void launchMainActivity() {
        String packageName = appContext.getPackageName();
        Intent i = appContext.getPackageManager().getLaunchIntentForPackage(packageName);
        //clear the entire stack, except for the activity being launched
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        appContext.startActivity(i);
    }

    public static void finishCurrentActivity() {
        Log.i("AsyncDroid", "Finishing activity.");
        currentAct.finish();
    }

    // to be used for replay
    public static void setActionBarMenu(Menu menu) {
        // Need the menu reference only in replay mode
        if(!(executionMode instanceof RepeatingMode))
            return;

        if(menu== null)
            Log.i("Repeater", "Menu is null");
        actionBarMenu = menu;
    }

    public static void setRecorderForActionBar(final MenuItem item) {
        // Recorder works only in record mode
        if(!(executionMode instanceof RecordingMode)) {
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

    public static Context getApplicationContext() {
        return appContext;
    }
    
    public static Activity getCurrentActivity() {
        return currentAct;
    }
    
    public static Menu getActionBarMenu() {
        return actionBarMenu;
    }
}
