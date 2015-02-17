package ase.scheduler;

import android.util.Log;
import ase.repeater.InputRepeater;
import ase.scheduler.PendingThreads.ThreadType;
import ase.util.LooperReader;
import ase.util.ReflectionUtils;
import ase.util.Logger;

public class RRScheduler extends Scheduler {    
    private DelaySequence delaySeq;
    boolean isDelaying = true;
    int numDelays;
    int numCompletedTests = 0;
    
    private int idleTypes = 0;
    // default schedule: AsyncTaskSerialThread AsyncTaskPoolThreads HandlerThreads InputRepeater MainThread 
    private int nextTypeToSchedule = 0;
    private ThreadType[] types = {ThreadType.INPUTREPEATER, ThreadType.MAIN, ThreadType.ASYNCTASK_SERIAL, ThreadType.ASYNCTASK_POOL, ThreadType.HANDLERTHREAD};
    boolean onPool= false;
    private int handlerThreadIndex = 0;
    private int poolThreadIndex = 0;
    
    private ThreadData prevThread = scheduledThread = null;
    
    public RRScheduler(PendingThreads threads, InputRepeater inputRepeater) {
        super(threads, inputRepeater);
    }
    
    @Override
    public void initiateScheduler(int bound, int inputSize) {      
        numDelays = bound;
    }

    @Override
    public void setUpTestCase() {
        taskToProcess = 0;
        nextTypeToSchedule = 0;
        if(delaySeq != null) { // null for the first test
            delaySeq.next();
            Logger.i("DelayInfo", "Current delay indices:" + delaySeq.toString());
        }  
        refreshThreadList();
        prevThread = scheduledThread = null;
        // run the main thread until there are no more messages
        runMainToCompletionOrToWait();
    }
        
    public synchronized void runMainToCompletionOrToWait() {
        ThreadData td = threads.getThreadById(1);
        
        Thread t = td.getThread();
        boolean empty = false;
        while (!empty && !td.isWaiting()) {
            try {
                this.wait(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //String contents = LooperReader.getInstance().dumpQueue(t);
            empty = LooperReader.getInstance().hasEmptyLooper(t);
            //Log.i("contents:", contents);
        }
    }
    
    @Override
    public boolean isEndOfTestCase() {
        return !hasAvailableThreads() && !inputRepeater.hasEventsToHandle() && !inputRepeater.hasMoreInputs() && (numAsyncTasksInMainLooper() == 0) && (taskToProcess > 1);
    }
    
    @Override
    public boolean hasMoreTestCases() {
        if(numCompletedTests == 0) 
            return true;
        
        if(isDelaying && numDelays > 0) {
            if(numCompletedTests == 1) { // first test without any delays
                delaySeq = new DelaySequence(numDelays, taskToProcess + 3);
                return true;
            }else {
                return delaySeq.hasNext();
            }          
        }
        return false;
    }
 
    @Override
    public void doOnPreScheduling() {
        // if the prev thread was the main thread, execute all tasks in its message queue up to wait
        if(scheduledThread != null && scheduledThread.getId()==1) {
            runMainToCompletionOrToWait();
        }   
    }
    
    /**
     * Returns the thread to be scheduled
     * Returns null if no threads is scheduled
     */
    @Override
    public ThreadData selectNextThread() {
       
        ThreadData current = getNextThread();
        logThreads(current);
            
        // current is the task to be dispatched
        scheduledThread = current;
        
        if(prevThread != scheduledThread && scheduledThread != null) {
            taskToProcess ++;
            prevThread = current;
        }
        
        // check if current will be delayed, if so delay
        if(current != null && taskToProcess == getNextTaskIndexToDelay()) { 
            Logger.i("RRScheduler", "Delayed " + current.getName() + " Consumed Task to process: " + taskToProcess);
            Log.i("DelayInfo", "Consumed delay: " + taskToProcess);
            delaySeq.spendCurrentDelayIndex();       
            nextTypeToSchedule = (nextTypeToSchedule + 1) % types.length;
            Log.i("RRScheduler","Moved to next type: " + types[nextTypeToSchedule]);
            current = getNextThread();
        } 
        
        if(current != null) {
            Logger.i("RRScheduler", "Scheduled: " + current.getName() + " Task to process: " + taskToProcess);
        }

        return current;
    }


    public ThreadData getNextThread() {
        idleTypes = 0;
        refreshThreadList();
            
        // current is the next thread of type to schedule
        ThreadData current = getNextThreadOfType(types[nextTypeToSchedule]);
        //logThreads(current);
        
        // if current thread is not okToSchedule, get the next available thread
        while(!okToSchedule(current) && idleTypes < types.length) { 
            idleTypes ++;
            nextTypeToSchedule = (nextTypeToSchedule + 1) % types.length;
            // Log.v("Scheduler","Next type: " + types[nextTypeToSchedule]);
            current = getNextThreadOfType(types[nextTypeToSchedule]);
        }
        
        if(!okToSchedule(current) && (idleTypes == types.length)) return scheduledThread = null;
        
        return current;
    }
    
    /**
     * @return okTOSchedule thread of type if exists, null otherwise
     */
    private ThreadData getNextThreadOfType(ThreadType type) {
        ThreadData current = null;
        
        //logger.i("RRScheduler", "Current type: " + type);
        Log.v("RRScheduler", "Current type: " + type);
        
        if(types[nextTypeToSchedule].equals(ThreadType.INPUTREPEATER)) {
            current = threads.getThreadByName("InputRepeater");    
            
        } else if(types[nextTypeToSchedule].equals(ThreadType.MAIN)) {
            current = threads.getThreadById(1);            
            
        } else if(types[nextTypeToSchedule].equals(ThreadType.ASYNCTASK_SERIAL)) {
            current = getSerialAsyncTaskThread();
            
        } else if(types[nextTypeToSchedule].equals(ThreadType.ASYNCTASK_POOL)) {
            current = getNextAsyncTaskPoolThread();
           
        } else if(types[nextTypeToSchedule].equals(ThreadType.HANDLERTHREAD)) {
            current = getNextHandlerThread();
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
                current = null; // do not schedule serial AsyncTask
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
         
    private int getNextTaskIndexToDelay() {
        if (numCompletedTests == 0) return -1;
        return delaySeq.getNextDelayIndex();
    }
    
    @Override
    public void tearDownTestCase() {
        numCompletedTests ++;     
    }
}
