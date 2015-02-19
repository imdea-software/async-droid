package ase.util;

import android.util.Log;
import ase.AppRunTimeData;
import ase.Parameters;

import java.io.FileInputStream;
import java.util.Scanner;

import org.json.JSONObject;

/**
 * Created by burcuozkan on 13/12/14.
 */
public class ParametersReader {

    private final String file;

    public ParametersReader(String file) {
        this.file = file;
    }

    public Parameters readObject() {
        Parameters parameters = Parameters.EMPTY;
        FileInputStream fIn;
        Scanner scanner = null;

        try {
            fIn = AppRunTimeData.getInstance().getAppContext().openFileInput(file);
            scanner = new Scanner(fIn);  
            scanner.useDelimiter("\\Z");  
            String content = scanner.next(); 

            parameters = new Parameters(new JSONObject(content));

        } catch (Exception e) {
            Log.w("FileReader", "Could not read from file: " + file);
        } finally {
            if (scanner != null)
                scanner.close();
        }

        return parameters;
    }
}
