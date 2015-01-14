package ase;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import ase.scheduler.RecordingMode;
import ase.scheduler.NopMode;
import ase.scheduler.RepeatingMode;
import ase.scheduler.ExecutionMode;
import ase.util.IOFactory;

/*
 *  static interface class between the scheduler and the app under test
 */
public class AseTestBridge {

    private static ExecutionMode executionMode;
    private static AppRunTimeData appData;
    private static boolean initiated = false;

    /**
     * called by UI thread in onCreate method of Activity or Application
     * with the application/activity instance as parameter
     */
    public static void initiateTesting(Context context) {
        if (!initiated) {
            initiated = true;
            appData = new AppRunTimeData(context);
            setTestParameters(context);
        }
        if(context instanceof Activity)
            appData.setCurrentAct((Activity)(context));
    }

    /**
     * Sets the execution mode and bound parameter (number of delays)
     * Also sets the application context 
     * to be used to relaunch mainActivity after each test case
     */
    private static void setTestParameters(Context context) {
        Parameters parameters = IOFactory.getParameters(context);
        Log.i("AsyncDroid", "Running in " + parameters.getMode() + " mode...");
        switch (parameters.getSchedulerMode()) {
            case RECORD:
                executionMode = new RecordingMode(context);
                break;
            case REPEAT:
                Log.i("AsyncDroid", "Number of delays: " + parameters.getNumDelays());
                executionMode = new RepeatingMode(parameters.getNumDelays(), context);
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
            //appData.setViewViewerContext(act.getApplicationContext());
            appData.setActivityRootView(v);
            appData.traverseViewIds(v.getRootView(), null);
        } else if (executionMode.getExecutionModeType() == ExecutionModeType.REPEAT) {
            appData.setActivityRootView(v);
        }
    }

    /*
     *  This method is called in onViewCreated of a Fragment before returning the rootView
     *  Traverses inflated view hierarchy and sets the currently loaded fragment name
     */
    public static void setFragmentViewTraverser(View rootView, Object fragmentThisLocal) {
        if (executionMode.getExecutionModeType() == ExecutionModeType.RECORD) {
            appData.traverseViewIds(rootView, (Fragment)fragmentThisLocal);
        } else if (executionMode.getExecutionModeType() == ExecutionModeType.REPEAT) {
            String fragmentClassName = fragmentThisLocal.getClass().getName();
            Log.v("View","Fragment view created: " + fragmentClassName);
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
    
    public static void launchMainActivity() {
        
        Intent i = appData.getAppContext().getPackageManager().getLaunchIntentForPackage(appData.getPackageName());
        //clear the entire stack, except for the activity being launched
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        appData.getAppContext().startActivity(i);
    }

    public static void finishCurrentActivity() {
        Log.i("AsyncDroid", "Finishing activity.");
        appData.getCurrentAct().finish();
    }

    // to be used for replay
    public static void setActionBarMenu(Menu menu) {
        // Need the menu reference only in replay mode
        if(!(executionMode instanceof RepeatingMode))
            return;

        if(menu== null)
            Log.i("Repeater", "Menu is null");
        appData.setActionBarMenu(menu);
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
             event = new AseNavigateUpEvent(item.getItemId(), appData.getCurrentAct().getComponentName().flattenToString());
        }
        IOFactory.getRecorder(appData.getAppContext()).record(event);
    }
    
    public static ExecutionModeType getExecutionMode() {
        return executionMode.getExecutionModeType();
    }
    
    public static AppRunTimeData getAppData() {
        return appData;
    }
    
    public static void executeFragmentTransactions() {
        FragmentManager fm = appData.getCurrentAct().getFragmentManager();
        fm.executePendingTransactions();
    }
}
