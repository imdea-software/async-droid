package ase;

import android.app.Activity;
import android.content.Context;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import ase.event.AseActionBarEvent;
import ase.event.AseActionBarTabEvent;
import ase.event.AseEvent;
import ase.event.AseItemClickEvent;
import ase.event.AseNavigateUpEvent;
import ase.scheduler.RecordingMode;
import ase.scheduler.NopMode;
import ase.scheduler.RepeatingMode;
import ase.scheduler.ExecutionMode;
import ase.util.IOFactory;
import ase.util.ReflectionUtils;
import ase.util.ViewUtils;

/**
 *  This class acts as an interface between Ase scheduler and the application under test
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
            IOFactory.initializeLogs();
            AppRunTimeData.createInstance(context);
            setTestParameters();
        }
        if(context instanceof Activity)
            AppRunTimeData.getInstance().setCurrentAct((Activity)(context));
    }

    /**
     * Sets the execution mode and bound parameter (number of delays)
     * Also sets the application context 
     * to be used to relaunch mainActivity after each test case
     */
    private static void setTestParameters() {
        Parameters parameters = IOFactory.getParameters();
        Log.i("AsyncDroid", "Running in " + parameters.getMode() + " mode...");
        switch (parameters.getSchedulerMode()) {
            case RECORD:
                executionMode = new RecordingMode();
                break;
            case REPEAT:
                Log.i("AsyncDroid", "Number of delays: " + parameters.getNumDelays());
                executionMode = new RepeatingMode(parameters.getNumDelays());
                executionMode.runScheduler();  
                break;
            case NOP:
                executionMode = new NopMode();
        }
        Log.i("AsyncDroid", "Scheduler initialized in mode: " + parameters.getMode());
    }

    /**
     * @return current execution mode: RECORD, REPEAT or NOP
     */
    public static ExecutionModeType getExecutionMode() {
        return executionMode.getExecutionModeType();
    }
    
    /** Methods that traverse a group of views and add instrumented listeners **/
    
    /**
     *  This method is called in onCreate of an Activity before returning the rootView
     *  Traverses inflated view hierarchy and sets the current activity reference in AppRunTimeData
     *  Instruments view handlers with recorders if in RECORD mode
     */
    public static void setActivityViewTraverser(Activity act) {
        View v = act.getWindow().getDecorView().getRootView();
        if (executionMode.getExecutionModeType() == ExecutionModeType.RECORD) {
            AppRunTimeData.getInstance().setActivityRootView(v);
            ViewUtils.traverseViewIds(v.getRootView());
        } else if (executionMode.getExecutionModeType() == ExecutionModeType.REPEAT) {
            AppRunTimeData.getInstance().setActivityRootView(v);
        }
    }

    /**
     *  This method is called in onViewCreated of a Fragment before returning the rootView
     *  Traverses inflated view hierarchy
     *  Instruments view handlers with recorders if in RECORD mode
     */
    public static void setFragmentViewTraverser(View rootView) {
        if (executionMode.getExecutionModeType() == ExecutionModeType.RECORD) {
            ViewUtils.traverseViewIds(rootView);
        } else if (executionMode.getExecutionModeType() == ExecutionModeType.REPEAT) {
            Log.v("View","Fragment view created with root: " + Integer.toHexString(rootView.getId()));
        }
    }
    
    /**
     *  This method is called in getView of an Adapter of an AdapterView 
     *  Traverses inflated view hierarchy for the view item 
     *  Instruments view handlers with recorders if in RECORD mode
     */
    public static void setAdapterItemViewTraverser(View view, ViewGroup parent, int pos) {
        if (executionMode.getExecutionModeType() == ExecutionModeType.RECORD) {
            ViewUtils.traverseItemView(view, parent, pos);
        }
        
    }
    
    /** Methods that add instrumented listeners in dynamically set event listener in app **/
    
    /**
     *  This method is called in OnItemClick listener of an item of an AdapterView 
     *  Instruments item click listeners with recorder if in RECORD mode
     */
    @SuppressWarnings("rawtypes")
    public static void setRecorderForItemClick(AdapterView adapter, int pos, long index) {
        if (executionMode.getExecutionModeType() == ExecutionModeType.RECORD) {
            // get the view and the path
            View view = adapter.getSelectedView();
            AseEvent event = new AseItemClickEvent(adapter.getId(), ViewUtils.logViewParents(view.getParent()), pos, index); /// update event type
            IOFactory.getRecorder().record(event);
        }     
    }
           
    /**
     * Set the reference for the action bar menu
     * To be used in the REPEAT mode
     * @param menu
     */
    public static void setActionBarMenu(Menu menu) {
        // Need the menu reference only in replay mode
        if(!(executionMode instanceof RepeatingMode))
            return;

        if(menu== null)
            Log.w("Repeater", "Menu is null");
        AppRunTimeData.getInstance().setActionBarMenu(menu);
    }
    
    /**
     * Set the event recorder for the action bar menu
     * To be used in the RECORD mode
     * @param item
     */
    public static void setRecorderForActionBar(final MenuItem item) {
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
        IOFactory.getRecorder().record(event);
    }
    
    /**
     * Set the event recorder for the action bar tabs
     * To be used in the RECORD mode
     * @param tab
     */
    public static void setRecorderForActionBarTab(Object tab) {
        if(AseTestBridge.getExecutionMode() == ExecutionModeType.RECORD) {
            // Call android.support.v7.app.ActionBar.Tab.getPosition() or android.app.ActionBar.Tab.getPosition()
            // Depending on the library app uses
            int pos = ReflectionUtils.getActionBarTabPosition(tab);
            if(pos == -1) {
                Log.e("Recorder", "Cannot set recorder for ActionBar.Tab");
                return;
            }
            
            AseEvent event = new AseActionBarTabEvent(0, pos); 
            ase.util.IOFactory.getRecorder().record(event);
            Log.v("Recorder", "Recorder is set for tab position: " + pos);
        } else {
            // execute transactions in the initial tab as well
            AppRunTimeData.getInstance().executeFragmentTransactions();
        }
    }
        
    /**
     * @return true if a thread will be managed by the dispatcher
     * If so, it will be instrumented for wait/notify synchronization with the dispatcher
     */
    private static boolean threadToAnalyze() {
        Thread current = Thread.currentThread();
        return (current.getId() == 1) || (current.getName().startsWith("AsyncTask")) 
                || (current instanceof HandlerThread) || (current.getName().equals("InputRepeater"));
    }
    
    /**
     * Called when an application thread waits for dispatching
     */
    public static void waitForDispatch() {
        if(threadToAnalyze())
            executionMode.waitForDispatch();
    }

    /**
     * Called when an application thread yields
     */
    public static void yield() {
        if(threadToAnalyze())
            executionMode.yield();
    }
    
    /**
     * Called when an application thread notifies the dispatcher 
     */
    public static void notifyDispatcher() {
        if(threadToAnalyze())
            executionMode.notifyDispatcher();
    }

    /**
     * Called when an application thread enters in a monitor
     */
    public static void enterMonitor() {
        if(threadToAnalyze())
            executionMode.enterMonitor();
    }

    /**
     * Called when an application thread exits a monitor
     */
    public static void exitMonitor() {
        if(threadToAnalyze())
            executionMode.exitMonitor();
    }
}
