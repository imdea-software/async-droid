package ase.util;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import android.content.Context;
import android.util.Log;
import ase.AseEvent;

public class FileRecorder implements Recorder {

    private final Context context;
    private final String file;

    public FileRecorder(Context context, String file) {
        this.context = context;
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
            fOut = context.openFileOutput(file, 0);
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
            fOut = context.openFileOutput(file, Context.MODE_APPEND);
            PrintWriter writer = new PrintWriter(fOut);
            writer.println(event.toString());
            writer.flush();
            writer.close();
            Log.i("Recorder", "Recorded: " + Integer.toHexString(event.viewId) + " Value: " + event.toString());
        } catch (Exception e) {
            Log.e("Recorder", "Could not record event "  + Integer.toHexString(event.viewId), e);
        }
    }
}
