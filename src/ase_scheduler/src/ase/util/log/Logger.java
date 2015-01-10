package ase.util.log;

public interface Logger {
    void i(String tag, String message);
    void e(String tag, String message);
    void w(String tag, String message);
    void v(String tag, String message);
}
