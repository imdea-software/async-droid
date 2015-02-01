package ase.util.log;

public class LogManager {
    public static Logger getLog() {
        return new AndroidLogger();
    }
    
    public static Logger getFileLog(String fileName) {
        return new FileLogger(fileName);
    }
}
