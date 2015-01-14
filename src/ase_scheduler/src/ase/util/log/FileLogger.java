package ase.util.log;

import java.io.FileOutputStream;
import java.io.PrintWriter;

import android.content.Context;
import android.util.Log;
import ase.AppRunTimeData;
import ase.AseTestBridge;

public class FileLogger implements Logger {

    private final String fileName;
    
    public FileLogger(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public void i(String tag, String message) {
        tryAppendFile(tag, message, "Info"); 
    }

    @Override
    public void e(String tag, String message) {
        tryAppendFile(tag, message, "Error");
    }

    @Override
    public void w(String tag, String message) {
        tryAppendFile(tag, message, "Warning");
    }

    @Override
    public void v(String tag, String message) {
        tryAppendFile(tag, message, "Verbose");
    }

    private void tryAppendFile(String tag, String message, String level) {
        Context context = AppRunTimeData.getInstance().getAppContext();
        FileOutputStream fOut;
        try {
            fOut = context.openFileOutput(fileName, Context.MODE_APPEND);
            PrintWriter writer = new PrintWriter(fOut);
            writer.printf("%s: %s %s\n", level, tag, message);
            writer.flush();
            writer.close();
        } catch (Exception e) {
            Log.e("FileLogger", "Could not save file log to: "  + fileName, e);
        }
    }
}
