
package dev.ssdd.rtdb;

import android.util.Log;

import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Interpreter extends Thread {

    private URI uri;
    private final String TAG = "SSDDRTDB";
    private WSClient wsClient;
    public List<String> children2 = new ArrayList<>();
    private ValueEventListener ve;

    public Interpreter() {
        start();
    }

    @Override
    public void run() {
        //TODO config this on publish
        try {
            this.uri = new URI("ws://localhost:56118/");
            // this.uri = new URI("wss://45.79.48.63/");
            gitConnection();

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        //TODO remove LAN ws client and get from the server on lib release.

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
                Log.d(TAG, "onTextReceived: "+message);
                if (message.startsWith("[") && message.contains(",")) {
                    String[] snapshots = message.replace("[", "").replace("]", "").split(",");

                    List<DataSnapshot> snapshots1 = new ArrayList<>();
                    for (String x : snapshots) {
                        if(x.startsWith("{")) {
                            snapshots1.add(new DataSnapshot(x));
                        }
                    }
                    ve.updateData(snapshots1);
                } else if (message.startsWith("[")) {
                    String snapshots = message.replace("[", "").replace("]", "");
                    List<DataSnapshot> snapshots1 = new ArrayList<>();
                    if(snapshots.startsWith("{")) {
                        snapshots1.add(new DataSnapshot(snapshots));
                        ve.updateData(snapshots1);
                    }
                } else {
                    try {
                        throw new JSONException("recieved data is not in correct format");
                    } catch (JSONException e) {
                        e.printStackTrace();
                        ve.throwError(e);
                    }
                }
            }
        };

        wsClient.setConnectTimeout(6000);
        wsClient.enableAutomaticReconnection(5000);
        wsClient.connect();

    }

    void push(){
        WSClient client = new WSClient(this.uri) {
            @Override
            public void onTextReceived(String message) {
            }
        };
        client.connect();
        JSONObject o = new JSONObject();
        try {
            o.put("id","time");
            o.put("message","time?");
            client.send(o.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public Interpreter child(String path) {
        try {
            if (path.contains("/")) {
                String[] children = path.split("/");
                children2.addAll(Arrays.asList(children));
                Log.d(TAG, "child: " + Arrays.toString(children) + " " + children2);
            } else {
                children2.add(path);
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
            StringBuilder tm = new StringBuilder();
            jsonObject.put("id", "sv");
            for (int i = 0; i < children2.size(); i++) {
                if (i < (children2.size() - 1)) {
                    tm.append(children2.get(i)).append("/");
                } else {
                    String x = tm + children2.get(i) + "=" + value;
                    jsonObject.put("message", x);
                    wsClient.send("" + jsonObject);
                    Log.d(TAG, "setValue: " + jsonObject);
                }
            }
//            String x = children + "=" + value;
//            jsonObject.put("message", x);
//            wsClient.send("" + jsonObject);
//            Log.d(TAG, "setValue: " + children + jsonObject);
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
            } catch (IOException ignored) {
                // ignore close exception
            }
        }
    }

    public void addValueEventListener(ValueEventListener valueEventListener) {
        this.ve = valueEventListener;
        JSONObject object = new JSONObject();
        try {
            object.put("id","nsv");
            StringBuilder tm = new StringBuilder();
            for (int i = 0; i < children2.size(); i++) {
                if (i < (children2.size() - 1)) {
                    tm.append(children2.get(i)).append("/");
                } else {
                    String x = tm + children2.get(i);
                    object.put("message", x);
                    wsClient.send("" + object);
                    Log.d(TAG, "aVEL: " + object);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(TAG, "addValueEventListener: "+e);
        }
    }


    //  public abstract void onTxt(String msg);

}
