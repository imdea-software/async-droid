package ase.scheduler;

import android.util.Log;
import ase.repeater.InputRepeater;
import ase.scheduler.PendingThreads.ThreadType;
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
    private int consecutiveMainTasks = 0;
    // MAX_CONSECUTIVEMAINTASKS can be modified depending on the nature of the app under test in the programmer implemented scheduler
    private final int MAX_CONSECUTIVEMAINTASKS = 5; // allows for more if there are events to handle or asynctasks in it
    
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
    
    @Override
    public boolean isEndOfTestCase() {
        boolean b = !hasAvailableThreads() && !inputRepeater.hasEventsToHandle() && !inputRepeater.hasMoreInputs() 
                && (numAsyncTasksInMainLooper() == 0) && (numPendingAsyncTasks() == 0) && (taskToProcess > 1);
        
        if(b) { 
            // allow for the AsyncTask to run and execute its waiting statement (e.g. in comics app, music app)
            // PROBLEM: We still have a nondeterminism here as we cannot guess exactly when an AsyncTask thread becomes busy
            // Problematic if the execution of an AsyncTask task is leftover to a next test
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // clear AsyncTask messages in the UI thread
            // if the UI thread consecutively gets scheduled, it might indicate that it posts recurring tasks to itself, end the test in this case
            if (numAsyncTasksInMainLooper() != 0 || (threads.getThreadById(1).isWaiting() && (consecutiveMainTasks < MAX_CONSECUTIVEMAINTASKS))) { // eliminate recurring messages, only allow postexecutes 
                Log.i("RRScheduler", "Not the end of the test - main looper is not empty - is waiting ");
                return false;  
            }
            
            // check whether newly spawned AsyncTasks spawned
            if (!ReflectionUtils.getAsyncTaskSerialExecutorTasks().isEmpty() && (numPendingAsyncTasks() != 0) && !hasAvailableAsyncTaskThreads()) { 
                Log.i("RRScheduler", "Not the end of the test - AsyncTask thread is not idle ");
                return false;  
            }
        }
       
        return b;
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
        runMainToCompletionOrToWait(); 
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
            Logger.i("RRScheduler", "Scheduled: " + current.getName() + " Task to process: " + taskToProcess + " # of consecutive main tasks: " + consecutiveMainTasks);

            if(current.getId() == 1) 
                consecutiveMainTasks ++;
            else
                consecutiveMainTasks = 0;
        }

        return current;
    }

    public ThreadData getNextThread() {
        idleTypes = 0;
        refreshThreadList();
            
        // current is the next thread of type to schedule
        ThreadData current = getNextThreadOfType(types[nextTypeToSchedule]);
        logThreads(current);
        
        // if current thread is not okToSchedule, get the next available thread
        while(!okToSchedule(current) && idleTypes < types.length) { 
            refreshThreadList();
            idleTypes ++;
            nextTypeToSchedule = (nextTypeToSchedule + 1) % types.length;
            // Log.v("Scheduler","Next type: " + types[nextTypeToSchedule]);
            current = getNextThreadOfType(types[nextTypeToSchedule]);
            //Log.i("Scheduler", "Retrying next to schedule");
        }
        // can add if input repeater, move one by one =  skip to next thread after an input
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
        int size = threads.getThreads(ThreadType.ASYNCTASK).length;
        ThreadData current = null;
        int index = 0;
        
        while(index < size) {
            current = threads.getThreadByIndex(index, ThreadType.ASYNCTASK);
            index ++;
                       
            if(isOnSerialExecutor(current.getThread())) {
                
                // check if serial AsyncTask is waiting to be scheduled  
                if(okToSchedule(current)) { 
                    return current;
                    
                // check if it is posted but not executed and is waiting yet
                } else if (!ReflectionUtils.getAsyncTaskSerialExecutorTasks().isEmpty()) { 
                    Log.e("ASYNC", "There exists an AsyncTask posted but not waited yet..");
                    Log.e("ASYNC", "Check if all tasks are executed in the previous test, otherwise it might be a leftover..");

                    while(!ReflectionUtils.getAsyncTaskSerialExecutorTasks().isEmpty()) {
                        // allow for the AsyncTask to run and execute its waiting statement
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
    
                        for(int i=0; i<threads.getThreads(ThreadType.ASYNCTASK).length; i++) {
                            ThreadData td = threads.getThreadByIndex(i, ThreadType.ASYNCTASK);
                            if(okToSchedule(td) && isOnSerialExecutor(td.getThread())) {
                                return td;
                            } 
                        }
                        
                    }
                }
            }
        } 

        return null;
    }

    /**
     * Gets the next okToSchedule AsyncTask thread (indexed by poolThreadIndex)
     * that does not run on the serial executor
     * poolThreadIndex keeps the index of the pool thread to be scheduled next
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
            return null; // no available threads are found in this round
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
    public void doOnPostScheduling() {
        // clean the main thread after getting the last input, etc 
        // important for the last turn with the input, before checking the end of the test
        runMainToCompletionOrToWait(); 
    }
    
    @Override
    public void tearDownTestCase() {
        numCompletedTests ++; 
    }
}
