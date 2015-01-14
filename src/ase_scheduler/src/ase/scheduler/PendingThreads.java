package ase.scheduler;

import android.os.HandlerThread;
import android.util.Log;

import java.util.*;

/* 
 * keeps the threads registered to the scheduler
 */
public class PendingThreads {
    
    public enum ThreadType {
        MAIN, INPUTREPEATER, ASYNCTASK, HANDLERTHREAD, ANY
    }
    
    // allThreads include the main, InputRepeater, AsyncTask and HandlerThreads
    private ThreadList allThreads = new ThreadList();
    private ThreadList asyncTaskThreads = new ThreadList();
    private ThreadList handlerThreads = new ThreadList();
    
    private Set<String> defaultThreadNames = null;

    /*
    * Collect application's user threads
    */
    public void captureAllThreads() {
        Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
        for (Thread t : threadSet) {
            if (isThreadToBeControlled(t))
                captureThread(t); 
        }
    }

    /*
    * Send current thread info to the scheduler This method is called by the
    * scheduler to register existing threads
    */
    public void captureThread(Thread thisThread) {
        long id = thisThread.getId();
        if (!allThreads.isInList(id)) {
            ThreadData td = new ThreadData(id, thisThread);
            allThreads.addThread(td);
            
            if(td.getName().startsWith("AsyncTask"))
                asyncTaskThreads.addThread(td);
            else if(thisThread instanceof HandlerThread)
                handlerThreads.addThread(td);
            
            Log.v("AseScheduler", "In list: " + thisThread.getName() + " Id: " + thisThread.getId());    
        }
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
     * False for default application threads (except for the main thread)
     */
    public boolean isUserThread(Thread t) {
        String name = t.getName();
        return !(getDefaultThreadNames().contains(name) || name.startsWith("Binder"));
    }
    
    /*
     * The scheduler manages the execution of:
     * Main thread, AsyncTask threads and HandlerThreads
     * together with the InputRepeater thread
     */
    public boolean isThreadToBeControlled(Thread t) {
        return isUserThread(t) && (t.getId() == 1 || t.getName().startsWith("AsyncTask") || 
                t.getName().equalsIgnoreCase("InputRepeater") || (t instanceof HandlerThread) );
    }
    
    public void clearThreads() {
        allThreads.clear();
        asyncTaskThreads.clear();
        handlerThreads.clear();
    }
   
    public void sortThreadsByName() {
        allThreads.sortThreadsByName();
        asyncTaskThreads.sortThreadsByName();
        handlerThreads.sortThreadsByName();
    }
    
    public void sortThreadsById() {
        allThreads.sortThreadsById();
        asyncTaskThreads.sortThreadsById();
        handlerThreads.sortThreadsById();
    }
    
    public ThreadData getThreadById(long id) {
        return allThreads.getThreadById(id);
    }
    
    public ThreadData getThreadByIndex(int index, ThreadType type) {
        switch(type) {
        case ANY:
            return allThreads.getThreadByIndex(index);
        case ASYNCTASK:
            return asyncTaskThreads.getThreadByIndex(index);
        case HANDLERTHREAD:
            return handlerThreads.getThreadByIndex(index);
        case MAIN:
            return allThreads.getThreadById(1);
        case INPUTREPEATER:
            return allThreads.getThreadByName("InputRepeater");  
        }
        return null;
    }
    
    public ThreadData getThreadByName(String name) {
        return allThreads.getThreadByName(name);
    }
    
    public int getSize() {
        return allThreads.getSize();
    }
    
    public int getSize(ThreadType type) {
        switch(type) {
        case ANY:
            return allThreads.getSize();
        case ASYNCTASK:
            return asyncTaskThreads.getSize();
        case HANDLERTHREAD:
            return handlerThreads.getSize();
        case MAIN: 
        case INPUTREPEATER:
            return 1;
        }
        return 0;
    }

    public String toString() {
        return "Pending threads: " + allThreads;
    }

    private static class ThreadList {
        private List<ThreadData> threads; 
        
        public ThreadList() {
            threads = new ArrayList<ThreadData>();
        }
        
        public void clear() {
            threads.clear();
        }

        @SuppressWarnings("unused")
        public synchronized boolean isEmpty() {
            return threads.isEmpty();
        }

        public synchronized int getSize() {
            return threads.size();
        }

        public synchronized boolean isInList(long id) {
            for (ThreadData td : threads) {
                if (td.getId() == id)
                    return true;
            }
            return false;
        }
        
        public synchronized void addThread(ThreadData td) {
                threads.add(td);
        }
        
        public synchronized void sortThreadsByName() {
            Collections.sort(threads, new Comparator<ThreadData>() {
                @Override
                public int compare(ThreadData t1, ThreadData t2) {
                    return t1.getName().compareTo(t2.getName());
                }
            });
        }
        
        public synchronized void sortThreadsById() {
            Collections.sort(threads, new Comparator<ThreadData>() {
                @Override
                public int compare(ThreadData t1, ThreadData t2) {
                    if (t1.getId() < t1.getId())
                        return -1;
                    if (t1.getId() > t1.getId())
                        return 1;
                    return 0;
                }
            });
        }
        
        public synchronized ThreadData getThreadByIndex(int index) {
            if(threads.size() > index)
                return threads.get(index);
            return null;
        }

        public synchronized ThreadData getThreadById(long id) {
            for (ThreadData td : threads) {
                if (td.getId() == id) {
                    return td;
                }
            }
            return null;
        }
        
        public synchronized ThreadData getThreadByName(String name) {
            for (ThreadData td : threads) {
                if (td.getName().equalsIgnoreCase(name)) {
                    return td;
                }
            }
            return null;
        }
        
        public String toString() {
            String s = "";

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

}
