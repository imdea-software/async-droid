package ase.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import android.util.Log;
import ase.AppRunTimeData;
import ase.event.AseEvent;

public class FileReader implements Reader {

    private final String file;
    
    private final JsonParser parser;
    private final Gson gson;
    
    public FileReader(String file) {
        this.file = file;
        this.parser = new JsonParser();
        this.gson = new Gson();
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

    @SuppressWarnings( {"unchecked", "rawtypes"} )
    private AseEvent createEventFromLine(String line) {
        JsonObject eventObj = parser.parse(line).getAsJsonObject();
        Class eventClass = AseEvent.getEventClass(eventObj.get("type").getAsString());
        return (AseEvent) gson.fromJson(eventObj, eventClass);
    }
}
