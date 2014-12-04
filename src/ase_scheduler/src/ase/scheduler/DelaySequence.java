package ase.scheduler;

public class DelaySequence {

    private int[] delaySequence;
    private int numDelays;
    private int maxDelayIndex;
    private int currentIndexToDelay;

    public DelaySequence(int numDelays, int maxDelayIndex) {
        this.numDelays = numDelays;
        this.maxDelayIndex = maxDelayIndex;
    }

    public boolean hasNext() {
        if (delaySequence == null)
            return true;

        for (int i = 0; i < numDelays; i++)
            if (delaySequence[i] > 0)
                return true;

        return false;
    }

    public void next() {
        if (delaySequence == null) {

            // on the first call
            delaySequence = new int[numDelays];
            for (int i = 0; i < numDelays; i++)
                delaySequence[i] = maxDelayIndex - numDelays + i;

        } else {

            // on subsequent calls
            int decrementPoint = -1;
            for (int i = 0; i < numDelays; i++) {
                if (delaySequence[i] > 0) {
                    decrementPoint = i;
                    break;
                }
            }

            // otherwise hasNext() should have returned false
            assert(decrementPoint > 0);

            delaySequence[decrementPoint]--;
            for (int i = decrementPoint - 1; i >= 0; i--)
                delaySequence[i] = Math.max(delaySequence[i + 1] - 1, 0);
        }
        currentIndexToDelay = 0;
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
