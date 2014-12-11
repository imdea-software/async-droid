package ase;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.*;

/**
 * Created by burcuozkan on 11/12/14.
 */
public class AseNavigateUpEvent extends AseEvent {

    private String currentActivityName;

    // viewId is the id of the AdapterView
    // pos is the position of the item in the AdapterView
    public AseNavigateUpEvent(int viewId, String actName) {
        super(EventType.NAVIGATEUP, viewId);
        currentActivityName = actName;
    }

    @Override
    public String toString() {
        return String.format("%s %d %s", type.name(), viewId, currentActivityName);
    }

    @Override
    public boolean isFirable() {
        return AseTestBridge.currentAct.getComponentName().flattenToString().equals(currentActivityName);
    }

    @Override
    public void injectEvent() {
        //AseTestBridge.currentAct.onNavigateUp();

        MenuItem item = new HomeMenuItem();
        ((HomeMenuItem)item).setItemId(android.R.id.home);
        AseTestBridge.currentAct.onOptionsItemSelected(item);
        Log.i("Repeater", "Navigated to up: " + Integer.toHexString(viewId));

    }

    public static class HomeMenuItem implements MenuItem {

        int itemId;

        public void setItemId(int id) {
            itemId = id;
        }

        @Override
        public int getItemId() {
            return itemId;
        }

        @Override
        public int getGroupId() {
            return 0;
        }

        @Override
        public int getOrder() {
            return 0;
        }

        @Override
        public MenuItem setTitle(CharSequence charSequence) {
            return null;
        }

        @Override
        public MenuItem setTitle(int i) {
            return null;
        }

        @Override
        public CharSequence getTitle() {
            return null;
        }

        @Override
        public MenuItem setTitleCondensed(CharSequence charSequence) {
            return null;
        }

        @Override
        public CharSequence getTitleCondensed() {
            return null;
        }

        @Override
        public MenuItem setIcon(Drawable drawable) {
            return null;
        }

        @Override
        public MenuItem setIcon(int i) {
            return null;
        }

        @Override
        public Drawable getIcon() {
            return null;
        }

        @Override
        public MenuItem setIntent(Intent intent) {
            return null;
        }

        @Override
        public Intent getIntent() {
            return null;
        }

        @Override
        public MenuItem setShortcut(char c, char c1) {
            return null;
        }

        @Override
        public MenuItem setNumericShortcut(char c) {
            return null;
        }

        @Override
        public char getNumericShortcut() {
            return 0;
        }

        @Override
        public MenuItem setAlphabeticShortcut(char c) {
            return null;
        }

        @Override
        public char getAlphabeticShortcut() {
            return 0;
        }

        @Override
        public MenuItem setCheckable(boolean b) {
            return null;
        }

        @Override
        public boolean isCheckable() {
            return false;
        }

        @Override
        public MenuItem setChecked(boolean b) {
            return null;
        }

        @Override
        public boolean isChecked() {
            return false;
        }

        @Override
        public MenuItem setVisible(boolean b) {
            return null;
        }

        @Override
        public boolean isVisible() {
            return false;
        }

        @Override
        public MenuItem setEnabled(boolean b) {
            return null;
        }

        @Override
        public boolean isEnabled() {
            return false;
        }

        @Override
        public boolean hasSubMenu() {
            return false;
        }

        @Override
        public SubMenu getSubMenu() {
            return null;
        }

        @Override
        public MenuItem setOnMenuItemClickListener(OnMenuItemClickListener onMenuItemClickListener) {
            return null;
        }

        @Override
        public ContextMenu.ContextMenuInfo getMenuInfo() {
            return null;
        }

        @Override
        public void setShowAsAction(int i) {

        }

        @Override
        public MenuItem setShowAsActionFlags(int i) {
            return null;
        }

        @Override
        public MenuItem setActionView(View view) {
            return null;
        }

        @Override
        public MenuItem setActionView(int i) {
            return null;
        }

        @Override
        public View getActionView() {
            return null;
        }

        @Override
        public MenuItem setActionProvider(ActionProvider actionProvider) {
            return null;
        }

        @Override
        public ActionProvider getActionProvider() {
            return null;
        }

        @Override
        public boolean expandActionView() {
            return false;
        }

        @Override
        public boolean collapseActionView() {
            return false;
        }

        @Override
        public boolean isActionViewExpanded() {
            return false;
        }

        @Override
        public MenuItem setOnActionExpandListener(OnActionExpandListener onActionExpandListener) {
            return null;
        }
    }
}


// remove id from events?