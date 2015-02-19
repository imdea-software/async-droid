package ase.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.json.JSONObject;

import android.util.Log;
import ase.AppRunTimeData;
import ase.event.AseEvent;

public class FileReader implements Reader {

    private final String file;
        
    public FileReader(String file) {
        this.file = file;
    }

    @Override
    public List<AseEvent> read() {

        List<AseEvent> events = new ArrayList<AseEvent>();
        FileInputStream fIn;
        Scanner in = null;
        String inputLine;

        try {
            fIn = AppRunTimeData.getInstance().getAppContext().openFileInput(file);
            InputStreamReader isr = new InputStreamReader(fIn);
            BufferedReader inBuff = new BufferedReader(isr);

            while ((inputLine = inBuff.readLine()) != null) {
                in = new Scanner(inputLine);
                String line = in.nextLine();

                AseEvent event = createEventFromLine(line);
                events.add(event);
                Log.i("FileReader", "Read: " + Integer.toHexString(event.viewId));
            }
            
        } catch (Exception e) {
            Log.e("FileReader", "Could not read event from file: " + file, e);
        
        } finally {
            in.close();
        }
        
        return events;
    }

    private AseEvent createEventFromLine(String line) throws Exception {
        JSONObject eventObj = new JSONObject(line);
        return AseEvent.createEvent(eventObj);
    }
}
