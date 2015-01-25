package ase.scheduler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import android.os.AsyncTask;
import android.util.Log;
import ase.repeater.InputRepeater;
import ase.scheduler.PendingThreads.ThreadType;
import ase.util.LooperReader;
import ase.util.ReflectionUtils;
import ase.util.log.Logger;

public class RRScheduler extends Scheduler {   
    private int taskToProcess = 1;
    
    private DelaySequence delaySeq;
    boolean isDelaying = true;
    int numDelays;
    int numCompletedTests = 0;
    
    private int idleTypes = 0;
    // default schedule: InputRepeater MainThread AsyncTaskSerialThread AsyncTaskPoolThreads HandlerThreads
    private int typeToSchedule = 0;
    private ThreadType[] types = {ThreadType.INPUTREPEATER, ThreadType.MAIN, ThreadType.ASYNCTASK_SERIAL, ThreadType.ASYNCTASK_POOL, ThreadType.HANDLERTHREAD};
    boolean onPool= false;
    private int handlerThreadIndex = 0;
    private int poolThreadIndex = 0;
    
    public RRScheduler(PendingThreads threads, InputRepeater inputRepeater, Logger logger) {
        super(threads, inputRepeater, logger);
    }
    
    @Override
    public void initiateScheduler(int bound, int inputSize) {      
        numDelays = bound;
    }

    @Override
    public void setUpTestCase() {
        taskToProcess = 1;
        if(delaySeq != null) { // null for the first test
            delaySeq.next();
            Log.i("DelayInfo", "Current delay indices:" + delaySeq.toString());
        }     
    }
        
    @Override
    public boolean isEndOfTestCase() {
        return !hasAvailableThreads() && (taskToProcess > 1);
    }
    
    @Override
    public boolean hasMoreTestCases() {
        if(numCompletedTests == 0) return true;
        if(isDelaying && numDelays > 0) {
            if(numCompletedTests == 1) { // first test without any delays
                delaySeq = new DelaySequence(numDelays, taskToProcess-1 );
                return true;
            }else {
                return delaySeq.hasNext();
            }          
        }
        return false;
    }
 
    /**
     * Returns the thread to be scheduled
     * Returns null if no threads is scheduled
     */
    @Override
    public ThreadData selectNextThread() {
        idleTypes = 0;
        refreshThreadList();
        
        // current is the next thread of type to schedule
        ThreadData current = getNextThread(types[typeToSchedule]);
        logThreads(current);
        
        // if current thread is not okToSchedule, get the first available one
        while(!okToSchedule(current) && idleTypes < types.length) {        
            idleTypes ++;
            current = getNextThread(types[typeToSchedule]);
            logThreads(current);
        }
        
        // added
        if(!okToSchedule(current) && (idleTypes == types.length)) return null;
        
        // check if current will be delayed, if so delay
        if(current != null && taskToProcess == getNextTaskIndexToDelay()) { 
            Log.i("AseScheduler", "Delayed Thread Id: " + current.getId() + " Last Processed: " + taskToProcess);
            Log.i("DelayInfo", "Consumed delay: " + taskToProcess);
            delaySeq.spendCurrentDelayIndex();
            taskToProcess ++;
            return selectNextThread(); // terminates since delaySeq is not infinite
        } 
        
        // current is the task to be dispatched
        // current is null if no task is selected, i.e. it is the end of the test
        
        // get the stats: How many tasks do each thread have?
        int numMainTasks = numInputsInMainLooper() + numAsyncTasksInMainLooper();
        if(threads.getThreadById(1).isWaiting()) numMainTasks++;
        
        int numInputTasks = inputRepeater.numInputsLeft();
        if(threads.getThreadByName("InputRepeater").isWaiting()) numInputTasks++;
        
        int numAsyncSerialTasks = ReflectionUtils.getAsyncTaskSerialExecutorTasks().size();
        boolean isSerialActive = ReflectionUtils.isAsyncTaskSerialThreadActive();
        if(isSerialActive) numAsyncSerialTasks ++;
        
        int numAsyncPoolTasks = getAsyncTaskPoolQueue().size() + getAsyncTaskPoolActiveCount();
        if(isSerialActive) numAsyncPoolTasks --;
        
        Map<Long, Integer> numHandlerThreadTasks = new HashMap<Long, Integer>();
        Object[] handlerThreads = threads.getThreads(ThreadType.HANDLERTHREAD);
        for(int i=0; i<handlerThreads.length; i++) {
            ThreadData td = (ThreadData) handlerThreads[i];
            int taskCount = LooperReader.getInstance().getMessages(td.getThread()).size();
            if(td.isWaiting()) taskCount++;
            // if isActive, increment
            numHandlerThreadTasks.put(td.getId(), taskCount);
        }
        
        Log.v("Stat", " " + numMainTasks + " " + numInputTasks + " " + numAsyncSerialTasks + " " + numAsyncPoolTasks);
        logger.i("Stat", " " + numMainTasks + " " + numInputTasks + " " + numAsyncSerialTasks + " " + numAsyncPoolTasks);
     
        Log.v("RRScheduler", "Scheduled: " + current.getName() + " Task to process: " + taskToProcess);
        logger.i("RRScheduler", "Scheduled " + current.getName() + " Task to process: " + taskToProcess);
        
        taskToProcess ++;  
        return current;
    }

    /**
     * @return okTOSchedule thread of type if exists, null otherwise
     */
    private ThreadData getNextThread(ThreadType type) {
        ThreadData current = null;
        
        //logger.i("RRScheduler", "Current type: " + type);
        //Log.w("RRScheduler", "Current type: " + type);
        
        if(types[typeToSchedule].equals(ThreadType.INPUTREPEATER)) {
            current = threads.getThreadByName("InputRepeater");  
            typeToSchedule = (typeToSchedule + 1) % types.length;
            
        } else if(types[typeToSchedule].equals(ThreadType.MAIN)) {
            current = threads.getThreadById(1);
            typeToSchedule = (typeToSchedule + 1) % types.length;
            
        } else if(types[typeToSchedule].equals(ThreadType.ASYNCTASK_SERIAL)) {
            current = getSerialAsyncTaskThread();
            typeToSchedule = (typeToSchedule + 1) % types.length;
            
        } else if(types[typeToSchedule].equals(ThreadType.ASYNCTASK_POOL)) {
            current = getNextAsyncTaskPoolThread();
            if(current == null) {
                typeToSchedule = (typeToSchedule + 1) % types.length;
            }
           
        } else if(types[typeToSchedule].equals(ThreadType.HANDLERTHREAD)) {
            current = getNextHandlerThread();
            if(current == null) {
                typeToSchedule = (typeToSchedule + 1) % types.length;
            }
        }   
        
        return current;
    }
    
    /**
     * Gets the okToSchedule AsyncTask thread 
     * that runs on the serial executor
     */
    private ThreadData getSerialAsyncTaskThread() {
        ThreadData selected = null;
        do {
            refreshThreadList();
            Object[] asyncTaskThreads = threads.getThreads(ThreadType.ASYNCTASK);
            for(Object o: asyncTaskThreads) {
                ThreadData td = (ThreadData) o;
                if(okToSchedule(td) && isOnSerialExecutor(td.getThread())) {
                    selected = td;
                    break;
                }
            }
        } while (selected == null && !ReflectionUtils.getAsyncTaskSerialExecutorTasks().isEmpty());
         
        return selected;
    }

    /**
     * Gets the next okToSchedule AsyncTask thread (indexed by poolThreadIndex)
     * that does not run on the serial executor
     */
    private ThreadData getNextAsyncTaskPoolThread() {
        int size = threads.getThreads(ThreadType.ASYNCTASK).length;
        ThreadData current = null;

        while(!okToSchedule(current) && poolThreadIndex < size) {
            current = threads.getThreadByIndex(poolThreadIndex, ThreadType.ASYNCTASK);
            poolThreadIndex ++;
            
            // skip if the current AsyncTask thread runs on the serial executor
            if((current != null) && isOnSerialExecutor(current.getThread())) 
                continue;
        } 
        if(!okToSchedule(current) && poolThreadIndex == size) {
            poolThreadIndex = 0;
            return null; // no available threads are found
        }
        return current;
    }
    
    /**
     * Gets the next okToSchedule HandlerThread (indexed by handlerThreadIndex)
     */
    private ThreadData getNextHandlerThread() {
        int size = threads.getThreads(ThreadType.HANDLERTHREAD).length;
        ThreadData current = null;
        
        while(!okToSchedule(current) && handlerThreadIndex < size) {
            current = threads.getThreadByIndex(handlerThreadIndex, ThreadType.HANDLERTHREAD);
            handlerThreadIndex ++;
        } 
        if(!okToSchedule(current) && handlerThreadIndex == size) {
            handlerThreadIndex = 0;
            return null; // no available threads are found
        }
        return current;
    }
    
    /**
     * @return the number of active asyncTask threads (on serial executor and on thread pool executor)
     */
    private int getAsyncTaskPoolActiveCount() {
        ThreadPoolExecutor executor = ((ThreadPoolExecutor)AsyncTask.THREAD_POOL_EXECUTOR);
        if(executor != null) {
            return executor.getActiveCount();
        }
        return 0;
    }
    
    private BlockingQueue getAsyncTaskPoolQueue() {
        ThreadPoolExecutor executor = ((ThreadPoolExecutor)AsyncTask.THREAD_POOL_EXECUTOR);
        if(executor != null) {
            return executor.getQueue();
        }
        return null;
    }
        
    private boolean isOnSerialExecutor(Thread t) {
        if(t == null) return false;
        
        StackTraceElement[] calls = t.getStackTrace();
        for(StackTraceElement call: calls) {
            if(call.toString().contains("android.os.AsyncTask$SerialExecutor")){
                return true;
            }
        }
        return false;
    }
    
    private boolean isOnThreadPoolExecutor(Thread t) {
        StackTraceElement[] calls = t.getStackTrace();
        for(StackTraceElement call: calls) {
            if(call.toString().contains("android.os.AsyncTask$SerialExecutor")){
                return false;
            }
        }
        return true;
    }
    
    private void refreshThreadList() {
        threads.captureAllThreads();
        threads.sortThreadsByName();
    }
    
    private void logThreads(ThreadData current) {
      //List<Message> mainLooperMessages =  LooperReader.getInstance().getMessages(threads.getThreadById(1).getThread());
        logger.i("Main Looper Contents:", LooperReader.getInstance().dumpQueue(threads.getThreadById(1).getThread()));
        logger.i("RRScheduler", threads.toString());
        if(current != null) {
            logger.i("RRScheduler", "Current: " + current.getName() + " Next Task#: " + taskToProcess);
            Log.v("RRScheduler", "Current: " + current.getName() + " Next Task#: " + taskToProcess);
        }
    }
    
    private int getNextTaskIndexToDelay() {
        if (numCompletedTests == 0) return -1;
        return delaySeq.getNextDelayIndex();
    }
/*
    public synchronized void increaseWalker() {
        if (threads.getSize() >= 1)
            walkerIndex = (walkerIndex + 1) % threads.getSize();
        else
            walkerIndex = (walkerIndex + 1);
    }

    public synchronized void decreaseWalker() {
        if (threads.getSize() > 0)
            walkerIndex = (walkerIndex - 1 + threads.getSize()) % threads.getSize();
        else
            walkerIndex = -1;
    }
    
    public synchronized int getWalkerIndex() {
        return walkerIndex;
    }*/
    
    @Override
    public void tearDownTestCase() {
        numCompletedTests ++;     
    }
}
