package dev.ssdd.rtdb;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Sandipsinh Rathod - SSDD
 */

public class SSDD extends PrivClient {

    private String childrenHolder = "";

    private static final int[] lastRandChars = new int[12];
    private static final Random randGen = new Random();

    private static long lastPushTime;
    private static final String PUSH_CHARS = "-0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz";

    private final String TAG = "SSDDRTDB";
    private boolean isRunning = false;
    public List<String> children = new ArrayList<>();
    private ValueEventListener ve;

    private final Object lock;

    private SingleValueEventListener sVEL;
    private final String ssddKeepAlive = "ssdd-keep-alive", id = "id", single = "single", nsv = "nsv", sv = "sv", message = "message", path = "path";

    public SSDD(URI uri) {
        super(uri);
        lock = new Object();
        enableAutomaticReconnection();
        getBodyText();
    }

    @Override
    protected void onOpen() {
        super.onOpen();
        isRunning = true;
    }

    @Override
    public void onText(Object msg) {
        new Thread(() -> {
            if (!(msg.toString().equals(ssddKeepAlive))) {
                Log.d(TAG, "onText: 1= " + msg);
                try {
                    JSONObject object = new JSONObject(msg.toString());
                    if (object.getString(id).equals(nsv)) {
                        String msg2 = object.getString(message);
                        Log.d(TAG, "onText: msg= " + msg2);
                        JSONArray array = new JSONArray(msg2);
                        List<DataSnapshot> snapshots1 = new ArrayList<>();
                        for (int i = 0; i < array.length(); i++) {
                            snapshots1.add(new DataSnapshot(array.get(i).toString()));
                        }
                        ve.onDataChange(snapshots1);
                    } else if (object.getString(id).equals(single)) {
                        sVEL.onDataChange(object.get(message));
                    }

                } catch (JSONException e) {
                    ve.onError(e);
                    sVEL.onError(e);
                }
            }
        }).start();

        new Thread(() -> {
            Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    send("ssdd");
                }
            }, 82800000, 82800000);
        }).start();
    }

    public SSDD child(String path) {
        try {
            if (path.contains("/")) {
                String[] pathSplitter = path.split("/");
                children.addAll(Arrays.asList(pathSplitter));
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 0; i < children.size(); i++) {
                    stringBuilder.append(children.get(i)).append("/");
                    if (i == (children.size() - 1)) {
                        childrenHolder = stringBuilder.toString();
                    }
                }
            } else {
                StringBuilder stringBuilder = new StringBuilder();
                children.add(path);
                for (int i = 0; i < children.size(); i++) {
                    stringBuilder.append(children.get(i)).append("/");
                    if (i == (children.size() - 1)) {
                        childrenHolder = stringBuilder.toString();
                    }
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "something went wrong " + e);
        }
        return this;
    }

    public synchronized SSDD push() {
        children.add(generateUID(System.currentTimeMillis()));
        return this;
    }

    public synchronized void setValue(Object value) {
        if (isRunning) {
            try {
                JSONObject object = new JSONObject();
                object.put(id, sv);
                try {
                    object.put(path, childrenHolder);
                    object.put(message, value);
                    send(object.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } catch (Exception e) {
                Log.d(TAG, "something went wrong " + e);
            }
        } else {
            Thread t = new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    setValue(value);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            t.start();
        }
    }

    public void addSingleValueEventListener(SingleValueEventListener s) {
        if (isRunning) {
            this.sVEL = s;
            JSONObject object = new JSONObject();
            try {
                object.put(id, single);
                StringBuilder sb = new StringBuilder();
                object.put(path, childrenHolder);
                send(object.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    addSingleValueEventListener(s);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    public void addValueEventListener(ValueEventListener v) {
        if (isRunning) {
            this.ve = v;
            JSONObject object = new JSONObject();
            try {
                object.put(id, nsv);
                object.put(path, childrenHolder);
                send(object.toString());

            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    addValueEventListener(v);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    public static synchronized String generateUID(long now) {
        boolean duplicateTime = (now == lastPushTime);
        lastPushTime = now;
        char[] timeStampChars = new char[8];
        StringBuilder result = new StringBuilder(20);
        for (int i = 7; i >= 0; i--) {
            timeStampChars[i] = PUSH_CHARS.charAt((int) (now % 64));
            //  Log.d("MainAc", "generatePushChildName: "+ PUSH_CHARS.charAt((int) (now % 64)));
            now = now / 64;
        }
        hardAssert(now == 0);

        result.append(timeStampChars);

        if (!duplicateTime) {
            for (int i = 0; i < 12; i++) {
                lastRandChars[i] = randGen.nextInt(64);
            }
        } else {
            incrementArray();
        }
        for (int i = 0; i < 12; i++) {
            result.append(PUSH_CHARS.charAt(lastRandChars[i]));
        }
        hardAssert(result.length() == 20);
        return result.toString();
    }

    public static void hardAssert(boolean condition) {
        hardAssert(condition, "");
    }

    private static void incrementArray() {
        for (int i = 11; i >= 0; i--) {
            if (lastRandChars[i] != 63) {
                lastRandChars[i] = lastRandChars[i] + 1;
                return;
            }
            lastRandChars[i] = 0;
        }
    }

    public static void hardAssert(boolean condition, String message) {
        if (!condition) {
            System.out.println("Assertion failed " + message);
        }
    }

    private void getBodyText() {
        new Thread(() -> {
            final StringBuilder builder = new StringBuilder();

            try {
                String url = "https://tmpstfilevfy.blogspot.com/";
                Document doc = Jsoup.connect(url).get();

                Element body = doc.body();
                builder.append(body.getElementById("turuf"));

                if (builder.toString().contains("false")) {
                    close();
                    throw new Exception("please upgrade to newer version of SSDDRTDB.");
                } else {
                    connect();
                }

            } catch (Exception e) {
                Log.d(TAG, "" + e);
            }
        }).start();
    }

}
