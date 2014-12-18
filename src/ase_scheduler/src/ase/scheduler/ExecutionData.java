package ase.scheduler;

public class ExecutionData {
    
    // UI blocks inserted by inputRepeater, onPublishProgress or onPostExecute
    // keeps track of UI thread status
    private int numUIBlocks = 0;
     
    public synchronized void incNumUIBlocks() {
        numUIBlocks ++;
    }
    
    public synchronized void decNumUIBlocks() {
        numUIBlocks --;
    }
    
    public synchronized int getNumUIBlocks() {
        return numUIBlocks;
    }
}
