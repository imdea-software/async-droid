package ase.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import android.app.Activity;
import android.os.AsyncTask;
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
    
    public static boolean isAsyncTaskSerialThreadActive() {
        try {
            // get the serial executor instance
            Field serialExecutorField = Class.forName("android.os.AsyncTask").getDeclaredField("SERIAL_EXECUTOR");
            Object serialExecutor = serialExecutorField.get(null);

            // get mTasks field
            Class serialExecutorClass = Class.forName("android.os.AsyncTask$SerialExecutor");
            Field mActiveField = serialExecutorClass.getDeclaredField("mActive");
            mActiveField.setAccessible(true);
            Object mActive = mActiveField.get(serialExecutor);

        } catch (Exception ex) {
            Log.e("Reflectionnn", "Can not read AsyncTask serial executor active field");
        }
        return false;
    }

    public static BlockingQueue<Runnable> getAsyncTaskPoolExecutorTasks() {
        // executor.getQueue??
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
    
    // AsyncTask.THREAD_POOL_EXECUTOR ////// ????
    // active asyncTask threads (on serial executor and on thread pool executor)
    @SuppressWarnings("unchecked")
    public static int numActiveAsyncTaskThreads() {
        try {
            Field executorField = AsyncTask.class.getDeclaredField("THREAD_POOL_EXECUTOR");
            executorField.setAccessible(true);
            ThreadPoolExecutor executor = (ThreadPoolExecutor)executorField.get(null);

            Class threadPoolExecutor = getSuperClassOfType(executor.getClass(), "java.util.concurrent.ThreadPoolExecutor");
            Method getActiveCountMethod = threadPoolExecutor.getDeclaredMethod("getActiveCount");
            getActiveCountMethod.setAccessible(true);
            Integer activeCount = (Integer) getActiveCountMethod.invoke(executor);
         
            return activeCount.intValue();

        } catch (Exception ex) {
            Log.e("Reflectionnn", "Can not read AsyncTask serial executor active field");
        }
        return 0;
    }
       
    public static List<Object> getFragments(Activity act) {
        try
        {
            Class supFrag = Class.forName ("android.support.v4.app.FragmentActivity");
            Log.v("Reflection", "Get fragments from: android.support.v4.app.FragmentActivity");
            // reads fragments from android.support.v4.app.FragmentManager in android.support.v4.app.FragmentActivity
            // parameter is of type android.support.v4.app.FragmentActivity or android.app.Activity (if it does not have fragments)     
            return getFragmentsFromClass(act, "android.support.v4.app.FragmentActivity", "android.support.v4.app.FragmentManagerImpl");
        }
        catch (ClassNotFoundException e)
        {
            try {
                Class frag = Class.forName ("android.app.Fragment");
                Log.v("Reflection", "Get fragments from: android.app.Fragment");
                // reads fragments from android.app.FragmentManager
                // parameter is of type android.app.Activity                
                return getFragmentsFromClass(act, "android.app.Activity", "android.app.FragmentManagerImpl");                  
            } catch (ClassNotFoundException e1) {
                Log.e("Reflection", "Can not read fragment info");
            }  
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static List<Object> getFragmentsFromClass(Activity act, String actClass, String fragManClass) {
        List<Object> activeFragments = null;
        try {
            // get fragment manager by activity.getSupportFragmentManager()
            Method getFragmentManagerMethod = null;
            Class fragmentActivity = getSuperClassOfType(act.getClass(), actClass);
            getFragmentManagerMethod = fragmentActivity.getDeclaredMethod("getSupportFragmentManager");
            Object fragmentManager = getFragmentManagerMethod.invoke(act);

            Field mActiveField = null;
            Class<?> innerClass = Class.forName(fragManClass);
            mActiveField = innerClass.getDeclaredField("mActive");
            mActiveField.setAccessible(true);
            activeFragments = (List<Object>) mActiveField.get(fragmentManager);
        } catch (Exception ex) {
            activeFragments = null;
            Log.e("Reflection", "Can not read active fragments");
        }

        return activeFragments;
    } 
    
    public static int getActionBarTabPosition(Object tab) {
        String actionBarTabClassName;
        try
        {
            Class supTabClass = Class.forName("android.support.v7.app.ActionBar$Tab");
            actionBarTabClassName = "android.support.v7.app.ActionBar$Tab";
        }
        catch (ClassNotFoundException e)
        {
            try {
                Class tabClass = Class.forName("android.app.ActionBar$Tab");      
                actionBarTabClassName = "android.app.ActionBar$Tab";
            } catch (ClassNotFoundException e1) {
                Log.e("Reflection", "Can not read tab position");
                return -1;
            }  
        }
        Log.v("Reflection", "Get fragments from: " + actionBarTabClassName);
        return getActionBarTabPositionFromClass(tab, actionBarTabClassName);
    }
    
    private static int getActionBarTabPositionFromClass(Object tab, String className) {
        try {
            Class actionBarTab = Class.forName(className);
            Method getPositionMethod = actionBarTab.getDeclaredMethod("getPosition");
            Integer position = (Integer) getPositionMethod.invoke(tab);
            return position.intValue();
            
        } catch (Exception ex) {
            Log.e("Reflection", "Can not read tab position using " + className);
        }      
        return -1;
    }
    
    /*
     * Returns the name of the fragment in which the view with viewID is inflated
     */
    @SuppressWarnings({ "unchecked", "unused" })
    public static String getFragmentByViewID(Activity act, int viewId) {
        try
        {
            Class supFrag = Class.forName ("android.support.v4.app.FragmentActivity");
            Log.v("Reflection", "Get fragments from: android.support.v4.app.FragmentActivity");
            // reads fragments from android.support.v4.app.FragmentManager in android.support.v4.app.FragmentActivity
            // parameter is of type android.support.v4.app.FragmentActivity or android.app.Activity (if it does not have fragments)     
            return getFragmentByViewIdFromClass(act, viewId, "android.support.v4.app.Fragment");
        }
        catch (ClassNotFoundException e)
        {
            try {
                Class frag = Class.forName ("android.app.Fragment");
                Log.v("Reflection", "Get fragments from: android.app.Fragment");
                // reads fragments from android.app.FragmentManager
                // parameter is of type android.app.Activity                
                return getFragmentByViewIdFromClass(act, viewId, "android.app.Fragment");                  
            } catch (ClassNotFoundException e1) {
                Log.e("Reflection", "Can not read fragment info");
            }  
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
    private static String getFragmentByViewIdFromClass(Activity act, int viewId, String fragClassName) {
        try {
            Method isVisibleMethod = null, getViewMethod = null, findViewByIdMethod = null;
            Class fragmentClass = Class.forName(fragClassName);
            Class viewClass = Class.forName("android.view.View");

            isVisibleMethod = fragmentClass.getDeclaredMethod("isVisible");
            getViewMethod = fragmentClass.getDeclaredMethod("getView");
            findViewByIdMethod = viewClass.getDeclaredMethod("findViewById", Integer.TYPE);

            for(Object f: getFragments(act)) {
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

        return tempClass;
    }

}
