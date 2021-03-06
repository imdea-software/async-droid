package ase.scheduler;

import ase.ExecutionModeType;

public interface ExecutionMode {

    ExecutionModeType getExecutionModeType();

    void runScheduler();
    
    /*
     * application thread waits for its signal to start/resume
     */
    void waitForDispatch();

    /*
     * application thread yields
     */
    void yield();

    /*
     * application thread notify scheduler when they are completed
     */
    void notifyDispatcher();

    /*
     * application thread enters in a monitor
     */
    void enterMonitor();

    /*
     * application thread exits a monitor
     */
    void exitMonitor();
}
