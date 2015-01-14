package ase;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
    private static boolean initiated = false;

    /**
     * called by UI thread in onCreate method of Activity or Application
     * with the application/activity instance as parameter
     */
    public static void initiateTesting(Context context) {
        if (!initiated) {
            initiated = true;
            AppRunTimeData.createInstance(context);
            setTestParameters(context);
        }
        if(context instanceof Activity)
            AppRunTimeData.getInstance().setCurrentAct((Activity)(context));
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

    public static ExecutionModeType getExecutionMode() {
        return executionMode.getExecutionModeType();
    }
    
    public static void setActivityViewTraverser(Activity act) {
        View v = act.getWindow().getDecorView().getRootView();
        if (executionMode.getExecutionModeType() == ExecutionModeType.RECORD) {
            AppRunTimeData.getInstance().setActivityRootView(v);
            AppRunTimeData.getInstance().traverseViewIds(v.getRootView(), null);
        } else if (executionMode.getExecutionModeType() == ExecutionModeType.REPEAT) {
            AppRunTimeData.getInstance().setActivityRootView(v);
        }
    }

    /*
     *  This method is called in onViewCreated of a Fragment before returning the rootView
     *  Traverses inflated view hierarchy and sets the currently loaded fragment name
     */
    public static void setFragmentViewTraverser(View rootView, ViewGroup parent) {
        if (executionMode.getExecutionModeType() == ExecutionModeType.RECORD) {
            AppRunTimeData.getInstance().traverseViewIds(rootView, parent);
        } else if (executionMode.getExecutionModeType() == ExecutionModeType.REPEAT) {
            Log.v("View","Fragment view created with root: " + Integer.toHexString(rootView.getId()));
        }
    }
    
    public static void setAdapterViewItemTraverser(View view, ViewGroup parent, int pos) {
        if (executionMode.getExecutionModeType() == ExecutionModeType.RECORD) {
            AppRunTimeData.getInstance().traverseItemView(view, parent, pos);
        }
    }
        
    public static void setActionBarMenu(Menu menu) {
        // Need the menu reference only in replay mode
        if(!(executionMode instanceof RepeatingMode))
            return;

        if(menu== null)
            Log.i("Repeater", "Menu is null");
        AppRunTimeData.getInstance().setActionBarMenu(menu);
    }
    
    public static void setRecorderForActionBar(final MenuItem item) {
        // Recorder works only in record mode
        if(!(executionMode instanceof RecordingMode)) {
            return;
        }

        if(item == null) {
            Log.w("Repeater", "ActionBar menu item is null");
            return;
        }
        
        AseEvent event;
        if (item.getItemId() != android.R.id.home) {
             event = new AseActionBarEvent(item.getItemId());
        } else {
             event = new AseNavigateUpEvent(item.getItemId(), AppRunTimeData.getInstance().getCurrentAct().getComponentName().flattenToString());
        }
        IOFactory.getRecorder(AppRunTimeData.getInstance().getAppContext()).record(event);
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
}
