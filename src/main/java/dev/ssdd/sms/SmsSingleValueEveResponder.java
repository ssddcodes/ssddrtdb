package dev.ssdd.sms;

import dev.ssdd.rtdb.WebSocket;
import dev.ssdd.zot.JSONObject;
import org.eclipse.jetty.websocket.api.Session;

public class SmsSingleValueEveResponder {
    public boolean check = false;
    public SmsFileMaster g;


    void gitChildren(String path, Session session, String reqid, String dbid) {
        if (path.contains("/")) {
            String[] x = path.split("/");
            SmsSingleResp(x, session, reqid, dbid);
        } else {
            path = path + "/";
            String[] x = path.split("/");
            SmsSingleResp(x, session, reqid, dbid);
        }
    }

    void SmsSingleResp(String[] path, Session session, String reqid, String dbid) {
        if (path != null) {
            g = new SmsFileMaster(dbid) {
                @Override
                public void readFileResult(String json) {

                    try {
                        JSONObject o = new JSONObject(json).getJSONObject(dbid);
                        if (path.length == 0) {
                            JSONObject xx = new JSONObject();
                            xx.put("id", "single");
                            xx.put("message", o);
                            xx.put("reqid", reqid);
                            semd(xx.toString(), session);
                            check = true;
                        } else {
                            for (int i = 0; i < path.length; i++) {
                                if (o.has(path[i])) {
                                    if (i < (path.length) - 1) {
                                        String tmpx = path[i];
                                        if(o.has(tmpx)){
                                            o = o.getJSONObject(path[i]);
                                        }else {
                                            JSONObject queryResultToJson = new JSONObject();
                                            queryResultToJson.put("id", "nsv");
                                            queryResultToJson.put("message", "");
                                            queryResultToJson.put("reqid", reqid);
                                            semd(queryResultToJson.toString(), session);
                                            break;
                                        }
                                    } else {
                                        String tmpx = path[i];
                                        Object x;
                                        if(o.has(tmpx)){
                                            x = o.get(path[i]);
                                        }else {
                                            semd("", session);
                                            break;
                                        }
                                        //JsonPath.parse perses the json
                                        //                                DocumentContext context = JsonPath.parse(o.toString());
                                        //                                Object x = context.read("$.*").toString();
                                        JSONObject xx = new JSONObject();
                                        xx.put("id", "single");
                                        xx.put("message", x);
                                        xx.put("reqid", reqid);
                                        semd(xx.toString(), session);
                                        check = true;
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("something went wrong ct2 " + e);
                    }
                }
            };
        }
        g.readFileResult();
    }

    private void semd(String msg, Session session) {
        if (WebSocket.sessions.contains(session)) {
            System.out.println("sent SSVER");
            WebSocket.sendClient(session, msg);
        }
    }
}
