package ase.scheduler;

public interface Scheduler {

    void runScheduler();

    /*
     * application thread waits for its signal to start/resume
     */
    void waitMyTurn();

    /*
     * application thread yields
     */
    void yield();

    /*
     * application thread notify scheduler when they are completed
     */
    void notifyScheduler();

    /*
     * application thread enters in a monitor
     */
    void enterMonitor();

    /*
     * application thread exits a monitor
     */
    void exitMonitor();

}
