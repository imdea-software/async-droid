package ase.scheduler;

import android.util.Log;
import ase.repeater.InputRepeater;
import ase.scheduler.PendingThreads.ThreadType;

public class RRScheduler extends Scheduler {
    private DelaySequence delaySeq;
    private int segmentToProcess = 1;
    private int walkerIndex = -1;
    
    int idleSteps = 0;
    
    public RRScheduler(PendingThreads threads, InputRepeater inputRepeater) {
        super(threads, inputRepeater);
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
        return delaySeq.isEndOfCurrentDelaySequence() && (selectNextThread() == null);
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
            Log.v("Scheduled", threads.toString());
            Log.v("Scheduled", "Current: " + current.getName() + " Walker Index: " + getWalkerIndex());

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
