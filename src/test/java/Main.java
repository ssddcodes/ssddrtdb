import dev.ssdd.rtdb.SingleValueEventListener;
import dev.ssdd.rtdb.Zot;
import org.jetbrains.annotations.Nullable;

import java.net.URISyntaxException;

public class Main {
    public static void main(String[] args) throws URISyntaxException {
        Zot s = new Zot().instance("ws://127.0.0.1:19194/");
        s.child("abc/xyz/xyz1");
        s.setValue("ssdd");
        s.child("abc/xyz/xyz1");
        s.addSingleValueEventListener(new SingleValueEventListener() {
            @Override
            public void onDataChange(@Nullable Object data) {
                System.out.println(data);
            }
        });
    }
}