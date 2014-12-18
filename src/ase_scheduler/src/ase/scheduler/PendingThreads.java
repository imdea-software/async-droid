package ase.scheduler;

import android.util.Log;

import java.util.*;

/* 
 * keeps the threads registered to the scheduler
 */
public class PendingThreads {
    private List<ThreadData> threads = new ArrayList<ThreadData>();
    private int walkerIndex = -1; // index of the threadData to be scheduled
                                  // next, updated when a thread terminates

    private Set<String> defaultThreadNames = null;

    public synchronized boolean isEmpty() {
        return threads.isEmpty();
    }

    public synchronized int getSize() {
        return threads.size();
    }

    /*
    * Collect application's user threads
    */
    public void captureAllThreads() {
        Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
        for (Thread t : threadSet) {
            if (!t.getName().equalsIgnoreCase("SchedulerThread")
                    && isUserThread(t))
                captureThread(t); // no need to send info from other threads?
        }
    }

    /*
    * Send current thread info to the scheduler This method is called by the
    * scheduler to register existing threads
    */
    public void captureThread(Thread thisThread) {
        long id = thisThread.getId();
        if (!capturedBefore(id)) {
            addThread(new ThreadData(id, thisThread));
            Log.v("MyScheduler", "Added: " + thisThread.getName() + " Id: " + thisThread.getId());
        }
    }

    public synchronized void addThread(ThreadData td) {
        if(!capturedBefore(td.getId())){
            threads.add(td);
            sortByName();
        }
    }

    public synchronized boolean capturedBefore(long id) {
        for (ThreadData td : threads) {
            if (td.getId() == id)
                return true;
        }
        return false;
    }

    public Set<String> getDefaultThreadNames() {
        if (defaultThreadNames == null) {
            defaultThreadNames = new HashSet<>();
            defaultThreadNames.add("GC");
            defaultThreadNames.add("Signal Catcher");
            defaultThreadNames.add("JDWP");
            defaultThreadNames.add("Compiler");
            defaultThreadNames.add("ReferenceQueueDaemon");
            defaultThreadNames.add("FinalizerDaemon");
            defaultThreadNames.add("FinalizerWatchdogDaemon");
            defaultThreadNames.add("SchedulerThread");
        }
        return defaultThreadNames;
    }

    /*
 * false for default application threads (except for the main thread)
 */
    public boolean isUserThread(Thread t) {
        String name = t.getName();
        return !(getDefaultThreadNames().contains(name) || name.startsWith("Binder"));
    }

    public synchronized void increaseWalker() {
        if (threads.size() >= 1)
            walkerIndex = (walkerIndex + 1) % threads.size();
        else
            walkerIndex = (walkerIndex + 1);
    }

    public synchronized void decreaseWalker() {
        if (threads.size() > 0)
            walkerIndex = (walkerIndex - 1 + threads.size()) % threads.size();
        else
            walkerIndex = -1;
    }

    public void clear() {
        threads.clear();
        walkerIndex = -1;
    }

    public void sortByName() {
        Collections.sort(threads, new Comparator<ThreadData>() {
            @Override
            public int compare(ThreadData t1, ThreadData t2) {
                return t1.getName().compareTo(t2.getName());
            }
        });
    }

    public synchronized int getWalkerIndex() {
        return walkerIndex;
    }

    public synchronized ThreadData getThreadByIndex(int index) {
        return threads.get(index);
    }

    public synchronized ThreadData getCurrentThread() {
        return getThreadByIndex(walkerIndex);
    }

    public synchronized ThreadData getThreadById(long id) {
        for (ThreadData td : threads) {
            if (td.getId() == id) {
                return td;
            }
        }
        return null;
    }

    public String toString() {
        String s = "Pending threads: ";

        for (ThreadData t : threads)
            s = s.concat(" " + t);

        return s;
    }

    /*
    public synchronized void removeThread(ThreadData td) {
        threads.remove(td);
    }

    public synchronized ThreadData removeThreadById(long id) {
        threads.remove(getThreadById(id));
        decreaseWalker();
        return null;
    }
    */

}
