package ase.util;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.AdapterView;
import android.widget.CheckBox;
import ase.recorder.InstrumentedCheckBoxClickListener;
import ase.recorder.InstrumentedListener;

public class ViewUtils {
    /**
     * View Traversers instrument views with recording event handlers
     * Takes a given root view and sets proper event listeners for its children views
     * Called from: (1) Activity onCreate (2) Fragment onCreateView (3) AdapterView getView
     * If (3), i.e. the view is an item in an AdapterView, take its parent as well
     */
    public static View traverseViewIds(View view) {
        Log.v("ViewLogger", "traversing: " + view.getClass().getSimpleName() + ", id: " + Integer.toHexString(view.getId()) );
        if (view.getParent() != null && (view.getParent() instanceof ViewGroup)) {
            return traverseViewIds((View) view.getParent());
        } else {
            traverseChildViewIds(view);
            return view;
        }
    }

    @SuppressWarnings("rawtypes")
    private static void traverseChildViewIds(View view) {
        
        if(view.getClass().getSimpleName().equals("ActionBarContainer")) {
            Log.i("ViewLogger", "ActionBarContainer Detail: " + view.toString() + " ID: " + Integer.toHexString(view.getId()));
            return;
        }

        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                View child = group.getChildAt(i);
                Log.v("ViewLogger", "traversed: " + child.getClass().getSimpleName() + " " + Integer.toHexString(child.getId()));

                // moved to onItemClickListener in the app code
                if(child instanceof AdapterView) {                    
                    //if(child instanceof ListView) {
                        // add onItemClickListener to the adapter
                        // the following two lines were there!
                        //--//AdapterView.OnItemClickListener listener = new InstrumentedItemClickListener((AdapterView) child);
                        //--//((AdapterView) child).setOnItemClickListener(listener);
                        
                    //}

                } else if (!child.getClass().getSimpleName().contains("Layout")) {      
                    // add onClickListener to the traversed view
                    // if it has a listener
                    InstrumentedListener listener = new InstrumentedListener(child);
                    if(listener.getOwnListener() != null)
                        child.setOnClickListener(listener);    
                }

                traverseChildViewIds(child);
            }
        }
    }
    
    public static void traverseItemView(View view, ViewGroup parent, int pos) {
        if (view instanceof ViewGroup)
            traverseItemChildren(view, parent, pos);
        else
            return;
    }

    private static void traverseItemChildren(View view, ViewGroup parent, int pos) {
        
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                View child = group.getChildAt(i);
                traverseItemChildren(child, parent, pos);
            }
            
        } else {

            if (view instanceof CheckBox) {                
                view.setOnClickListener(new InstrumentedCheckBoxClickListener((CheckBox) view, parent, pos));                
                
            } else {
                // add onClickListener to the traversed view only if it has a specific listener (other than its parent "itemView")
                // otherwise the new listener overwrites adapter view's item listener
                InstrumentedListener listener = new InstrumentedListener(view);
                if(listener.getOwnListener() != null)
                    view.setOnClickListener(listener);    
            }
        }
    }
    
    @SuppressWarnings("rawtypes")
    public static List<Integer> logViewParents(ViewParent p) {
        int parentId = -1;
        List<Integer> ancestors = new ArrayList<Integer>();
        
        while(p != null) {
            if(p instanceof ViewGroup) {
                parentId = ((ViewGroup)p).getId();
            } else if(p instanceof AdapterView) {
                parentId = ((AdapterView)p).getId();
            } else if (p.toString().contains("ViewRootImpl")) {
                break; // top of the view hierarchy
            }
            ancestors.add((Integer)parentId);
            p = p.getParent();
        }
        
        return ancestors;
    }
}
