package dev.ssdd.rtdb;

import dev.ssdd.ws.ZotWS;

public class Main {
    static ZotWS zotWS;
    public static void main(String[] args) {

        if(args != null){
            for (int i = 0; i < args.length; i++) {
                if(args[i].equals("-pw")){
                    PewPew.verifypw(args[i+1]);
                    GenFile.bps = true;
                    break;
                }
            }
        }

        zotWS = new ZotWS();
        zotWS.port(19194);
        zotWS.webSocket("/zotws", WebSocket.class);
        zotWS.staticFileLocation("/");
        GenFile.fileCheck();
        zotWS.init();
        System.out.println("Started server at http://localhost:19194/index.html");
    }
}