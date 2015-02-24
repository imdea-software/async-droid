package ase.util;

import java.io.FileOutputStream;
import java.io.PrintWriter;

import org.json.JSONObject;

import android.content.Context;
import android.util.Log;
import ase.AppRunTimeData;

public class FileUtils {
        
    public static void appendObject(String fileName, JSONObject obj) {
        appendLine(fileName, obj.toString());
    }
    
    public static void appendLine(String fileName, String content) {
        Context context = AppRunTimeData.getInstance().getAppContext();
        FileOutputStream fOut;
        try {
            fOut = context.openFileOutput(fileName, Context.MODE_APPEND);
            PrintWriter writer = new PrintWriter(fOut);
            writer.println(content);
            writer.flush();
            writer.close();
        } catch (Exception e) {
            Log.e("FileUtils", "Could not save file log to: "  + fileName, e);
        }
    }
}
