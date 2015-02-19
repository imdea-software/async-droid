package ase.util;

import android.util.Log;
import ase.AppRunTimeData;
import ase.Parameters;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

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

        try {
            fIn = AppRunTimeData.getInstance().getAppContext().openFileInput(file);
            BufferedReader inBuff = new BufferedReader(new InputStreamReader(fIn));
            
            parameters = new Gson().fromJson(inBuff, Parameters.class);

        } catch (Exception e) {
            Log.w("FileReader", "Could not read from file: " + file);
        }

        return parameters;
    }
}
