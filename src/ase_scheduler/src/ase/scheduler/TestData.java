package ase.scheduler;

import org.json.JSONObject;
 
public class TestData {
    
    private int numInputTasks;
    private int numUIThreadTasks;
    private int numAllUIThreadTasks;
    private int numAsyncPoolTasks;
    
    public TestData(int numInputTasks, int numUIThreadTasks, int numAllUIThreadTasks, int numAsyncPoolTasks) {
        this.numInputTasks = numInputTasks;
        this.numUIThreadTasks = numUIThreadTasks;
        this.numAllUIThreadTasks = numAllUIThreadTasks; 
        this.numAsyncPoolTasks = numAsyncPoolTasks;
    }

    public int getNumInputTasks() {
        return numInputTasks;
    }

    public int getNumUIThreadTasks() {
        return numUIThreadTasks;
    }

    public int getNumAllUIThreadTasks() {
        return numAllUIThreadTasks;
    }

    public int getNumAsyncPoolTasks() {
        return numAsyncPoolTasks;
    }
    
    public JSONObject toJson() throws Exception {
        JSONObject json = new JSONObject();
        json.put("numInputTasks", numInputTasks)
            .put("numUIThreadTasks", numUIThreadTasks)
            .put("numAllUIThreadTasks", numAllUIThreadTasks)
            .put("numAsyncPoolTasks", numAsyncPoolTasks);
        return json;
    }
}

