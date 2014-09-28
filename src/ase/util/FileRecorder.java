package ase.util;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

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
        FileOutputStream fOut;
        try {
            fOut = context.openFileOutput(file, Context.MODE_APPEND);
            OutputStreamWriter osw = new OutputStreamWriter(fOut); 
            osw.append(event.viewId + "\n");
            osw.flush();
            osw.close();
            Log.i("Recorder", "Recorded: " + Integer.toHexString(event.viewId));
        } catch (Exception e) {
            Log.e("Recorder", "Could not record event " + Integer.toHexString(event.viewId), e);
        }
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
}
