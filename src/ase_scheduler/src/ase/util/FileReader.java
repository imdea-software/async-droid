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

import android.content.Context;
import android.util.Log;
import ase.*;

public class FileReader implements Reader {

    private final Context context;
    private final String file;
    
    private final JsonParser parser;
    private final Gson gson;
    
    public FileReader(Context context, String file) {
        this.context = context;
        this.file = file;
        this.parser = new JsonParser();
        this.gson = new Gson();
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
                Scanner in = new Scanner(inputLine);
                String line = in.nextLine();

                AseEvent event = createEventFromLine(line);
                events.add(event);
                Log.i("FileReader", "Read: " + Integer.toHexString(event.viewId));
            }

        } catch (Exception e) {
            Log.e("FileReader", "Could not read event from file: " + file, e);
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
