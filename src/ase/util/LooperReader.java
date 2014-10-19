package ase.util;

import java.lang.reflect.Field;

import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.util.Log;

public class LooperReader {
    private final Field messagesField;
    private final Field nextField;
    private final Field queueField;

    // private final List<Message> allMessages = new ArrayList<Message>();

    public LooperReader() {
        try {
            queueField = Looper.class.getDeclaredField("mQueue");
            queueField.setAccessible(true);
            messagesField = MessageQueue.class.getDeclaredField("mMessages");
            messagesField.setAccessible(true);
            nextField = Message.class.getDeclaredField("next");
            nextField.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Looper getLooper(Thread t) {
        if (t instanceof HandlerThread) {
            return ((HandlerThread) t).getLooper();
        }
        if (t.getId() == 1) {
            return Looper.getMainLooper();
        }
        Log.i("LooperReader", "No looper: " + t.getName());
        return null;
    }

    public boolean hasEmptyLooper(Thread t) {
        Looper looper = getLooper(t);
        if (looper == null)
            return true;

        Message message = null;
        try {
            MessageQueue messageQueue = (MessageQueue) queueField.get(looper);
            dumpQueue(messageQueue);
            message = (Message) messagesField.get(messageQueue);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (message == null)
            return true;
        Log.v("LooperReader", "Looper of Thread " + t.getId());
        return false;
    }

    public void dumpQueue(Thread t) {
        Looper looper = getLooper(t);
        if (looper == null)
            return;

        try {
            MessageQueue messageQueue = (MessageQueue) queueField.get(looper);
            dumpQueue(messageQueue);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void dumpQueue(MessageQueue messageQueue) {
        try {
            Message nextMessage = (Message) messagesField.get(messageQueue);
            // Log.d("LooperReader", "Begin dumping queue");
            dumpMessages(nextMessage);
            // Log.d("LooperReader", "End dumping queue");
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void dumpMessages(Message message) throws IllegalAccessException {
        if (message != null) {
            Log.d("LooperReader", message.toString());
            Message next = (Message) nextField.get(message);
            dumpMessages(next);
        }
    }
}
