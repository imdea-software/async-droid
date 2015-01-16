package ase.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class ReflectionUtils {

    static final int LIBRARY_TYPE = getLibraryType();
    
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
    
    @SuppressWarnings("rawtypes")
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

    @SuppressWarnings("rawtypes")
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
    
    public static ArrayDeque<Runnable> getAsyncTaskSerialExecutorTasks() {
        try {
            // get the serial executor instance
            Field serialExecutorField = Class.forName("android.os.AsyncTask").getDeclaredField("SERIAL_EXECUTOR");
            Object serialExecutor = serialExecutorField.get(null);

            // get mTasks field
            Class serialExecutorClass = Class.forName("android.os.AsyncTask$SerialExecutor");
            Field mTasksField = serialExecutorClass.getDeclaredField("mTasks");
            mTasksField.setAccessible(true);
            Object mTasks = mTasksField.get(serialExecutor);

            return (ArrayDeque<Runnable>) mTasks;

        } catch (Exception ex) {
            Log.e("Reflectionnn", "Can not read AsyncTask serial executor work queue");
        }
        return null;
    }

    public static BlockingQueue<Runnable> getAsyncTaskPoolExecutorTasks() {

        try {
            Field workQueueField = AsyncTask.class.getDeclaredField("sPoolWorkQueue");
            workQueueField.setAccessible(true);
            Object workQueue = workQueueField.get(null);

            return (BlockingQueue<Runnable>) workQueue;

        } catch (Exception ex) {
            Log.e("Reflection", "Can not read AsyncTask pool executor work queue");
        }

        return null;
    }
    // TODO revise, improve and enum support libraries
    private static int getLibraryType() {
        try
        {
            Class supFrag = Class.forName ("android.support.v4.app.FragmentActivity");
            Log.v("Reflection", "Get fragments from: android.support.v4.app.FragmentActivity");
            return 1; // uses support library v4 
        }
        catch (ClassNotFoundException e)
        {
            try {
                Class frag = Class.forName ("android.app.Fragment");
                Log.v("Reflection", "Get fragments from: android.app.Fragment");
                return 0; // uses a higher version of Android 
            } catch (ClassNotFoundException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }  
        }
        return -1;
    }

    @SuppressWarnings({ "unchecked", "unused" })
    public static List<Object> getFragments(Activity act) {
      //TODO merge these two "getFragments" methods
        if(LIBRARY_TYPE == 1) {
            return getFragmentsUsingSupportFragmentManager(act);
        } else if (LIBRARY_TYPE == 0) {
            getFragmentsUsingFragmentManager(act);  
        }     
        
        return null;
    }
     
    // reads fragments from android.app.FragmentManager
    // parameter is of type android.app.Activity 
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static List<Object> getFragmentsUsingFragmentManager(Activity act) {
        List<Object> activeFragments = null;
        try {
            // get fragment manager by activity.getFragmentManager() in android.app.Activity
            Method getFragmentManagerMethod = null;
            Class fragmentActivity = getSuperClassOfType(act.getClass(), "android.app.Activity");
            getFragmentManagerMethod = fragmentActivity.getDeclaredMethod("getFragmentManager");
            Object fragmentManager = getFragmentManagerMethod.invoke(act);
            
            Field mActiveField = null;
            Class<?> innerClass = Class.forName("android.app.FragmentManagerImpl");
            mActiveField = innerClass.getDeclaredField("mActive");
            mActiveField.setAccessible(true);       
            activeFragments = (List<Object>) mActiveField.get(fragmentManager);
        } catch (Exception ex) {
            activeFragments = null;
            Log.e("Reflection", "Can not read active fragments");
        }

        return activeFragments;
    }

    
    // reads fragments from android.support.v4.app.FragmentManager in android.support.v4.app.FragmentActivity
    // parameter is of type android.support.v4.app.FragmentActivity or android.app.Activity (if it does not have fragments)
    @SuppressWarnings("unchecked")
    private static List<Object> getFragmentsUsingSupportFragmentManager(Activity act) {
        List<Object> activeFragments = null;
        try {
            // get fragment manager by activity.getSupportFragmentManager()
            Method getFragmentManagerMethod = null;
            Class fragmentActivity = getSuperClassOfType(act.getClass(), "android.support.v4.app.FragmentActivity");
            getFragmentManagerMethod = fragmentActivity.getDeclaredMethod("getSupportFragmentManager");
            Object fragmentManager = getFragmentManagerMethod.invoke(act);

            Field mActiveField = null;
            Class<?> innerClass = Class.forName("android.support.v4.app.FragmentManagerImpl");
            mActiveField = innerClass.getDeclaredField("mActive");
            mActiveField.setAccessible(true);

            activeFragments = (List<Object>) mActiveField.get(fragmentManager);
        } catch (Exception ex) {
            activeFragments = null;
            Log.e("Reflection", "Can not read active fragments");
        }

        return activeFragments;
    } 
    
    /*
     * Returns the name of the fragment in which the view with viewID is inflated
     */
    @SuppressWarnings({ "unchecked", "unused" })
    public static String getFragmentByViewID(Activity act, int viewId) {
        //TODO merge these two "getFragments" methods
        if(LIBRARY_TYPE == 1) {
            return getFragmentByViewIdUsingSupportFragmentManager(act, viewId);
        } else if (LIBRARY_TYPE == 0) {
            // TODO
            //getFragmentByViewIdUsingFragmentManager(act, viewId);  
        }     
        return null;
    }
    
    @SuppressWarnings("unchecked")
    private static String getFragmentByViewIdUsingSupportFragmentManager(Activity act, int viewId) {
        try {
            Method isVisibleMethod = null, getViewMethod = null, findViewByIdMethod = null;
            Class fragmentClass = Class.forName("android.support.v4.app.Fragment");
            Class viewClass = Class.forName("android.view.View");

            isVisibleMethod = fragmentClass.getDeclaredMethod("isVisible");
            getViewMethod = fragmentClass.getDeclaredMethod("getView");
            findViewByIdMethod = viewClass.getDeclaredMethod("findViewById", Integer.TYPE);

            for(Object f: getFragmentsUsingSupportFragmentManager(act)) {
                Object rootView = getViewMethod.invoke(f);

                View v = (View) findViewByIdMethod.invoke(rootView, viewId);
                Boolean b = (Boolean) isVisibleMethod.invoke(f);
                if( (v != null) && (b.booleanValue())) {
                    return f.getClass().getName();
                }
            }

        } catch (Exception ex) {
            Log.e("Reflection", "Can not read active fragments");
        }

        return null;
    }
    
      
    @SuppressWarnings("rawtypes")
    public static Class getSuperClassOfType(Class clazz, String superClassName) {
        Class tempClass = clazz;
        while (tempClass != null && !tempClass.getName().equals(superClassName))
            tempClass = tempClass.getSuperclass();

        //Log.i("Recorder", tempClass == null ? "null" : tempClass.getName());

        return tempClass;
    }

}
