package ase.scheduler;

import java.util.Stack;

import android.util.Log;
import ase.util.LooperReader;

// provides synchronization of its corresponding Thread

public class ThreadData {

    private long id;
    public static long SCHEDULER_ID = -1;

    private int currentMonitors = 0;
    // Each waitMyTurn inserts an item stating whether its corresponding
    // notifyScheduler will notify
    // Notification of scheduler is allowed only when popped item is true
    // This handles two cases:
    // (1) Nested wait-notify blocks (notify scheduler when exiting last block)
    // (2) Threads not suspended (did not wait) since it had monitors
    private Stack<Boolean> willNotify = new Stack<Boolean>();

    // if this is true once in a nested chunk of blocks
    // the inner blocks will not notify
    private boolean isWaiting = false;
    // Not used for SchedulerThread

    private Thread thread;

    /*
     * public ThreadData(long id){ this.id = id; }
     */

    public ThreadData(long id, Thread t) {
        this.id = id;
        this.thread = t;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        if(thread!=null)
            return thread.getName();
       Log.e("MyScheduler", "Reading name of a null-thread");
       return null;
    }

    public synchronized void notifyThread() {
        this.notify();
    }

    public synchronized void waitThread() {
        try {
            this.wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void enteredMonitor() {
        currentMonitors++;
    }

    public void exitedMonitor() {
        currentMonitors--;
    }

    public int getCurrentMonitors() {
        // Log.v("MyScheduler", "Monitors: " + currentMonitors );
        return currentMonitors;
    }

    public void pushWaitBlock(boolean b) {
        willNotify.push(Boolean.valueOf(b));
    }

    public boolean popWaitBlock() {
        if (!willNotify.empty()) {
            Boolean b = willNotify.pop();
            return b.booleanValue();
        }
        return false;
    }

    public boolean isInBlock() {
        if (willNotify.isEmpty())
            return false;
        else
            return true;
    }

    public boolean isWaiting() {
        return isWaiting;
    }

    public void setIsWaiting(boolean b) {
        isWaiting = b;
    }

    public boolean hasMsgToHandle() {
        return !LooperReader.getInstance().hasEmptyLooper(thread);
    }

}
