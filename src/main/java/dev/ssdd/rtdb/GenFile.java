package dev.ssdd.rtdb;

import dev.ssdd.sms.SmsMain;
import dev.ssdd.zot.JSONArray;
import dev.ssdd.zot.JSONObject;
import org.eclipse.jetty.websocket.api.Session;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class GenFile {
    public static final File credFile = new File(System.getProperty("user.home") + File.separator + ".ssddrtdb" + File.separator + "creds.json");

    //            dbfile = new File(System.getProperty("user.home") + File.separator + ".ssddrtdb" + File.separator + "db.json")
    public static String readFileAndReturnCred() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(credFile));
            return br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return e.toString();
        }
    }

    static void fileCheck() {
        Scanner scanner = new Scanner(System.in);
        if (!credFile.exists()) {
            try {
                credFile.getParentFile().mkdirs();
                credFile.createNewFile();
                PewPew.createpw(scanner);
                createDB(scanner);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            PewPew.verifypw(scanner);
        }
    }

    static String getDB() {
        JSONObject j = new JSONObject(readFileAndReturnCred());
        JSONArray ja = j.getJSONArray("servers");
        return ja.toString();
    }

    static void createDB(Scanner sc) {
        System.out.println("Please enter root name for new JSON DB: ");
        String rootName = sc.nextLine();
        JSONObject jsonObject = new JSONObject(readFileAndReturnCred());
        int port = inputNewPort(new Scanner(System.in));
        try {
            JSONArray serverList = jsonObject.getJSONArray("servers");
            serverList.put(new JSONObject().put(rootName, port));
        } catch (Exception ignored) {
            jsonObject.put("servers", new JSONArray().put(new JSONObject().put(rootName, port)));
        }
        updateCred(jsonObject);

        new SmsMain().main(rootName, port);
    }
    static void createDB(String rootName, int port, Session session) {
//        System.out.println("Please enter root name for new JSON DB: ");
//        String rootName = sc.nextLine();
        JSONObject jsonObject = new JSONObject(readFileAndReturnCred());
//        int port = inputNewPort(new Scanner(System.in));
        try {
            JSONArray serverList = jsonObject.getJSONArray("servers");
            serverList.put(new JSONObject().put(rootName, port));
        } catch (Exception ignored) {
            jsonObject.put("servers", new JSONArray().put(new JSONObject().put(rootName, port)));
        }
        updateCred(jsonObject);
        new SmsMain().main(rootName, port);
        sendClient(session);
    }

    static synchronized void sendClient(Session session) {
        System.out.println("sending "+ "Created DB and Started.");
        try {
            session.getRemote().sendString("Created DB and Started.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static int inputNewPort(Scanner sc) {
        System.out.println("Enter port for new server (>1024 and exclude 19194)");
        int port = sc.nextInt();
        if (!(port < 1024)) {
            JSONArray array = new JSONArray(getDB());
            for (Object o : array) {
                Map<String, Object> jo = new JSONObject(o.toString()).toMap();
                for (Object x : jo.values()) {
                    if(Integer.parseInt(x.toString()) == port){
                        System.err.println("Please enter port such that port>1024, exclude 19194 and all other ports of different servers");
                        inputNewPort(sc);
                        break;
                    }
                }
            }
        }else {
            System.err.println("Please enter port such that port>1024, exclude 19194 and all other ports of different servers");
            inputNewPort(sc);
        }
        return port;
    }

    public static void updateCred(JSONObject rootName) {
        FileWriter myWriter;
        try {
            myWriter = new FileWriter(credFile);
            myWriter.write(rootName.toString());
            myWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
