package ase.util;

import android.util.Log;

public class Logger {

    public static void i(String tag, String message) {
        Log.i(tag, message);
        tryAppendFile(tag, message, "Info"); 
    }

    public static void e(String tag, String message) {
        Log.e(tag, message);
        tryAppendFile(tag, message, "Error");
    }

    public static void w(String tag, String message) {
        Log.w(tag, message);
        tryAppendFile(tag, message, "Warning");
    }

    public static void v(String tag, String message) {
        Log.v(tag, message);
        tryAppendFile(tag, message, "Verbose");
    }

    private static void tryAppendFile(String tag, String message, String level) {
        String content = String.format("%s: %s %s\n", level, tag, message);
        FileUtils.appendLine(IOFactory.LOG_FILE, content);
    }
}
