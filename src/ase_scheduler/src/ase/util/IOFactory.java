package ase.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.util.Log;
import ase.Parameters;

public class IOFactory {

    private static final String DEFAULT_TRACE_FILE = "events.trc";
    private static final String DEFAULT_PARAMETER_FILE = "parameters.json";

    private static Parameters PARAMETERS;

    private static Recorder DEFAULT_RECORDER;
    private static Reader DEFAULT_READER;

    public static String LOG_FILE;
    public static String STATS_FILE;
    private static boolean LOGS_INIT = false;
    
    public static void initializeLogs() {
        if(!LOGS_INIT) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
            String fileTag = dateFormat.format(new Date());
            
            LOG_FILE = "TestLogs-" + fileTag + ".log";
            STATS_FILE = "Stats-" + fileTag + ".log";

            LOGS_INIT = true;
        }
        Log.i("IOFactory", "Log file names ready...");
    }
    
    public static Recorder getRecorder() {
        if (DEFAULT_RECORDER == null) {
            DEFAULT_RECORDER = new FileRecorder(DEFAULT_TRACE_FILE);
        }
        return DEFAULT_RECORDER;
    }

    public static Reader getReader() {
        if (DEFAULT_READER == null) {
            DEFAULT_READER = new FileReader(DEFAULT_TRACE_FILE);
        }
        return DEFAULT_READER;
    }

    public static Parameters getParameters() {
        if (PARAMETERS == null) {
            ParametersReader reader = new ParametersReader(DEFAULT_PARAMETER_FILE);
            PARAMETERS = reader.readObject();
        }
        return PARAMETERS;
    }
}
