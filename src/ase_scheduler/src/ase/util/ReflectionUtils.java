package ase.util;

import java.lang.reflect.Field;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.CompoundButton.OnCheckedChangeListener;

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

   
    public static OnCheckedChangeListener  getOnCheckedChangeListener(View view) {
        OnCheckedChangeListener listener = null;
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
                listener = (OnCheckedChangeListener) listenerField.get(myLiObject);
                Log.i("Recorder", "Checkbox listener cannot be read");
            }
        } catch (Exception ex) {
            listener = null;
        }
        return listener;
    }
    
    public static AdapterView.OnItemClickListener getOnItemClickListener(AdapterView view) {
        AdapterView.OnItemClickListener listener = null;
        try {
            Field listenerField = null;
            listenerField = getSuperClassOfType(view.getClass(), AdapterView.class.getName()).getDeclaredField("mOnItemClickListener");
            listenerField.setAccessible(true);
            listener = (AdapterView.OnItemClickListener) listenerField.get(view);
        } catch (Exception ex) {
            listener = null;
        }
        return listener;
    }

    public static AdapterView.OnItemSelectedListener getOnItemSelectedListener(AdapterView view) {
        AdapterView.OnItemSelectedListener listener = null;
        try {
            Field listenerField = null;
            listenerField = getSuperClassOfType(view.getClass(), AdapterView.class.getName()).getDeclaredField("mOnItemSelectedListener");
            listenerField.setAccessible(true);
            listener = (AdapterView.OnItemSelectedListener) listenerField.get(view);
        } catch (Exception ex) {
            listener = null;
        }
        return listener;
    }
    
    @SuppressWarnings("unchecked")
    public static List<Fragment> getFragments(Activity act) {
        List<Fragment> activeFragments = null;
        try {
            Field mActiveField = null;
            mActiveField = getSuperClassOfType(act.getClass(), Activity.class.getName()).getDeclaredField("mActive");
            mActiveField.setAccessible(true);       
            activeFragments = (List<Fragment>) mActiveField.get(act);
        } catch (Exception ex) {
            activeFragments = null;
        }

        Log.e("Reflection", activeFragments.toString());
        return activeFragments;
    }

    public static Class getSuperClassOfType(Class clazz, String superClassName) {
        Class tempClass = clazz;
        while (tempClass != null && !tempClass.getName().equals(superClassName))
            tempClass = tempClass.getSuperclass();

        Log.i("Recorder", tempClass == null ? "null" : tempClass.getName());

        return tempClass;
    }

}
