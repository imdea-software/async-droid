package ase;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Spinner;
import ase.recorder.InstrumentedItemClickListener;
import ase.recorder.InstrumentedItemSelectedListener;
import ase.recorder.InstrumentedListener;

public class AppRunTimeData {

    private String packageName;
    private Context appContext;
    private Activity currentActivity;
    private View activityRootView;
    private Menu actionBarMenu;
    
    public AppRunTimeData(Context context) {
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
        Log.v("Repeater", "Current activity view: " + view.toString());
    }
    
    /*
     * Traverse view ids to instrument views with recording event handlers
     * Takes a given root view and sets proper event listeners for its children views
     * Called from: (1) Activity onCreate (2) Fragment onCreateView (3) AdapterView getView
     * If called in a fragment, record the fragment name in the event as well
     *  (moved from a former class ViewTraverser.java)
     */
    public View traverseViewIds(View view, Fragment f) {
        Log.v("ViewLogger", "traversing: " + view.getClass().getSimpleName() + ", id: " + view.getId() );
        if (view.getParent() != null && (view.getParent() instanceof ViewGroup)) {
            return traverseViewIds((View) view.getParent(), f);
        } else {
            traverseChildViewIds(view, f);
            return view;
        }
    }

    private void traverseChildViewIds(View view, Fragment f) {

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

                traverseChildViewIds(child, f);
            }
        }
    }
    
    
}
