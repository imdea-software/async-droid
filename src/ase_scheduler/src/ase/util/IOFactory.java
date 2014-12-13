package ase.util;

import android.content.Context;
import ase.Parameters;

public class IOFactory {

    private static final String DEFAULT_TRACE_FILE = "events.trc";
    private static final String DEFAULT_PARAMETER_FILE = "parameters.json";

    private static Parameters PARAMETERS;

    private static Recorder DEFAULT_RECORDER;
    private static Reader DEFAULT_READER;

    public static Recorder getRecorder(Context context) {
        if (DEFAULT_RECORDER == null) {
            DEFAULT_RECORDER = new FileRecorder(context, DEFAULT_TRACE_FILE);
        }
        return DEFAULT_RECORDER;
    }

    public static Reader getReader(Context context) {
        if (DEFAULT_READER == null) {
            DEFAULT_READER = new FileReader(context, DEFAULT_TRACE_FILE);
        }
        return DEFAULT_READER;
    }

    public static Parameters getParameters(Context context) {
        if (PARAMETERS == null) {
            ParametersReader reader = new ParametersReader(context, DEFAULT_PARAMETER_FILE);
            PARAMETERS = reader.readObject();
        }
        return PARAMETERS;
    }
}
