package ase.scheduler;

import android.util.Log;
import ase.repeater.InputRepeater;
import ase.scheduler.PendingThreads.ThreadType;
import ase.util.LooperReader;
import ase.util.ReflectionUtils;
import ase.util.log.Logger;

public class RRScheduler extends Scheduler {   
    private int taskToProcess = 1;
    private int walkerIndex = -1;
    
    private DelaySequence delaySeq;
    boolean isDelaying = true;
    int numDelays;
    int numCompletedTests = 0;
    
    private int idleTypes = 0;
    // default schedule:
    // InputRepeater MainThread AsyncTaskThreads HandlerThreads
    private int typeToSchedule = 0;
    private ThreadType[] types = {ThreadType.INPUTREPEATER, ThreadType.MAIN, ThreadType.ASYNCTASK_SERIAL, ThreadType.ASYNCTASK_POOL, ThreadType.HANDLERTHREAD};
    boolean onPool= false;
    private int handlerThreadIndex = 0;
    //private int poolThreadIndex = 0;
    
    public RRScheduler(PendingThreads threads, InputRepeater inputRepeater, Logger logger) {
        super(threads, inputRepeater, logger);
    }
    
    @Override
    public void initiateScheduler(int bound, int inputSize) {      
        numDelays = bound;
    }

    @Override
    public void setUpTestCase() {
        walkerIndex = -1;
        taskToProcess = 1;
        if(delaySeq != null) { // null for the first test
            delaySeq.next();
            Log.i("DelayInfo", "Current delay indices:" + delaySeq.toString());
        }     
    }
        
    @Override
    public boolean isEndOfTestCase() {
        //TODO find a better synchronization to execute the remaining tasks
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return /*delaySeq.isEndOfCurrentDelaySequence() &&*/ !hasAvailableThreads() && (taskToProcess > 1);
    }
    
    @Override
    public boolean hasMoreTestCases() {
        if(numCompletedTests == 0) return true;
        if(isDelaying && numDelays > 0) {
            if(numCompletedTests == 1) { // first test without any delays
                //delaySeq = new DelaySequence(numDelays, taskToProcess-1 );
                delaySeq = new DelaySequence(0, 5 );
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
        
        ThreadData current = getNextThread(types[typeToSchedule]);
        logThreads(current);
        
        while(!okToSchedule(current) && idleTypes < types.length) {        
            idleTypes ++;
            current = getNextThread(types[typeToSchedule]);
            logThreads(current);
        }
        
        // check if the task will be delayed
        if(current != null && taskToProcess == getNextTaskIndexToDelay()) { 
            Log.i("AseScheduler", "Delayed Thread Id: " + current.getId() + " Last Processed: " + taskToProcess);
            Log.i("DelayInfo", "Consumed delay: " + taskToProcess);
            delaySeq.spendCurrentDelayIndex();
            taskToProcess ++;
            return selectNextThread(); // terminates since delaySeq is not infinite
        } 
        
        taskToProcess ++;  
        return current;
    }

    // returns okTOSchedule thread of type if exists, null otherwise
    private ThreadData getNextThread(ThreadType type) {
        ThreadData current = null;
        
        logger.i("RRScheduler", "Current type: " + type);
        Log.w("RRScheduler", "Current type: " + type);
        
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
            current = getPoolAsyncTaskThread();
            typeToSchedule = (typeToSchedule + 1) % types.length;
            
        } else if(types[typeToSchedule].equals(ThreadType.HANDLERTHREAD)) {
            int size = threads.getThreads(ThreadType.HANDLERTHREAD).length;
            // get the first okToSchedule handler thread
            while(!okToSchedule(current) && handlerThreadIndex < size) {
                handlerThreadIndex = (handlerThreadIndex + 1) % size;
                current = threads.getThreadByIndex(handlerThreadIndex, ThreadType.HANDLERTHREAD);
            } 
            if(handlerThreadIndex == size) {
                typeToSchedule = (typeToSchedule + 1) % types.length;
            }
        }
           
        return current;
    }
    
    private ThreadData getSerialAsyncTaskThread() {
        ThreadData selected = null;
        do {
            Log.e("Here", "Here");
            refreshThreadList();
            Object[] asyncTaskThreads = threads.getThreads(ThreadType.ASYNCTASK);
            for(Object o: asyncTaskThreads) {
                ThreadData td = (ThreadData) o;
                if(okToSchedule(td) && onSerialExecutor(td.getThread())) {
                    selected = td;
                    break;
                }
            }
        } while (selected == null && !ReflectionUtils.getAsyncTaskSerialExecutorTasks().isEmpty());
         
        return selected;
    }

    // TODO: What if there are more available concurrent tasks??
    // max thread pool size of them might be concurrent!!
    // keep an index as in HandlerThreads
    private ThreadData getPoolAsyncTaskThread() {
        ThreadData toSchedule = null;
       // do {
            refreshThreadList();
           Object[] asyncTaskThreads = threads.getThreads(ThreadType.ASYNCTASK);
            for(Object o: asyncTaskThreads) {
                ThreadData td = (ThreadData) o;
                if(okToSchedule(td) && onThreadPoolExecutor(td.getThread())) {
                    toSchedule = td;
                }
            }
      //  } while (toSchedule.size() != max_pool_size && !ReflectionUtils.getAsyncTaskPoolExecutorTasks().isEmpty());
         
        return toSchedule;
    }
    
    private void refreshThreadList() {
        threads.captureAllThreads();
        threads.sortThreadsByName();
    }
    
    private boolean onSerialExecutor(Thread t) {
        StackTraceElement[] calls = t.getStackTrace();
        for(StackTraceElement call: calls) {
            Log.i("Here", call.toString());
            if(call.toString().contains("android.os.AsyncTask$SerialExecutor")){
                return true;
            }
        }
        return false;
    }
    
    private boolean onThreadPoolExecutor(Thread t) {
        StackTraceElement[] calls = t.getStackTrace();
        for(StackTraceElement call: calls) {
            if(call.toString().contains("android.os.AsyncTask$SerialExecutor")){
                return false;
            }
        }
        return true;
    }
    
    private void logThreads(ThreadData current) {
      //List<Message> mainLooperMessages =  LooperReader.getInstance().getMessages(threads.getThreadById(1).getThread());
        logger.i("Main Looper Contents:", LooperReader.getInstance().dumpQueue(threads.getThreadById(1).getThread()));
        logger.i("RRScheduler", threads.toString());
        if(current != null) {
        logger.i("RRScheduler", "Current: " + current.getName() + " Walker Index: " + getWalkerIndex() + " Next Task#: " + taskToProcess);
        Log.v("RRScheduler", "Current: " + current.getName() + " Walker Index: " + getWalkerIndex() + " Next Task#: " + taskToProcess);
        }
    }
    
    private int getNextTaskIndexToDelay() {
        if (numCompletedTests == 0) return -1;
        return delaySeq.getNextDelayIndex();
    }

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
    }
    
    @Override
    public void tearDownTestCase() {
        numCompletedTests ++;     
    }
}
