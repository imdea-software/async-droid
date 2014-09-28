package ase.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;
import ase.AseEvent;

public class FileReader implements Reader {

    private final Context context;
    private final String file;
    
    public FileReader(Context context, String file) {
        this.context = context;
        this.file = file;
    }
    
    @Override
    public List<AseEvent> read() {
        
        List<AseEvent> events = new ArrayList<AseEvent>();
        FileInputStream fIn;
        String inputLine;
        
        try {
            fIn = context.openFileInput(file);
            InputStreamReader isr = new InputStreamReader(fIn);
            BufferedReader inBuff = new BufferedReader(isr);

            while ((inputLine = inBuff.readLine()) != null) {
                AseEvent event = new AseEvent();
                event.viewId = Integer.parseInt(inputLine);
                events.add(event);
                Log.i("FileReader", "Read: " + Integer.toHexString(Integer.parseInt(inputLine)));
            }
                 
        } catch (Exception e) {
            Log.e("FileReader", "Could not read event from file: " + file, e);
        }
        
        return events;
    }
    

}
