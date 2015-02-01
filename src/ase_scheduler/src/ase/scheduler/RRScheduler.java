package ase.scheduler;

import android.util.Log;
import ase.repeater.InputRepeater;
import ase.scheduler.PendingThreads.ThreadType;
import ase.util.ReflectionUtils;
import ase.util.log.Logger;

public class RRScheduler extends Scheduler {    
    private DelaySequence delaySeq;
    boolean isDelaying = true;
    int numDelays;
    int numCompletedTests = 0;
    
    private int idleTypes = 0;
    // default schedule: AsyncTaskSerialThread AsyncTaskPoolThreads HandlerThreads InputRepeater MainThread 
    private int typeToSchedule = 2;
    private ThreadType[] types = {ThreadType.INPUTREPEATER, ThreadType.MAIN, ThreadType.ASYNCTASK_SERIAL, ThreadType.ASYNCTASK_POOL, ThreadType.HANDLERTHREAD};
    boolean onPool= false;
    private int handlerThreadIndex = 0;
    private int poolThreadIndex = 0;
    
    private ThreadData scheduled = null;
    private int inputCount;
    
    public RRScheduler(PendingThreads threads, InputRepeater inputRepeater, Logger logger) {
        super(threads, inputRepeater, logger);
        inputCount = inputRepeater.numInputsLeft();
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
            logger.i("DelayInfo", "Current delay indices:" + delaySeq.toString());
        }     
    }
        
    @Override
    public boolean isEndOfTestCase() {
        return !hasAvailableThreads() && !inputRepeater.hasEventsToHandle() && (taskToProcess > 1);
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
 
    @Override
    public void doOnPreScheduling() {
        // Do not increment task count for the previous round
        // if inputrepeater is scheduled but it did not inject any events
        if(scheduled != null && scheduled.getName().equalsIgnoreCase("InputRepeater")) {
            if(inputRepeater.numInputsLeft() >= this.inputCount)
                taskToProcess --;
            inputCount = inputRepeater.numInputsLeft();
        }
        
        // IF MAIN_UNTIL_HAS_EVENT is set to true
        // Reschedule the main thread if it still has events to handle
        if(scheduled != null && scheduled.getId()==1 && inputRepeater.hasEventsToHandle()) {
            typeToSchedule --;
        }    
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
        if(!okToSchedule(current) && (idleTypes == types.length)) return scheduled = null;
        
        // check if current will be delayed, if so delay
        if(current != null && taskToProcess == getNextTaskIndexToDelay()) { 
            Log.i("AseScheduler", "Delayed Thread Id: " + current.getId() + " Last Processed: " + taskToProcess);
            logger.i("RRScheduler", "Delayed " + current.getName() + " Consumed Task to process: " + taskToProcess);
            Log.i("DelayInfo", "Consumed delay: " + taskToProcess);
            delaySeq.spendCurrentDelayIndex();       
            taskToProcess ++;
            return scheduled = selectNextThread(); // terminates since delaySeq is not infinite
        } 
        
        // current is the task to be dispatched
        taskToProcess ++;  
        return scheduled = current;
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
         
    private int getNextTaskIndexToDelay() {
        if (numCompletedTests == 0) return -1;
        return delaySeq.getNextDelayIndex();
    }
    
    @Override
    public void tearDownTestCase() {
        numCompletedTests ++;     
    }
}
