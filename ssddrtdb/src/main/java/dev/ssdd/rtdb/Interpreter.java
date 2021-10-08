
package dev.ssdd.rtdb;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.net.URISyntaxException;

public class Interpreter extends Thread {

    private URI uri;
    private final String TAG = "SSDDRTDB";
    private WSClient wsClient;
    private String children = "";

    public Interpreter() {
        start();
    }

    @Override
    public void run() {
        //TODO config this on publish
        try {
            this.uri = new URI("ws://10.42.0.1:56118/");
            // this.uri = new URI("wss://45.79.48.63/");
            gitConnection();

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
//        String urlx = "http://10.42.0.1:8000/ws.txt";
//        URL url;
//        try {
//            url = new URL(urlx);
//            URLConnection urlConnection = url.openConnection();
//            InputStream in = urlConnection.getInputStream();
//            BufferedReader r = new BufferedReader(new InputStreamReader(in));
//            StringBuilder total = new StringBuilder();
//            for (String line; (line = r.readLine()) != null; ) {
//                this.uri = new URI(total.append(line).toString());
//            }
//
//            gitConnection();
//
//        } catch (IOException e) {
//            Log.e("SSDDRTDB", "An error occurred while fetching ws server details");
//            isRunun = false;
//        } catch (URISyntaxException e) {
//            e.printStackTrace();
//            isRunun = false;
//        }
    }

    private synchronized void gitConnection() {
        //    onTxt(message);
        wsClient = new WSClient(this.uri) {
            @Override
            public void onTextReceived(String message) {
                //    onTxt(message);
            }
        };

        wsClient.setConnectTimeout(6000);
        wsClient.enableAutomaticReconnection(5000);
        wsClient.connect();

    }


    public void semd(String msg) {
        wsClient.send("");
    }

    public Interpreter child(String path) {

        StringBuilder sb = new StringBuilder(path);
        try {
            if (childrenChecker(path)) {
                if (!path.startsWith("/")) {
                    path = "/" + path;
                    Log.d(TAG, "child: ! "+path);
                    if (path.endsWith("/")) {
                        path = sb.deleteCharAt(path.length() - 1).toString();
                        children = children + path;
                        Log.d(TAG, "child: end "+path);
                    }else {
                        children = children + path;
                    }
                } else {
                    if (path.endsWith("/")) {
                        Log.d(TAG, "child: end 2"+path);
                        path = sb.deleteCharAt(path.length() - 1).toString();
                        children = children + path;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    private boolean childrenChecker(String children) throws Exception {
        if (children.contains("\"")) {
            throw new Exception("Child:- you can not put double inverted commas \"\" in children");
        } else {
            return true;
        }
    }

    public void setValue(String value) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", "sv");
            String x = children + "=" + value;
            jsonObject.put("message", x);
            wsClient.send("" + jsonObject);
            Log.d(TAG, "setValue: " + children + jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void setValue(Object value) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(value);
            out.flush();
            byte[] bytes = bos.toByteArray();
            wsClient.send(bytes);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bos.close();
            } catch (IOException ex) {
                // ignore close exception
            }
        }
    }


    //  public abstract void onTxt(String msg);

}
