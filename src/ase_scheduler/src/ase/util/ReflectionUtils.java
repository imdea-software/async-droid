package ase.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.List;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;

public class ReflectionUtils {

    private static final String CN_ACTIVITY = "android.app.Activity"; 
    private static final String CN_ACTIVITY_ACTIONBAR_SUPPORT = "android.support.v7.app.ActionBarActivity";
    private static final String CN_ACTION_BAR = "android.app.ActionBar";
    private static final String CN_ACTION_BAR_SUPPORT = "android.support.v7.app.ActionBar";
    private static final String CN_ACT_ACTION_BAR_SUPPORT_DEL = "android.support.v7.app.ActionBarActivityDelegate";
    private static final String CN_ACTIVITY_SUPPORT = "android.support.v4.app.FragmentActivity"; 
    private static final String CN_FRAGMENT_MANAGER = "android.app.FragmentManagerImpl";
    private static final String CN_FRAGMENT_MANAGER_SUPPORT = "android.support.v4.app.FragmentManagerImpl";
    private static final String CN_FRAGMENT = "android.app.Fragment";
    private static final String CN_FRAGMENT_SUPPORT = "android.support.v4.app.Fragment";
    private static final String CN_ACTION_BAR_TAB = "android.app.ActionBar$Tab";
    private static final String CN_ACTION_BAR_TAB_SUPPORT = "android.support.v7.app.ActionBar$Tab";
    private static final String CN_ASYNC_TASK = "android.os.AsyncTask";
    private static final String CN_ASYNC_TASK_SERIAL_EXECUTOR = "android.os.AsyncTask$SerialExecutor";
    private static final String CN_VIEW = "android.view.View";
    private static final String CN_VIEW_LISTENER_INFO = "android.view.View$ListenerInfo";

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
            listenerField = Class.forName(CN_VIEW_LISTENER_INFO)
                    .getDeclaredField("mOnClickListener");
            if (listenerField != null && myLiObject != null) {
                listener = (View.OnClickListener) listenerField.get(myLiObject);
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
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static ArrayDeque<Runnable> getAsyncTaskSerialExecutorTasks() {
        try {
            // get the serial executor instance
            Field serialExecutorField = Class.forName(CN_ASYNC_TASK).getDeclaredField("SERIAL_EXECUTOR");
            Object serialExecutor = serialExecutorField.get(null);

            // get mTasks field
            Class serialExecutorClass = Class.forName(CN_ASYNC_TASK_SERIAL_EXECUTOR);
            Field mTasksField = serialExecutorClass.getDeclaredField("mTasks");
            mTasksField.setAccessible(true);
            Object mTasks = mTasksField.get(serialExecutor);

            return ((ArrayDeque<Runnable>) mTasks).clone();

        } catch (Exception ex) {
            Log.e("Reflectionnn", "Can not read AsyncTask serial executor work queue");
        }
        return null;
    }
    
    @SuppressWarnings({ "rawtypes"})
    public static boolean isAsyncTaskSerialThreadActive() {
        try {
            // get the serial executor instance
            Field serialExecutorField = Class.forName(CN_ASYNC_TASK).getDeclaredField("SERIAL_EXECUTOR");
            Object serialExecutor = serialExecutorField.get(null);

            // get mTasks field
            Class serialExecutorClass = Class.forName(CN_ASYNC_TASK_SERIAL_EXECUTOR);
            Field mActiveField = serialExecutorClass.getDeclaredField("mActive");
            mActiveField.setAccessible(true);
            Object mActive = mActiveField.get(serialExecutor);
            return ((Boolean)mActive).booleanValue();
            
        } catch (Exception ex) {
            Log.e("Reflectionnn", "Can not read AsyncTask serial executor active field");
        }
        return false;
    }

    /**
     * Returns the list of active fragments in the activity
     * Returns null if no fragments
     * If the act is of type android.app.Activity 
     *   - Gets the fragments from: android.app.FragmentManagerImpl 
     * If the act is of type android.support.v4.app.FragmentActivity
     *   - Gets the fragments from: android.support.v4.app.FragmentManagerImpl
     * (FragmentManager is defined in Android API level 11)
     */ 
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static List<Object> getFragments(Activity act) {
        String activityClassName = null, 
               fragmentManagerClassName = null, 
               getFMMethodName = null;
        
        if(getSuperClassOfType(act.getClass(), CN_ACTIVITY_SUPPORT) != null) {
            activityClassName = CN_ACTIVITY_SUPPORT;
            fragmentManagerClassName = CN_FRAGMENT_MANAGER_SUPPORT;
            getFMMethodName = "getSupportFragmentManager";
        } else if(getSuperClassOfType(act.getClass(), CN_ACTIVITY) != null) {
            activityClassName = CN_ACTIVITY;
            fragmentManagerClassName = CN_FRAGMENT_MANAGER;
            getFMMethodName = "getFragmentManager";
        }

        List<Object> activeFragments = null;
        try {
            Method getFragmentManagerMethod = null;
            Class activity = getSuperClassOfType(act.getClass(), activityClassName);
            getFragmentManagerMethod = activity.getDeclaredMethod(getFMMethodName);
            Object fragmentManager = getFragmentManagerMethod.invoke(act);

            if(fragmentManager == null) return null;
            
            Field mActiveField = null;
            Class<?> fragmentManClass = Class.forName(fragmentManagerClassName);
            mActiveField = fragmentManClass.getDeclaredField("mActive");
            mActiveField.setAccessible(true);
            activeFragments = (List<Object>) mActiveField.get(fragmentManager);
        } catch (Exception ex) {
            activeFragments = null;
            Log.e("Reflection", "Can not read active fragments: fmMethod" + getFMMethodName, ex);
        }

        return activeFragments;
    } 
    
    /**
     * Returns the name of the fragment in which the view with viewID is inflated
     * If the act is of type android.app.Activity 
     *   - Gets the view of the fragment in: android.app.Fragment 
     * If the act is of type android.support.v4.app.FragmentActivity
     *   - Gets the view of the fragment in: android.support.v4.app.Fragment 
     * (Fragment is defined in Android API level 11)
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static String getFragmentByViewID(Activity act, int viewId) {
        String fragmentClassName = null;
        if(getSuperClassOfType(act.getClass(), CN_ACTIVITY_SUPPORT) != null) {
            fragmentClassName = CN_FRAGMENT_SUPPORT;
        } else if(getSuperClassOfType(act.getClass(), CN_ACTIVITY) != null) {
            fragmentClassName = CN_FRAGMENT;
        }

        try {
            Method isVisibleMethod = null, getViewMethod = null, findViewByIdMethod = null;
            Class fragmentClass = Class.forName(fragmentClassName);
            Class viewClass = Class.forName(CN_VIEW);

            isVisibleMethod = fragmentClass.getDeclaredMethod("isVisible");
            getViewMethod = fragmentClass.getDeclaredMethod("getView");
            findViewByIdMethod = viewClass.getDeclaredMethod("findViewById", Integer.TYPE);

            List<Object> fragments = getFragments(act);
            if(fragments != null) {
                for(Object f: fragments) {
                    Object rootView = getViewMethod.invoke(f);
                    if(rootView == null) return null;
                    View v = (View) findViewByIdMethod.invoke(rootView, viewId);
                    if(v == null) return null; // view is not inside that fragment
                    
                    Boolean b = (Boolean) isVisibleMethod.invoke(f);
                    if(b.booleanValue()) {
                        return f.getClass().getName();
                    }
                }
            }
        } catch (Exception ex) {
            Log.e("Reflection", "Can not read active fragments ", ex);
        }

        return null;
    }
    
    /**
     * Returns the mActionBar of the Activity class
     * (This method is used by the InputRepeater to check if it is initialized or not)
     */
    @SuppressWarnings("rawtypes")
    public static Object getActionBarInstance(Object act) {
        String activityClassName = null;
        if(getSuperClassOfType(act.getClass(), CN_ACTIVITY_ACTIONBAR_SUPPORT) != null) {
            activityClassName = CN_ACTIVITY_ACTIONBAR_SUPPORT;
                      
            try {
                Class activityClass = Class.forName(activityClassName);
                Field mActionBarActDelegateField = activityClass.getDeclaredField("mImpl");
                mActionBarActDelegateField.setAccessible(true);            
                Object mActionBarActDelegate = mActionBarActDelegateField.get(act);
                Class delegateClass = Class.forName(CN_ACT_ACTION_BAR_SUPPORT_DEL);
                Field mActionBarField = delegateClass.getDeclaredField("mActionBar");
                mActionBarField.setAccessible(true);
                Object mActionBar = mActionBarField.get(mActionBarActDelegate);
                return mActionBar;
                
            } catch (Exception ex) {
                Log.e("Reflection", "Can not read mActionBar of activity " + activityClassName);
            }      
            

        } else if(getSuperClassOfType(act.getClass(), CN_ACTIVITY) != null) {
            activityClassName = CN_ACTIVITY;
            
            try {
                Class activityClass = Class.forName(activityClassName);
                Field mActionBarField = activityClass.getDeclaredField("mActionBar");
                mActionBarField.setAccessible(true);
                Object mActionBar = mActionBarField.get(act);
                return mActionBar;
                
            } catch (Exception ex) {
                Log.e("Reflection", "Can not read mActionBar of activity " + activityClassName);
            }           
        }

        return null;
    }
    
    /////Cannot call this as isShowing is not included in the instrumented app (lost during translation)
    /**
     * Returns the position of the tab in ActionBar
     * (ActionBar is defined in Android API level 11)
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static boolean isActionBarShowing(Object bar) {
        String actionBarClassName = null;
        if(getSuperClassOfType(bar.getClass(), CN_ACTION_BAR_SUPPORT) != null) {
            actionBarClassName = CN_ACTION_BAR_SUPPORT;
        } else if(getSuperClassOfType(bar.getClass(), CN_ACTION_BAR) != null) {
            actionBarClassName = CN_ACTION_BAR;
        }

        try {
            Class actionBar = Class.forName(actionBarClassName);
            Method isShowingMethod = actionBar.getDeclaredMethod("isShowing");
            Boolean isShowing = (Boolean) isShowingMethod.invoke(bar);
            return isShowing.booleanValue();
            
        } catch (Exception ex) {
            Log.e("Reflection", "Can not check whether action bar is visible using " + actionBarClassName);
        }      
        return false;
    }
    
    /**
     * Returns the position of the tab in ActionBar
     * (ActionBar is defined in Android API level 11)
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static int getActionBarTabCount(Object bar) {
        String actionBarClassName = null;
        if(getSuperClassOfType(bar.getClass(), CN_ACTION_BAR_SUPPORT) != null) {
            actionBarClassName = CN_ACTION_BAR_SUPPORT;
        } else if(getSuperClassOfType(bar.getClass(), CN_ACTION_BAR) != null) {
            actionBarClassName = CN_ACTION_BAR;
        }

        try {
            Class actionBar = Class.forName(actionBarClassName);
            Method getTabCountMethod = actionBar.getDeclaredMethod("getTabCount");
            Integer tabCount = (Integer) getTabCountMethod.invoke(bar);
            return tabCount.intValue();
            
        } catch (Exception ex) {
            Log.e("Reflection", "Can not get action bar tab count using " + actionBarClassName);
        }      
        return 0;
    }
    
    /**
     * Returns the position of the tab in ActionBar
     * (ActionBar is defined in Android API level 11)
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static int getActionBarTabPosition(Object tab) {
        String actionBarTabClassName = null;
        if(getSuperClassOfType(tab.getClass(), CN_ACTION_BAR_TAB) != null) {
            actionBarTabClassName = CN_ACTION_BAR_TAB;
        } else if(getSuperClassOfType(tab.getClass(), CN_ACTION_BAR_TAB_SUPPORT) != null) {
            actionBarTabClassName = CN_ACTION_BAR_TAB_SUPPORT;
        }

        try {
            Class actionBarTab = Class.forName(actionBarTabClassName);
            Method getPositionMethod = actionBarTab.getDeclaredMethod("getPosition");
            Integer position = (Integer) getPositionMethod.invoke(tab);
            return position.intValue();
            
        } catch (Exception ex) {
            Log.e("Reflection", "Can not read tab position using " + actionBarTabClassName);
        }      
        return -1;
    }
          
    @SuppressWarnings("rawtypes")
    public static Class getSuperClassOfType(Class clazz, String superClassName) {
        Class tempClass = clazz;
        while (tempClass != null && !tempClass.getName().equals(superClassName)) {
            tempClass = tempClass.getSuperclass();
        }

        return tempClass;
    }

}
