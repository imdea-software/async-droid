package ase;

import org.json.JSONObject;

/**
 * Created by burcuozkan on 13/12/14.
 */
public class Parameters {

    public static final Parameters EMPTY = new Parameters(ExecutionModeType.NOP);

    private String mode;
    private int numDelays;

    private Parameters(ExecutionModeType mode) {
        this.mode = mode.toString();
    }
    
    public Parameters() {
    }

    public Parameters(JSONObject json) {
        mode = json.optString("mode", "NOP");
        numDelays = json.optInt("numDelays", 0);
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

    public ExecutionModeType getSchedulerMode() {
        return ExecutionModeType.valueOf(mode);
    }
}
