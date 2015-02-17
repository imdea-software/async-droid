package ase.scheduler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import android.os.AsyncTask;
import android.os.Message;
import ase.repeater.InputRepeater;
import ase.scheduler.PendingThreads.ThreadType;
import ase.util.FileUtils;
import ase.util.IOFactory;
import ase.util.LooperReader;
import ase.util.Logger;

public abstract class Scheduler {

    protected PendingThreads threads;
    protected InputRepeater inputRepeater;    
    protected ThreadData scheduledThread;

    protected int taskToProcess = 0;
    
    public Scheduler (PendingThreads threads, InputRepeater inputRepeater) {
        this.threads = threads;
        this.inputRepeater = inputRepeater;
    }
    
    public abstract void initiateScheduler(int bound, int numInputs);
    
    public abstract void setUpTestCase();
    
    public abstract void tearDownTestCase();

    public abstract boolean isEndOfTestCase();
    
    public abstract boolean hasMoreTestCases();
    
    public abstract void doOnPreScheduling();
    
    public abstract ThreadData selectNextThread();

    /**
     * Check if the currently visited thread will be scheduled:
     *  - isWaiting:  (executed waitMyTurn() and will notify (was not in monitor))
     *  - is InputRepeater and has inputs to post
     *  - hasMsgToHandle: has a non-empty message queue
     * Keep the number of blocks for the main thread instead of hasMsgToHandle
     * (if you allow anytime when UI has sth in its message queue
     * it has sth internal and does not notify the scheduler, all threads do wait!!)
     */
    protected boolean okToSchedule(ThreadData current){
        if(current == null) return false;
                    
        if(current.getId() == 1 && inputRepeater.hasEventsToHandle())
            return true;
        
        // if the InputRepeater has input to post to main
       if(current.getName().equalsIgnoreCase("InputRepeater") && inputRepeater.readyToInjectInput())
           return true;

        // if a looper thread has a non-empty message queue (may not yet executed waitMyTurn())
        if(current.getId() != 1 && current.hasMsgToHandle())
            return true;

        // if already went into waitMyTurn() and will notify (was not in monitor)
        if(current.isWaiting() && !current.getName().equalsIgnoreCase("InputRepeater"))
            return true;
            
        // add stmt to AsyncTasks - if executor has tasks or thread is active
        return false;
    }

    public boolean hasAvailableThreads() {
        for(int i=0; i<threads.getSize(); i++) {
            if(okToSchedule(threads.getThreadByIndex(i, ThreadType.ANY)))
                    return true;
        }
        return false;
    }
    
    protected int numInputsInMainLooper() {
        return inputRepeater.numEventsToHandle();
    }
    
    protected void refreshThreadList() {
        threads.captureAllThreads();
        threads.sortThreadsByName();
    }

    //TODO take asyncTask type as parameter (e.g. do not count onPublishProgress's ..)
    //TODO I assume all tasks to examine are instrumented
    // removed reading messages of the main looper
    protected int numAsyncTasksInMainLooper() {
        List<Message> messages = LooperReader.getInstance().getMessages(threads.getThreadById(1).getThread());
        int count = 0;
        // Main looper has an asynctask message - PostResult(onPostExecute or onCancalled) or PostProgress(onPublishProgress) 
        for(Message m: messages) {
            if(m.getTarget() != null && m.getTarget().getClass().getName().startsWith("android.os.AsyncTask") && (m.what == 1 || m.what == 2))
                count ++;
        }
        return count;
    }
    
    /**
     * @return the number of active asyncTask threads (on serial executor and on thread pool executor)
     */
    protected int getAsyncTaskPoolActiveCount() {
        ThreadPoolExecutor executor = ((ThreadPoolExecutor)AsyncTask.THREAD_POOL_EXECUTOR);
        if(executor != null) {
            return executor.getActiveCount();
        }
        return 0;
    }
    
    protected BlockingQueue getAsyncTaskPoolQueue() {
        ThreadPoolExecutor executor = ((ThreadPoolExecutor)AsyncTask.THREAD_POOL_EXECUTOR);
        if(executor != null) {
            return executor.getQueue();
        }
        return null;
    }
        
    protected boolean isOnSerialExecutor(Thread t) {
        if(t == null) return false;
        
        StackTraceElement[] calls = t.getStackTrace();
        for(StackTraceElement call: calls) {
            if(call.toString().contains("android.os.AsyncTask$SerialExecutor")){
                return true;
            }
        }
        return false;
    }
    
    protected boolean isOnThreadPoolExecutor(Thread t) {
        StackTraceElement[] calls = t.getStackTrace();
        for(StackTraceElement call: calls) {
            if(call.toString().contains("android.os.AsyncTask$SerialExecutor")){
                return false;
            }
        }
        return true;
    }
    
    protected void logThreads(ThreadData current) {
        Thread main = threads.getThreadById(1).getThread();
        //logger.i("Main Looper Contents:", LooperReader.getInstance().dumpQueue(main));
        //logger.i("RRScheduler", threads.toString()
        
        // get the stats: How many tasks do each thread have?
        int numUIThreadTasks = numInputsInMainLooper() + numAsyncTasksInMainLooper();
        int numAllUIThreadTasks = LooperReader.getInstance().getMessages(main).size();
                
        int numInputTasks = inputRepeater.numInputsLeft();
        
        //int numAsyncSerialTasks = ReflectionUtils.getAsyncTaskSerialExecutorTasks().size();
        //boolean isSerialActive = ReflectionUtils.isAsyncTaskSerialThreadActive();
        //if(isSerialActive) numAsyncSerialTasks ++;
        
        int numAsyncPoolTasks = getAsyncTaskPoolQueue().size() + getAsyncTaskPoolActiveCount();
        //if(isSerialActive) numAsyncPoolTasks --;
        
        Map<Long, Integer> numHandlerThreadTasks = new HashMap<Long, Integer>();
        Object[] handlerThreads = threads.getThreads(ThreadType.HANDLERTHREAD);
        for(int i=0; i<handlerThreads.length; i++) {
            ThreadData td = (ThreadData) handlerThreads[i];
            int taskCount = LooperReader.getInstance().getMessages(td.getThread()).size();
            if(td.isWaiting()) taskCount++;
            // if isActive, increment
            numHandlerThreadTasks.put(td.getId(), taskCount);
        }
        
        TestData runData = new TestData(numInputTasks, numUIThreadTasks, numAllUIThreadTasks, numAsyncPoolTasks);
        FileUtils.appendObject(IOFactory.STATS_FILE, runData);

        Logger.v("Stat", " " + numInputTasks + " " + numUIThreadTasks + " "  + numAllUIThreadTasks + " " + numAsyncPoolTasks);
    }
}
