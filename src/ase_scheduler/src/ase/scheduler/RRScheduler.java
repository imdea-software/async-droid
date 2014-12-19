package ase.scheduler;

import android.util.Log;
import ase.repeater.InputRepeater;

public class RRScheduler extends Scheduler {
    private DelaySequence delaySeq;
    private int segmentToProcess = 1;

    int idleSteps = 0;
    
    public RRScheduler(PendingThreads threads, InputRepeater inputRepeater) {
        super(threads, inputRepeater);
    }
    
    @Override
    public void initiateScheduler(int bound, int inputSize) {      
        delaySeq = new DelaySequence(bound, inputSize);
    }

    @Override
    public void initiateTestCase() {
        segmentToProcess = 1;
        delaySeq.next();
        Log.i("DelayInfo", "Current delay indices:" + delaySeq.toString());
    }
        
    @Override
    public boolean isEndOfTestCase() {
        return delaySeq.isEndOfCurrentDelaySequence() && (idleSteps >= threads.getSize());
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
            // walker keeps the index of the thread to be scheduled
            threads.increaseWalker();

            ThreadData current = threads.getCurrentThread();
            Log.v("Scheduled", threads.toString());
            Log.v("Scheduled", "Current: " + current.getName() + " Walker Index: " + threads.getWalkerIndex());

            if(okToSchedule(current)){
                // check whether the thread will be delayed
                if (segmentToProcess == delaySeq.getNextDelayIndex()) {
                    Log.i("AseScheduler", "Delayed Thread Id: " + current.getId() + " Last Processed: " + segmentToProcess);
                    Log.i("DelayInfo", "Consumed delay: " + segmentToProcess);

                    segmentToProcess++;

                    /// not infinite loop, at least one of them is ok (the delayed one)
                    do {
                        threads.increaseWalker(); // delay - go for a ready-to-run thread
                        current = threads.getCurrentThread();
                    } while (!okToSchedule(current));

                    delaySeq.spendCurrentDelayIndex();
                }

                segmentToProcess ++;
                return current;
                
            }else{
                idleSteps++;  // scheduled thread has no message to execute
            }
        }
        
        return null;
    }
    

}
