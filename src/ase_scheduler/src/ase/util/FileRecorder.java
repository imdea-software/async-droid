package ase.util;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import android.content.Context;
import android.util.Log;
import ase.AppRunTimeData;
import ase.event.AseEvent;

public class FileRecorder implements Recorder {

    private final String file;

    public FileRecorder(String file) {
        this.file = file;
    }

    @Override
    public void record(AseEvent event) {
        recordEvent(event);
    }

    @Override
    public void clear() {
        FileOutputStream fOut;
        try {
            fOut = AppRunTimeData.getInstance().getAppContext().openFileOutput(file, 0);
            OutputStreamWriter osw = new OutputStreamWriter(fOut);
            osw.close();
            Log.i("Recorder", "Cleared records in " + file);
        } catch (Exception e) {
            Log.e("Recorder", "Cannot clear records in " + file);
        }
    }

    public void recordEvent(AseEvent event) {
        FileOutputStream fOut;
        try {
            fOut = AppRunTimeData.getInstance().getAppContext().openFileOutput(file, Context.MODE_APPEND);
            PrintWriter writer = new PrintWriter(fOut);
                        
            String eventStr = event.toJson().toString();
            
            writer.println(eventStr);
            writer.flush();
            writer.close();
            Log.i("Recorder", "Recorded: " + Integer.toHexString(event.viewId) + " Value: " + eventStr);
        } catch (Exception e) {
            Log.e("Recorder", "Could not record event with id: "  + Integer.toHexString(event.viewId), e);
        }
    }
}
