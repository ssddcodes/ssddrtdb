package dev.ssdd.sms;

import dev.ssdd.rtdb.WebSocket;
import dev.ssdd.zot.JSONObject;
import org.eclipse.jetty.websocket.api.Session;

public class SmsValueEveResponder {
    public boolean check = false;
    public SmsFileMaster g;

    synchronized void gitChildren(String path, Session session, String reqid, String dbid) {
        if (path.contains("/")) {
            String[] x = path.split("/");
            childrenManager(x, session, reqid, dbid);
        } else {
            path = path + "/";
            String[] x = path.split("/");
            childrenManager(x, session, reqid, dbid);
        }
    }

    synchronized void childrenManager(String[] path, Session session, String reqid, String dbid) {
        g = new SmsFileMaster(dbid) {
            @Override
            public void readFileResult(String json) {
                if (path != null) {
                    try {
                        JSONObject o = new JSONObject(json).getJSONObject(dbid);
                        System.out.println(o.isEmpty());
                        for (int i = 0; i < path.length; i++) {
                            if (o.has(path[i])) {
                                if (i < (path.length) - 1) {
                                    String tmp = path[i];
                                    if (o.has(tmp)) {
                                        o = o.getJSONObject(tmp);
                                    } else {
                                        semd("", session);
                                        break;
                                    }

                                } else {
                                    String tmp = path[i];
                                    if (o.has(tmp)) {
                                        o = o.getJSONObject(tmp);
                                    } else {
                                        JSONObject queryResultToJson = new JSONObject();
                                        queryResultToJson.put("id", "nsv");
                                        queryResultToJson.put("message", "");
                                        queryResultToJson.put("reqid", reqid);
                                        semd(queryResultToJson.toString(), session);
                                        break;
                                    }
                                    /*DocumentContext context = JsonPath.parse(o.toString());
                                    String queryResult = context.read("$.*").toString();*/

                                    System.out.println(o);

                                    JSONObject queryResultToJson = new JSONObject();
                                    queryResultToJson.put("id", "nsv");
                                    queryResultToJson.put("message", o);
                                    queryResultToJson.put("reqid", reqid);
                                    semd(queryResultToJson.toString(), session);
                                    check = true;
                                }
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("something went wrong ct " + e);
                    }
                }
            }
        };
        g.readFileResult();
    }

    private void semd(String msg, Session session) {
        System.out.println("Sent SVER");
        WebSocket.sendClient(session, msg);
    }
}