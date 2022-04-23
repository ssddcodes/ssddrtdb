import dev.ssdd.rtdb.SingleValueEventListener;
import dev.ssdd.rtdb.Zot;
import org.jetbrains.annotations.Nullable;

public class Main {
    public static void main(String[] args) {
        Zot zot = new Zot().instance("ws://localhost:19195/","ssdd");
        zot.child("abc/xyz/xyz1").addSingleValueEventListener(new SingleValueEventListener() {
            @Override
            public void onDataChange(@Nullable Object data) {
                System.out.println(data);
            }
        });
    }
}
