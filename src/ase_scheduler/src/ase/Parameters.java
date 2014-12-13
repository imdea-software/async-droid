package ase;

/**
 * Created by burcuozkan on 13/12/14.
 */
public class Parameters {

    public static final Parameters EMPTY = new Parameters(SchedulerMode.NOP);

    private String mode;
    private int numDelays;

    public Parameters() {
    }

    private Parameters(SchedulerMode mode) {
        this.mode = mode.toString();
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public int getNumDelays() {
        return numDelays;
    }

    public void setNumDelays(int numDelays) {
        this.numDelays = numDelays;
    }

    public SchedulerMode getSchedulerMode() {
        return SchedulerMode.valueOf(mode);
    }
}
