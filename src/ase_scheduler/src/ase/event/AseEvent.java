package ase.event;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Log;
import android.view.View;
import ase.AppRunTimeData;
import ase.util.ViewUtils;

public abstract class AseEvent {    
    
    public enum EventType {
        CLICK, CHECKBOX, ITEMCLICK, ACTIONBAR, ACTIONBARTAB, NAVIGATEUP, BACK
    }
    
    public final EventType type;
    public final int viewId;
    // ids of the views on the full path to the event view 
    public final List<Integer> path;
    
    /*
     * If the view is inflated inside a fragment, keep the name of that fragment
     * An event is firable only if that fragment is visible 
     * (A view with the same id may exist in more than one fragments)
     */
    public final String fragmentName;

    protected AseEvent(EventType type, int viewId, List<Integer> path) {
        this.type = type;
        this.viewId = viewId;
        this.path = path;
        
        if(type == EventType.ACTIONBAR || type == EventType.ACTIONBARTAB)
            fragmentName = null;
        else
            fragmentName = AppRunTimeData.getInstance().getFragmentNameByViewId(viewId);
    }
   
    protected AseEvent(EventType type, int viewId, List<Integer> path, int parentId) {
        this.type = type;
        this.viewId = viewId;
        this.path = path;
        fragmentName = AppRunTimeData.getInstance().getFragmentNameByViewId(viewId);
    }
    
    protected AseEvent(EventType type, JSONObject jsonEvent) {
        this.type = type;
        this.viewId = jsonEvent.optInt("viewId");
        fragmentName = jsonEvent.optString("fragmentName", "");
        
        JSONArray jsonPath = jsonEvent.optJSONArray("path");
        path = new ArrayList<Integer>();
        if (jsonPath != null && jsonPath.length() > 0) {
            for (int i = 0; i < jsonPath.length(); i++) {
                path.add(jsonPath.optInt(i));
            }
        }
    }
    
    public boolean isFirable() {
        // check root view
        AppRunTimeData appData = AppRunTimeData.getInstance();
        if(appData.getActivityRootView() == null)
            return false;

        // check if the view id is loaded
        View curView = AppRunTimeData.getInstance().getActivityRootView().findViewById(viewId);
        if(curView == null)
            return false;
            
        // check if the full path of the recorded path match with current view path
        if(path.size() > 0) {
            List<Integer> curPath = ViewUtils.logViewParents(curView.getParent());
            if (path.size() != curPath.size()) 
                return false;
            
            for (int i=0; i< path.size(); i++) {
                if (!path.get(i).equals(curPath.get(i))) 
                    return false;
            }
        }
        
        // check the fragment
        String s = appData.getFragmentNameByViewId(viewId);
        if(fragmentName != null)
            Log.i("Event's", fragmentName);
        if(s != null)
            Log.i("Current", s);
        return (fragmentName == null) || fragmentName.equals(s);
    }

    abstract public void injectEvent();

    /**
     * Converts the event into JSONObject. Clients might need to override this method if they define
     * extra values. Also, the field names here should be the same names used in constructors.
     * @return JSON object
     * @throws Exception
     */
    public JSONObject toJson() throws Exception {
        JSONObject json = new JSONObject();
        json.put("type", type.name())
            .put("viewId", viewId)
            .put("fragmentName", fragmentName)
            .put("path", new JSONArray(path));
        return json;
    }
    
    public static AseEvent createEvent(JSONObject jsonEvent) {
        EventType type = EventType.valueOf(jsonEvent.optString("type", "INVALID"));
        AseEvent event = null;
        
        switch(type) {
            case ACTIONBAR: 
                event = new AseActionBarEvent(jsonEvent);
                break;
            case ACTIONBARTAB: 
                event = new AseActionBarTabEvent(jsonEvent);
                break;
            case CHECKBOX:
                event = new AseCheckBoxEvent(jsonEvent);
                break;
            case CLICK:
                event = new AseClickEvent(jsonEvent);
                break;
            case ITEMCLICK: 
                event = new AseItemClickEvent(jsonEvent);
                break;
            case NAVIGATEUP: 
                event = new AseNavigateUpEvent(jsonEvent);
                break;
            case BACK:
                event = new AseBackButtonEvent(jsonEvent);
                break;
            default:
                // Log unimplemented type
                break;
        }
        
        return event;
    }
}
