package ase.recorder;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Spinner;

public class ViewTraverser {

    public static Context applicationContext;
    public static View CURRENT_ROOT_VIEW;

    public static void setViewViewerContext(Context context) {
        applicationContext = context;
    }

    public static void setRootView(View view) {
        CURRENT_ROOT_VIEW = view;
        Log.i("Press", "" + view.toString());
    }

    public static View traverseViewIds(View view) {
        Log.v("ViewLogger", "traversing: " + view.getClass().getSimpleName()
                + ", id: " + view.getId() + "Inthread: "
                + Thread.currentThread().getName());
        if (view.getParent() != null && (view.getParent() instanceof ViewGroup)) {
            return traverseViewIds((View) view.getParent());
        } else {
            traverseChildViewIds(view);
            return view;
        }
    }

    private static void traverseChildViewIds(View view) {

        if(view.getClass().getSimpleName().equals("ActionBarContainer")) {
            Log.i("ViewLogger", "ActionBarContainer Detail: " + view.toString() + " ID: " + view.getId());
            // traverseActionBar(view);
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
                        AdapterView.OnItemClickListener listener = new InstrumentedItemClickListener((AdapterView) child, applicationContext);
                        ((AdapterView) child).setOnItemClickListener(listener);
                    } else if (child instanceof Spinner) {
                        Log.i("ViewLogger", "Spinner view: Id: " + child.getId() + " " + ((AdapterView) child).getCount());
                        // add onItemClickListener to the adapter
                        AdapterView.OnItemSelectedListener listener = new InstrumentedItemSelectedListener((AdapterView) child, applicationContext);
                        ((AdapterView) child).setOnItemSelectedListener(listener);
                    } else {
                        Log.i("ViewLogger", "Cannot record grid view or gallery view");
                    }

                } else if (!child.getClass().getSimpleName().contains("Layout")) {
                    // add onClickListener to the traversed view
                    OnClickListener listener = new InstrumentedListener(child, applicationContext);
                    child.setOnClickListener(listener);
                }

                traverseChildViewIds(child);
            }
        }
    }
/*
    private static void traverseActionBar(View view) {
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                View child = group.getChildAt(i);
                Log.v("ViewLogger", "traversed: " + child.getClass().getSimpleName() + " "
                        + Integer.toHexString(child.getId()));

                if (child.getClass().getSimpleName().equals("ImageView")) {
                    // add listener to the parent of HomeView
                    OnClickListener listener = new ListenerToRecordUp(child, applicationContext);
                    child.setOnClickListener(listener);
                }

                traverseActionBar(child);
            }
        }
    }*/

}
