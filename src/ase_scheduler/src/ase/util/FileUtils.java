package ase.util;

import java.io.FileOutputStream;
import java.io.PrintWriter;

import com.google.gson.Gson;

import android.content.Context;
import android.util.Log;
import ase.AppRunTimeData;

public class FileUtils {
    
    private static final Gson GSON = new Gson();
    
    public static void appendObject(String fileName, Object obj) {
        appendLine(fileName, GSON.toJson(obj, obj.getClass()));
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
