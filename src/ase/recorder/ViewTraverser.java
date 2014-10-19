package ase.recorder;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;

public class ViewTraverser {

    public static Context applicationContext;
    public static View CURRENT_ROOT_VIEW;

    public static void setViewViewerContext(Context context) {
        applicationContext = context;
    }

    public static void setRootView(View view) {
        CURRENT_ROOT_VIEW = view;
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
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                View child = group.getChildAt(i);
                Log.v("ViewLogger", "traversed: " + child.getClass().getSimpleName() + " " + Integer.toHexString(child.getId()));
                
                // add listener to the traversed view
                if(!child.getClass().getSimpleName().contains("Layout")){
                    OnClickListener listener = new InstrumentedListener(child, applicationContext); ////////////////
                    child.setOnClickListener(listener); //////////////////////
                }

                traverseChildViewIds(child);
            }
        }
    }

    
/*  // initially send with 0, in recursion sent by incrementing spaces
 * private static String padString(String str, int noOfSpaces) {
        if (noOfSpaces <= 0) {
            return str;
        }
        StringBuilder builder = new StringBuilder(str.length() + noOfSpaces);
        for (int i = 0; i < noOfSpaces; i++) {
            builder.append(' ');
        }
        return builder.append(str).toString();
    }
*/
}
