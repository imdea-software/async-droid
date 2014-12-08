package ase.util;

import java.lang.reflect.Field;

import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;

public class ReflectionUtils {

    public static OnClickListener getOnClickListener(View view) {
        OnClickListener listener = null;
        try {
            Field listenerInfoField = null;
            listenerInfoField = View.class.getDeclaredField("mListenerInfo");
            if (listenerInfoField != null) {
                listenerInfoField.setAccessible(true);
            }
            Object myLiObject = null;
            myLiObject = listenerInfoField.get(view);

            // get the field mOnClickListener
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

    public static AdapterView.OnItemClickListener getOnItemClickListener(AdapterView view) {
        AdapterView.OnItemClickListener listener = null;
        try {
            // get the field mOnClickListener that holds the listener
            Field listenerField = null;
            listenerField = getSuperClassOfType(view.getClass(), AdapterView.class.getName()).getDeclaredField("mOnItemClickListener");
            listenerField.setAccessible(true);
            listener = (AdapterView.OnItemClickListener) listenerField.get(view);
        } catch (Exception ex) {
            listener = null;
        }
        return listener;
    }

    public static Class getSuperClassOfType(Class clazz, String superClassName) {
        Class tempClass = clazz;
        while (tempClass != null && !tempClass.getName().equals(superClassName))
            tempClass = tempClass.getSuperclass();

        Log.i("Recorder", tempClass == null ? "null" : tempClass.getName());

        return tempClass;
    }

}
