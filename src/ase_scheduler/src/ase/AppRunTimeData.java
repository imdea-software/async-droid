package ase;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Spinner;
import ase.recorder.InstrumentedCheckBoxClickListener;
import ase.recorder.InstrumentedItemClickListener;
import ase.recorder.InstrumentedItemSelectedListener;
import ase.recorder.InstrumentedListener;
import ase.util.ReflectionUtils;

public class AppRunTimeData {

    private static AppRunTimeData INSTANCE;
    private String packageName;
    private Context appContext;
    private Activity currentActivity;
    private View activityRootView;
    private Menu actionBarMenu;
   
    public static AppRunTimeData createInstance(Context context) {
        if(INSTANCE == null)
            INSTANCE = new AppRunTimeData(context);
        return INSTANCE;
    }
    
    public static AppRunTimeData getInstance() {
        assert INSTANCE != null;
        return INSTANCE;
    }
    
    private AppRunTimeData(Context context) {
        appContext = context.getApplicationContext();
        packageName = appContext.getPackageName();
    }
    
    public Context getAppContext() {
        return appContext;
    }
    
    public String getPackageName() {
        return packageName;
    }
    
    public Activity getCurrentAct() {
        return currentActivity;
    }
    
    public void setCurrentAct(Activity currentAct) {
        this.currentActivity = currentAct;
    }
    
    public Menu getActionBarMenu() {
        return actionBarMenu;
    }
    
    public void setActionBarMenu(Menu actionBarMenu) {
        this.actionBarMenu = actionBarMenu;
    }
    
    public View getActivityRootView() {
        return activityRootView;
    }
    
    public void setActivityRootView(View view) {
        this.activityRootView = view;
        Log.v("ViewLogger", "Current activity view: " + view.toString());
    }
    
    public List<Object> getFragments() {
        List<Object> fragments = ReflectionUtils.getFragments(currentActivity);
        if(fragments == null)
            return new ArrayList<Object>();
        return fragments;
    }
    
    /*
     * Returns the name of the fragment in which the view with viewID is inflated
     */
    public String getFragmentNameByViewId(int viewId) {
        return ReflectionUtils.getFragmentByViewID(currentActivity, viewId);
    }
    
    public void launchMainActivity() {
        Intent i = appContext.getPackageManager().getLaunchIntentForPackage(packageName);
        //clear the entire stack, except for the activity being launched
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        appContext.startActivity(i);
    }

    public void finishCurrentActivity() {
        Log.i("AsyncDroid", "Finishing activity.");
        currentActivity.finish();
    }

    
    public void executeFragmentTransactions() {
        FragmentManager fm = currentActivity.getFragmentManager();
        fm.executePendingTransactions();
    }
 
    // TODO better structure view traversers
    
    /*
     * View Traversers instrument views with recording event handlers
     * Takes a given root view and sets proper event listeners for its children views
     * Called from: (1) Activity onCreate (2) Fragment onCreateView (3) AdapterView getView
     * If (3), i.e. the view is an item in an AdapterView, take its parent as well
     */
    public View traverseViewIds(View view, ViewGroup parent) {
        Log.v("ViewLogger", "traversing: " + view.getClass().getSimpleName() + ", id: " + view.getId() );
        if (view.getParent() != null && (view.getParent() instanceof ViewGroup)) {
            return traverseViewIds((View) view.getParent(), parent);
        } else {
            traverseChildViewIds(view, parent);
            return view;
        }
    }

    @SuppressWarnings("rawtypes")
    private void traverseChildViewIds(View view, ViewGroup parent) {

        if(view.getClass().getSimpleName().equals("ActionBarContainer")) {
            Log.i("ViewLogger", "ActionBarContainer Detail: " + view.toString() + " ID: " + view.getId());
            return;
        }

        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                View child = group.getChildAt(i);
                Log.v("ViewLogger", "traversed: " + child.getClass().getSimpleName() + " "
                                + Integer.toHexString(child.getId()));

                if(child instanceof AdapterView) {
                    if(child instanceof ListView) {
                        Log.i("ViewLogger", "List view: Id: " + child.getId() + " " + ((AdapterView) child).getCount());
                        // add onItemClickListener to the adapter
                        AdapterView.OnItemClickListener listener = new InstrumentedItemClickListener((AdapterView) child, appContext);
                        ((AdapterView) child).setOnItemClickListener(listener);
                    } else if (child instanceof Spinner) {
                        Log.i("ViewLogger", "Spinner view: Id: " + child.getId() + " " + ((AdapterView) child).getCount());
                        // add onItemClickListener to the adapter
                        AdapterView.OnItemSelectedListener listener = new InstrumentedItemSelectedListener((AdapterView) child, appContext);
                        ((AdapterView) child).setOnItemSelectedListener(listener);
                    } else {
                        Log.i("ViewLogger", "Cannot record grid view or gallery view");
                    }

                } else if (!child.getClass().getSimpleName().contains("Layout")) {      
                    // add onClickListener to the traversed view
                    OnClickListener listener = new InstrumentedListener(child, appContext);
                    child.setOnClickListener(listener);
                }

                traverseChildViewIds(child, parent);
            }
        }
    }
    
    public void traverseItemView(View view, ViewGroup parent, int pos) {
        if (view instanceof ViewGroup)
            traverseItemChildren(view, parent, pos);
        else
            return;
    }

    private void traverseItemChildren(View view, ViewGroup parent, int pos) {
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                View child = group.getChildAt(i);
                traverseItemChildren(child, parent, pos);
            }
        }
        // process elements in a viewGroup
        else {
            if (view instanceof CheckBox) {
                //CompoundButton.OnCheckedChangeListener listener = new InstrumentedOnCheckedChangeListener((CheckBox) view, pos, getContext());
                //((CheckBox) view).setOnCheckedChangeListener(listener);
                if (AseTestBridge.getExecutionMode() == ExecutionModeType.RECORD) {
                    view.setOnClickListener(new InstrumentedCheckBoxClickListener((CheckBox) view, parent, pos, appContext));
                }
            } else {
                OnClickListener listener = new InstrumentedListener(view, appContext);
                view.setOnClickListener(listener);
            }
        }
    }
    
    
}
