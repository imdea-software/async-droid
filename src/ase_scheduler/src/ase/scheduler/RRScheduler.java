package ase.scheduler;

import android.util.Log;
import ase.AseTestBridge;
import ase.repeater.InputRepeater;
import ase.scheduler.PendingThreads.ThreadType;
import ase.util.LooperReader;
import ase.util.log.Logger;

public class RRScheduler extends Scheduler {
    private DelaySequence delaySeq;
    private int segmentToProcess = 1;
    private int walkerIndex = -1;
    
    int postponed = 0;
    int idleSteps = 0;
    
    public RRScheduler(PendingThreads threads, InputRepeater inputRepeater, Logger logger) {
        super(threads, inputRepeater, logger);
    }
    
    @Override
    public void initiateScheduler(int bound, int inputSize) {      
        delaySeq = new DelaySequence(bound, inputSize);
    }

    @Override
    public void setUpTestCase() {
        walkerIndex = -1;
        segmentToProcess = 1;
        delaySeq.next();
        Log.i("DelayInfo", "Current delay indices:" + delaySeq.toString());
    }
        
    @Override
    public boolean isEndOfTestCase() {
        return delaySeq.isEndOfCurrentDelaySequence() && !hasAvailableThreads();
    }
    
    public boolean hasAvailableThreads() {
        for(int i=0; i<threads.getSize(); i++) {
            if(okToSchedule(threads.getThreadByIndex(i, ThreadType.ANY)))
                    return true;
        }
        return false;
    }
    
    @Override
    public boolean hasMoreTestCases() {
        return delaySeq.hasNext();
    }
   
    @Override
    public ThreadData selectNextThread() {
        idleSteps = 0;
        
        // if no available threads, then this is the end of the test case
        while (idleSteps < threads.getSize()) {
            
            threads.captureAllThreads();
            threads.sortThreadsByName();
            
            // walker keeps the index of the thread to be scheduled
            increaseWalker();

            ThreadData current = threads.getThreadByIndex(walkerIndex, ThreadType.ANY);
           
            logger.i("Main", LooperReader.getInstance().dumpQueue(threads.getThreadById(1).getThread()));
            logger.i("RRScheduler", threads.toString());
            logger.i("RRScheduler", threads.toString());
            logger.i("RRScheduler", "Current: " + current.getName() + " Walker Index: " + getWalkerIndex());

            if(okToSchedule(current)) {
                // check whether the thread will be delayed
                if (segmentToProcess == delaySeq.getNextDelayIndex()) {
                    Log.i("AseScheduler", "Delayed Thread Id: " + current.getId() + " Last Processed: " + segmentToProcess);
                    Log.i("DelayInfo", "Consumed delay: " + segmentToProcess);
                    delaySeq.spendCurrentDelayIndex();
                    segmentToProcess ++;
                    return selectNextThread(); // terminates since delaySeq is not infinite
                }
                segmentToProcess ++;
                return current;
                
            } else {
                logger.i("RRScheduler", "OkToSchedule is false for " + current.getName() + " numUIblocks: " + AseTestBridge.getNumUIBlocks());
                idleSteps++;  // scheduled thread has no message to execute
            }
        }
        
        return null;
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
        // TODO Auto-generated method stub        
    }
    

}
