package dev.ssdd.rtdb;

import dev.ssdd.sms.Insts;
import dev.ssdd.sms.SmsMain;
import dev.ssdd.zot.JSONArray;
import dev.ssdd.zot.JSONObject;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;

import java.io.IOException;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;

import static dev.ssdd.rtdb.GenFile.createDB;
import static dev.ssdd.rtdb.GenFile.readFileAndReturnCred;
import static dev.ssdd.rtdb.PewPew.createpw;

@org.eclipse.jetty.websocket.api.annotations.WebSocket
public class WebSocket {

    // Store sessions if you want to, for example, broadcast a message to all users
    public static final Queue<Session> sessions = new ConcurrentLinkedQueue<>();

/*    private final HashMap<Session, String> clientDentifier = new HashMap<>();

    private final List<HtmlNotifier> notifiers = new ArrayList<>();
    private final List<ChildrenTrader> childrenTraders = new ArrayList<>();
    private final List<SingleTrader> singleTraders = new ArrayList<>();
    HashMap<Session, ChildrenTrader> map = new HashMap<>();
    HashMap<Session, SingleTrader> map2 = new HashMap<>();
    HashMap<Session, HtmlNotifier> map3 = new HashMap<>();

    ChildrenManager childrenManager;
    ChildrenTrader childrenTrader;
    SingleTrader singleTrader;
    HtmlNotifier notifier;*/

    @OnWebSocketConnect
    public void connected(Session session) {
        System.out.println("Connected");
        sessions.add(session);
/*        String dentifierHolder = generateUID(System.currentTimeMillis());
        clientDentifier.put(session, dentifierHolder);
        System.out.println("connected " + dentifierHolder);

//        childrenManager = new ChildrenManager();

        *//*childrenTrader = new ChildrenTrader();
        childrenTraders.add(childrenTrader);
        Credentials.childrenTradersx = childrenTraders;
        map.put(session, childrenTrader);*//*

         *//*singleTrader = new SingleTrader();
        singleTraders.add(singleTrader);
        Credentials.singleTradersx = singleTraders;
        map2.put(session, singleTrader);*//*

        notifier = new HtmlNotifier();
        notifiers.add(notifier);
        Credentials.notifiers = notifiers;
        map3.put(session, notifier);*/

   /*     Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                JSONObject object = new JSONObject("{\"id\":\"nsv\",\"message\":\"[{\\\"i1\\\":\\\""+i+"\\\"}]\"}");
                sendClient(session, object.toString());
                i++;
            }
        },10000,2000);*/

        //      sendClient(session,"hi");

    }

    @OnWebSocketClose
    public void closed(Session session, int statusCode, String reason) {
        sessions.remove(session);
/*
        System.out.println("disconnected " + clientDentifier.get(session) + " " + reason + "status-code: "+statusCode);

        clientDentifier.remove(session);


        //TODO remove session afterwards

        if(map.containsKey(session)){
            childrenTraders.remove(map.get(session));
            map.remove(session);
            Credentials.childrenTradersx = childrenTraders;
        }

        if(map3.containsKey(session)){
            notifiers.remove(map3.get(session));
            map3.remove(session);
        }

        if(map2.containsKey(session)){
            singleTraders.remove(map2.get(session));
            Credentials.singleTradersx = singleTraders;
            map2.remove(session);
        }
*/

    }

    @OnWebSocketMessage
    public void message(Session session, String message) throws IOException {
        System.out.println(message);

        String cred = readFileAndReturnCred();
        String key = new JSONObject(cred).getString("key");

        if (!(message.equals("ssdd"))) {
            JSONObject j = new JSONObject(message);
            String id = j.getString("id");
            if (id.equals("ssddDBManagementServ07!")) {
                String pw = j.getString("pw");
                if (new PewPew(pw).verifyPassword(key, pw)) {
                    sendClient(session, new JSONObject().put("servers", GenFile.getDB()).toString());
                } else {
                    sendClient(session, "Password incorrect");
                }
            } else if (id.equals("ssddLaunchServer07!")) {

                String pw = j.getString("pw");
                if (new PewPew(pw).verifyPassword(key, pw)) {
                    String dbid = j.getString("dbid");
                    if (!Insts.insts.containsKey(dbid)) {
                        JSONArray ja = new JSONObject(readFileAndReturnCred()).getJSONArray("servers");
                        int port = 0;
                        for (Object o : ja) {
                            try {
                                port = new JSONObject(o.toString()).getInt(dbid);
                            }catch (Exception ignored){}
                        }
                        new SmsMain().main(dbid, port);
//                        createDB(new Scanner(System.in));
                        sendClient(session, "Started server");
                    } else {
                        sendClient(session, "Server already running, if not please contact developer");
                    }
                } else {
                    sendClient(session, "Password incorrect");
                }
            } else if (id.equals("ssddKillServer07!")) {
                String pw = j.getString("pw");
                if(new PewPew(pw).verifyPassword(key, pw)){
                    String dbid = j.getString("dbid");
                    if (Insts.insts.containsKey(dbid)) {
                        Insts.insts.get(dbid).stop();
                        Insts.insts.remove(dbid);
                        sendClient(session, "Killed server");
                    }
                } else {
                    sendClient(session, "Password incorrect");
                }
            } else if (id.equals("ssddCreateServer07!")) {
                String pw = j.getString("pw");
                if(new PewPew(pw).verifyPassword(key,pw)){
                    createDB(j.getString("dbid"),Integer.parseInt(j.get("port").toString()),session);
                } else {
                    sendClient(session, "Password incorrect");
                }
            } else if (id.equals("ssddResetPw07!")) {
                createpw(new Scanner(System.in));
            }
        } else {
            System.out.println("keep-alive");
        }

/*        if (!(message.equals("ssdd"))) {
            try {
                JSONObject j = new JSONObject(message);
                Object id = j.get("id"), path = j.get("path");//,path=j.getString("path");
             *//*   switch (id.toString()) {
                     case "sv" -> childrenManager.gitChildren(path.toString(), j.get("message"));
                    case "nsv" -> {
                        childrenTrader.gitChildren(path.toString(), session);
                        System.out.println("nsv");
                    }
                    case "single" -> singleTrader.gitChildren(path.toString(), session);
                    case "ssddNotifier07!"-> notifier.gitJSON(session);
                    case "ssddUpdator07!"-> {
                        notifier.updateJSON(j.get("message"));
                    }
                }*//*
                if (id.toString().equals("sv")) {

                    childrenManager = new ChildrenManager();
                    childrenManager.gitChildren(path.toString(), j.get("message"), false);

                } else if (id.toString().equals("nsv")) {

                    childrenTrader = new ChildrenTrader();
                    childrenTraders.add(childrenTrader);
                    Credentials.childrenTradersx = childrenTraders;
                    map.put(session, childrenTrader);

                    childrenTrader.gitChildren(path.toString(), session, j.getString("reqid"));
//                    System.out.println("nsv");
                } else if (id.toString().equals("single")) {

                    singleTrader = new SingleTrader();
                    singleTraders.add(singleTrader);
                    Credentials.singleTradersx = singleTraders;
                    map2.put(session, singleTrader);

                    singleTrader.gitChildren(path.toString(), session, j.getString("reqid"));

                } else if (id.toString().equals("ssddNotifier07!")) {
                    PewPew pew = new PewPew(j.getString("pw"));
                    JSONObject notifiero = new JSONObject(generateFileNCheck.readFileAndReturnCred());

                    if (pew.verifyPassword(notifiero.getString("key"), notifiero.getString("salt"))) {
                        notifier.gitJSON(session);
                    } else {
                        sendClient(session, "AD.");
                    }
                } else if (id.toString().equals("ssddUpdator07!")) {
                    PewPew pew = new PewPew(j.getString("pw"));

                    JSONObject notifiero = new JSONObject(generateFileNCheck.readFileAndReturnCred());

                    if (pew.verifyPassword(notifiero.getString("key"), notifiero.getString("salt"))) {
                        notifier.updateJSON(j.get("message"));
                    } else {
                        sendClient(session, "AD.");
                    }
                } else if (id.toString().equals("rm")) {
                    childrenManager = new ChildrenManager();
                    childrenManager.gitChildren(path.toString(), null, true);
                } else if (id.toString().equals("ssddResetPw07!")) {
                    Main.resetPw();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("keep-alive");
        }*/
    }

    public static synchronized void sendClient(Session session, String msg) {
        System.out.println("sending "+ msg);
        try {
            session.getRemote().sendString(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
