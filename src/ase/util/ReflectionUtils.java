package ase.util;

import java.lang.reflect.Field;
import java.util.ArrayList;

import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

public class ReflectionUtils {

    public static ArrayList<Integer> getViewsInApp(String packageName) {
        ArrayList<Integer> viewIDs = new ArrayList<Integer>();
        
        try {
            Class<?> r = Class.forName(packageName + ".R$id");
            
            for (Field f : r.getDeclaredFields()) {
                viewIDs.add((Integer)f.getInt(new Object()));
                // Log.i("ReflectionUtils", " " + Integer.toHexString(f.getInt(new Object())));
            }
            
        } catch (Exception e) {
            Log.i("ReflectionUtils", "Exception during reflection!");
        }
            
        return viewIDs;
    }
    
    public static OnClickListener getOnClickListener(View view) {
        OnClickListener listener = null;
        try {
            Field listenerInfoField = null;
            listenerInfoField = View.class
                    .getDeclaredField("mListenerInfo");
            if (listenerInfoField != null) {
                listenerInfoField.setAccessible(true);
            }
            Object myLiObject = null;
            myLiObject = listenerInfoField.get(view);

            // get the field mOnClickListener, that holds the listener and cast
            // it to a listener
            Field listenerField = null;
            listenerField = Class.forName("android.view.View$ListenerInfo")
                    .getDeclaredField("mOnClickListener");
            if (listenerField != null && myLiObject != null) {
                listener = (View.OnClickListener) listenerField.get(myLiObject);
            }
        } catch (Exception ex) {
            listener = null;
        }
        return listener;
    }
}
