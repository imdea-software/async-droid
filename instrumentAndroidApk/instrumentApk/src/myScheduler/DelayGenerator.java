package myScheduler;

public class DelayGenerator{

    private int numDelays;
    private int[] delayIndices;
    private int currentIndexToDelay = 0;
    private boolean isEnded;
    
    public DelayGenerator(int maxDelayIndex, int numDelays){
        this.numDelays = numDelays;    
        delayIndices = new int[numDelays];
        
        for(int i=0; i<numDelays; i++)
            delayIndices[i] = maxDelayIndex - numDelays + i;
    }
    
    public int getNextSegmentIndexToDelay() {

        if(currentIndexToDelay >= numDelays)
            return 0;
        
        // skip delay at 0 (e.g. for 4 0 0, the first delay is at 0) 
        while (currentIndexToDelay < numDelays-1 && delayIndices[currentIndexToDelay] == 0){
            currentIndexToDelay ++;
        }
            
        // if all 0, returns 0 (no segment will delay)
        return delayIndices[currentIndexToDelay]; 
    }

    public void setNextDelayPoint() {
        currentIndexToDelay++;
    }
    
    public boolean isEndOfCurrentDelaySequence() {
        if (currentIndexToDelay >= numDelays)
           return true;
        return false;
    }
    
    public boolean isEndOfAllDelaySequences() {
        return isEnded;
    }
    
    public boolean updateDelayIndices(){
        int decrementPoint = -1;
        for(int i=0; i<numDelays; i++){
            if(delayIndices[i] > 0){
                decrementPoint = i;
                break;
            }
        }
        // cannot update delay values
        // numIndices has all 0, test has finished
        if(decrementPoint == -1){
            isEnded = true;
            return false;
        }
                     
        delayIndices[decrementPoint] --;
        
        for(int i=decrementPoint-1; i>=0; i--){
            delayIndices[i] = Math.max(delayIndices[i+1] - 1, 0);  
        }
        
        currentIndexToDelay = 0;
        return true;
    }
    
    public int[] getIndices(){
        return delayIndices;
    }
    
    public int getDelayAtIndex(int index){
        if(index < delayIndices.length)
            return delayIndices[index];
        return -1;
    }
    
    public String delayIndicesToString(){
        String result = "";
        for(int i=numDelays-1; i>=0; i--){
            result = result.concat(Integer.toString(delayIndices[i]));
            result = result.concat(" ");
        }
        return result;
    }

}

