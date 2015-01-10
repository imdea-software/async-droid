package ase.util.log;

import android.util.Log;

public class AndroidLogger implements Logger {

    @Override
    public void i(String tag, String message) {
        Log.i(tag, message);
    }

    @Override
    public void e(String tag, String message) {
        Log.e(tag, message);
    }

    @Override
    public void w(String tag, String message) {
        Log.w(tag, message);
    }

    @Override
    public void v(String tag, String message) {
        Log.v(tag, message);
    }
}
