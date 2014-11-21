package ase.scheduler;

public class DelaySequence {

    private int[] delaySequence;
    private int numDelays;
    private int currentIndexToDelay = 0;
    private boolean isEnded;

    public DelaySequence(int numDelays, int maxDelayIndex) {
        this.numDelays = numDelays;
        delaySequence = new int[numDelays];

        for (int i = 0; i < numDelays; i++)
            delaySequence[i] = maxDelayIndex - numDelays + i;
    }

    public int getNextDelayIndex() {

        // skip delays 0 (e.g. for 4 0 0, the first delay is at 4)
        while (currentIndexToDelay < numDelays
                && delaySequence[currentIndexToDelay] == 0) {
            currentIndexToDelay++;
        }

        // if all 0, currentIndexToDelay == numDelays, then return 0 (no segment
        // will delay)
        if (currentIndexToDelay >= numDelays)
            return 0;
        else
            return delaySequence[currentIndexToDelay];
    }

    public void spendCurrentDelayIndex() {
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

    public boolean getNextDelaySequence() {
        int decrementPoint = -1;
        for (int i = 0; i < numDelays; i++) {
            if (delaySequence[i] > 0) {
                decrementPoint = i;
                break;
            }
        }
        // cannot update delay values
        // numIndices has all 0, test has finished
        if (decrementPoint == -1) {
            isEnded = true;
            return false;
        }

        delaySequence[decrementPoint]--;

        for (int i = decrementPoint - 1; i >= 0; i--) {
            delaySequence[i] = Math.max(delaySequence[i + 1] - 1, 0);
        }

        currentIndexToDelay = 0;
        return true;
    }

    public int getDelayAtIndex(int index) {
        if (index < delaySequence.length)
            return delaySequence[index];
        return -1;
    }

    @Override
    public String toString() {
        String result = "";
        for (int i = numDelays - 1; i >= 0; i--) {
            result = result.concat(Integer.toString(delaySequence[i]));
            result = result.concat(" ");
        }
        return result;
    }

}
