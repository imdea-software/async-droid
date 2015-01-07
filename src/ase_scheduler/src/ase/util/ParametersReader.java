package ase.util;

import android.content.Context;
import android.util.Log;
import ase.Parameters;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

/**
 * Created by burcuozkan on 13/12/14.
 */
public class ParametersReader {

    private final Context context;
    private final String file;

    public ParametersReader(Context context, String file) {
        this.context = context;
        this.file = file;
    }

    public Parameters readObject() {
        Parameters parameters = Parameters.EMPTY;
        FileInputStream fIn;

        try {
            fIn = context.openFileInput(file);
            BufferedReader inBuff = new BufferedReader(new InputStreamReader(fIn));
            
            parameters = new Gson().fromJson(inBuff, Parameters.class);

        } catch (Exception e) {
            Log.w("FileReader", "Could not read from file: " + file);
        }

        return parameters;
    }
}
