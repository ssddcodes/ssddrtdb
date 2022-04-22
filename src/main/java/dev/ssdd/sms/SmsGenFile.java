package dev.ssdd.sms;

import dev.ssdd.zot.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class SmsGenFile {
    static void fileCheck(File dbfile,File htmfile, String htmfin,String dbid){
        if(!htmfile.exists()){
            try {
                htmfile.getParentFile().mkdirs();
                htmfile.createNewFile();
                dbfile.createNewFile();
//                credfile.createNewFile();
                writeFile(htmfile, htmfin);
                writeFile(dbfile, new JSONObject().put(dbid, new JSONObject()).toString());
//                writeFile(credfile, new JSONObject().put("uname", dbid).toString());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }

    static void writeFile(File htmfile, String htmfin) {
        FileWriter myWriter;
        try {
            myWriter = new FileWriter(htmfile);
            myWriter.write(htmfin);
            myWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
