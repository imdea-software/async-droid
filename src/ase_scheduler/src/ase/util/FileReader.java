package ase.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import android.content.Context;
import android.util.Log;
import ase.AseClickEvent;
import ase.AseItemClickEvent;
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

    private AseEvent createEventFromLine(String line) {
        String tokens[] = line.split(" ");

        AseEvent.EventType type = AseEvent.EventType.valueOf(tokens[0]);
        int viewId = Integer.parseInt(tokens[1]);

        AseEvent event = null;
        if(type == AseEvent.EventType.CLICK) {
            event = new AseClickEvent(viewId);
        }
        if(type == AseEvent.EventType.ITEMCLICK) {
            event = new AseItemClickEvent(viewId, Integer.parseInt(tokens[2]), Integer.parseInt(tokens[3]));
            Log.i("FileReader", "Pos: " + ((AseItemClickEvent) event).itemPos);
        }
        return event;
    }

}
