package dev.ssdd.sms;

import java.io.*;
import java.util.Collection;

public abstract class SmsFileMaster {
    private String oldJson = "";
    File dbfile;
    public SmsFileMaster(String dbid) {
        dbfile = new File(System.getProperty("user.home") + File.separator + ".ssddrtdb" + File.separator + dbid + File.separator + "db.json");
    }

    public abstract void readFileResult(String json);
    void readFileResult(){
        readFile();
    }
    private synchronized void readFile() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(dbfile));
            readFileResult(oldJson = br.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void updateFile(String json, Collection<SmsValueEveResponder> childrenTradersx,Collection<SmsSingleValueEveResponder> singleTradersx,Collection<SmsHTMLNotifier> notifiers) {
        try {
            FileWriter myWriter = new FileWriter(dbfile);
            myWriter.write(json);
            myWriter.close();

            if (!oldJson.equals(json)) {
                for (SmsValueEveResponder t : childrenTradersx) {
                    if(t!=null){
                        if (t.check) {
                            oldJson = json;
                            t.g.readFileResult(json);
                        }
                    }
                }
                for (SmsSingleValueEveResponder t2 : singleTradersx) {
                    if(t2!=null){
                        if (t2.check) {
                            oldJson = json;
                            t2.g.readFileResult(json);
                        }
                    }
                }
                for (SmsHTMLNotifier t3 : notifiers) {
                    if(t3!=null){
                        if (t3.check) {
                            oldJson = json;
                            t3.g.readFileResult(json);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
