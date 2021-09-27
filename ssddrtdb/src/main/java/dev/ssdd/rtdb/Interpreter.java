package dev.ssdd.rtdb;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Random;

public abstract class Interpreter {

     URI uri;
    WebSocketClient webSocketClient,w;
    private String tmpx;

    private static final int[] lastRandChars = new int[12];
    private static final Random randGen = new Random();

    private static long lastPushTime;
    private static final String PUSH_CHARS = "-0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz";

    public Interpreter() {
        init();
    }

    public void init(){
        Thread t =  new Thread(()->{
            // String urlx="https://ssdd.rf.gd/server/ssdd/";
            String urlx = "http://10.42.0.1:8000/ws.txt";
            URL url;
            try {
                url = new URL(urlx);
                URLConnection urlConnection = url.openConnection();
                InputStream in = urlConnection.getInputStream();
                BufferedReader r = new BufferedReader(new InputStreamReader(in));
                StringBuilder total = new StringBuilder();
                for (String line; (line = r.readLine()) != null; ) {
                    this.uri = new URI(total.append(line).toString());
                }

                 webSocketClient = new WebSocketClient(this.uri) {

                    @Override
                    public void onTextReceived(String message) {

                        if(!(isTime(message))){
                            String tmp = null;
                            try {
                                JSONObject j = new JSONObject(message);
                                tmp = j.get("message").toString();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            onTxt(tmp);
                        }
                    }
                 };
                webSocketClient.setConnectTimeout(6000);
                webSocketClient.enableAutomaticReconnection(5000);
                webSocketClient.connect();

            } catch (IOException e) {
                Log.e("SSDDRTDB", "An error occurred while fetching ws server details");
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        });
        t.start();
    }
    private boolean isTime(String msg){
        String x = "message";
        try {
            JSONObject jsonObject = new JSONObject(msg);
            x = jsonObject.get("id").toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return x.equals("time");
    }

    public void semd(String msg){
        webSocketClient.send(msg);
    }
    public abstract void onTxt(String msg);

    public String push(){
        synchronized (this) {
             w = new WebSocketClient(this.uri) {
                @Override
                public void onTextReceived(String message) {
                    Thread thread = new Thread(() -> {
                        String tmp = null;
                        try {
                            JSONObject j = new JSONObject(message);
                            if (j.get("id").equals("time")) {
                                tmp = j.get("message").toString();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        assert tmp != null;
                        tmpx = generatePushChildName(Long.parseLong(tmp));
                        //TODO remove onTxt after debug
                        onTxt(tmpx);
                    });
                    thread.start();
                }
            };
            w.connect();
            w.send("time?");
        }
         return tmpx;
    }
    private static synchronized String generatePushChildName(long now) {
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
            if (BuildConfig.DEBUG) {
                throw new AssertionError("hardAssert failed: " + message);
            } else {
                Log.w("SSDDRTDB", "Assertion failed: " + message);
            }
        }
    }

}
