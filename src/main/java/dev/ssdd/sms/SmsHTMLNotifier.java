package dev.ssdd.sms;

import dev.ssdd.rtdb.WebSocket;
import org.eclipse.jetty.websocket.api.Session;

import java.util.Collection;

public class SmsHTMLNotifier {
    public SmsFileMaster g;
    public boolean check = false;

    void gitJSON(Session session, String dbid) {
        g = new SmsFileMaster(dbid) {
            @Override
            public void readFileResult(String json) {
                semd(json, session);
                check = true;
            }
        };
        g.readFileResult();
    }

    void updateJSON(Object json, String dbid, Collection<SmsValueEveResponder> childrenTradersx, Collection<SmsSingleValueEveResponder> singleTradersx, Collection<SmsHTMLNotifier> notifiers) {
        new SmsFileMaster(dbid) {
            @Override
            public void readFileResult(String json) {

            }
        }.updateFile(json.toString(), childrenTradersx, singleTradersx, notifiers);
    }

    private void semd(String msg, Session session) {
        WebSocket.sendClient(session, msg);
    }
}
