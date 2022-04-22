package dev.ssdd.sms;

import dev.ssdd.zot.JSONObject;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@org.eclipse.jetty.websocket.api.annotations.WebSocket
public class SmsWebSocket {
    public static final Queue<Session> sessions = new ConcurrentLinkedQueue<>();

    HashMap<Session, SmsValueEveResponder> svers = new HashMap<>();
    HashMap<Session, SmsSingleValueEveResponder> ssvers = new HashMap<>();
    HashMap<Session, SmsHTMLNotifier> shns = new HashMap<>();

    SmsValueEveResponder sver;
    SmsSingleValueEveResponder ssver;
    SmsHTMLNotifier shn;

    public static synchronized void sendClient(Session session, String msg) {
        try {
            if (sessions.contains(session)) {
                session.getRemote().sendString(msg);
            }
            session.getRemote().sendString(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @OnWebSocketConnect
    public void connected(Session session) {
        System.out.println("Connected ssdd");
        sessions.add(session);
        shn = new SmsHTMLNotifier();
        shns.put(session, shn);
    }

    @OnWebSocketClose
    public void closed(Session session, int statusCode, String reason) {
        sessions.remove(session);
        shns.remove(session);
        svers.remove(session);
        ssvers.remove(session);
    }

    @OnWebSocketMessage
    public void message(Session session, String message) throws IOException {
        System.out.println(message);

        if (!(message.equals("ssdd"))) {
            JSONObject j = new JSONObject(message);
            String id = j.getString("id");
            if (id.equals("sv")) {

                String[] x = j.getString("path").split("/");
                childManager(x,j.get("message"), j.getString("dbid"));

            } else if (id.equals("nsv")) {
                sver = new SmsValueEveResponder();
                svers.put(session, sver);
                sver.gitChildren(j.getString("path"), session, j.getString("reqid"), j.getString("dbid"));

            } else if (id.equals("single")) {
                ssver = new SmsSingleValueEveResponder();
                ssvers.put(session, ssver);
                ssver.gitChildren(j.getString("path"), session, j.getString("reqid"), j.getString("dbid"));

            } else if (id.equals("ssddNotifier07!")) {
                shn.gitJSON(session, j.getString("dbid"));
            } else if (id.equals("ssddUpdator07!")) {
                shn.updateJSON(j.getJSONObject("message"), j.getString("dbid"), svers.values(), ssvers.values(), shns.values());
            } else if (id.equals("rm")) {
                String[] path = j.getString("path").split("/");
                rm(path,j.getString("dbid"));
            }
        }else {
            sendClient(session, "keep-alive");
        }
    }

    private void rm(String[] path, String dbid){
        new Thread(()->{
            SmsFileMaster g = new SmsFileMaster(dbid) {
                @Override
                public void readFileResult(String json) {
                    JSONObject jx = new JSONObject(json);
                    JSONObject j = jx.getJSONObject(dbid);

                    for (int i = 0; i < path.length; i++) {
                        if (i < (path.length - 1)) {
                            if (j.has(path[i])) {
                                j = j.getJSONObject(path[i]);
                            }
                        } else {
                            j.remove(path[i]);
                        }
                    }
                    updateFile(jx.toString(), svers.values(), ssvers.values(), shns.values());
                }
            };
            g.readFileResult();
        }).start();
    }

    private void childManager(String[] svPathChildren, Object valu, String dbid) {
        Thread thread = new Thread(() -> {
            SmsFileMaster g = new SmsFileMaster(dbid) {
                @Override
                public void readFileResult(String json) {
                    try {
                        JSONObject object = new JSONObject(json);
                        JSONObject q = object.getJSONObject(dbid);
                        for (int i = 0; i < svPathChildren.length; i++) {
                            if (q.has(svPathChildren[i])) {
                                if (i < (svPathChildren.length - 1)) {
                                    try {
                                        q = q.getJSONObject(svPathChildren[i]);
                                    } catch (Exception ignored) {
                                        q.remove(svPathChildren[i]);
                                        for (int x = i; x < svPathChildren.length; x++) {
                                            if (x < (svPathChildren.length - 1)) {
                                                q.put(svPathChildren[x], new JSONObject());
                                                q = q.getJSONObject(svPathChildren[x]);
                                            } else {
                                                q.put(svPathChildren[x], valu);
                                            }
                                        }
                                    }
                                } else {
                                    q.put(svPathChildren[i], valu);
                                }
                            } else {
                                if (i < (svPathChildren.length - 1)) {
                                    JSONObject j = new JSONObject();
                                    q.put(svPathChildren[i], j);
                                    q = q.getJSONObject(svPathChildren[i]);
                                } else {
                                    q.put(svPathChildren[i], valu);
                                }
                            }
                            updateFile(object.toString(),svers.values(), ssvers.values(), shns.values());
                        }

                    } catch (Exception e) {
                        System.out.println("Something went wrong cm " + e);
                    }
                }
            };
            g.readFileResult();
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
