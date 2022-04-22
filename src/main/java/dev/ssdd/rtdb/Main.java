package dev.ssdd.rtdb;

import dev.ssdd.ws.ZotWS;

public class Main {
    static ZotWS zotWS;
    public static void main(String[] args) {
        zotWS = new ZotWS();
        zotWS.port(19194);
        zotWS.webSocket("/zotws", WebSocket.class);
        zotWS.staticFileLocation("/");
        GenFile.fileCheck();
        zotWS.init();
        System.out.println("Started server at http://localhost:19194/index.html");
    }
}