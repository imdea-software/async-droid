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
import android.view.ViewParent;
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
    
    /**
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
        //executeFragmentTransactions(); ?
    }

    public void finishCurrentActivity() {
        Log.i("AsyncDroid", "Finishing activity.");
        currentActivity.finish();
    }

    public void executeFragmentTransactions() {
        FragmentManager fm = currentActivity.getFragmentManager();
        fm.executePendingTransactions();
    }
 
}
