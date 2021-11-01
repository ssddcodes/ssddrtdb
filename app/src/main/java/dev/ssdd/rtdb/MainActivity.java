package dev.ssdd.rtdb;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.net.URI;
import java.net.URISyntaxException;

import dev.ssddRtdbClient.rtdb.R;

public class MainActivity extends AppCompatActivity {
    EditText editText;
    Button button;
    RecyclerView recyclerView;
    Adapter adapter;

    String TAG = "MainAc";
    boolean ijCon = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText = findViewById(R.id.et);
        button = findViewById(R.id.btn);
        recyclerView = findViewById(R.id.recy);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        TextView textView = findViewById(R.id.tmx);

        editText.setText("abc/xyz/xyz1");

        try {
            SSDD ssdd = new SSDD(new URI("ws://192.168.0.105:19194/"));
            ssdd.child(editText.getText().toString());

            for (int i = 0; i < 100; i++) {
                ssdd.children.clear();
                ssdd.child(editText.getText().toString()).push().setValue(i);
                Log.d(TAG, "onCreate: "+i);
            }

            ssdd.addSingleValueEventListener(new SingleValueEventListener() {
                @Override
                public void onDataChange(@Nullable Object o) {
                    runOnUiThread(()->{
                        assert o != null;
                        runOnUiThread(()->{
                            textView.setText(o.toString());
                        });
                    });
                }

                @Override
                public void onError(@Nullable Exception e) {

                }
            });
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

    }
}
/*
    public Interpreter child(String path){
        if(isRunun){
            StringBuilder sb = new StringBuilder(path);
            try {
                if(childrenChecker(path)){
                    if(!path.startsWith("/")){
                        path = "/"+path;
                        if(path.endsWith("/")){
                            path = sb.deleteCharAt(path.length()-1).toString();
                            children = children+path;
                        }
                    }else {
                        if(path.endsWith("/")){
                            path = sb.deleteCharAt(path.length()-1).toString();
                            children = children+path;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return this;
    }

    private boolean childrenChecker(String children) throws Exception{
        if(children.contains("\"")){
            throw new Exception("Child:- you can not put double inverted commas \"\" in children");
        }else {
            return true;
        }
    }
    public void setValue(String value){
        if(isRunun){
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("id","sv");
                String x  = children+"="+value;
                jsonObject.put("message",x);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    public void setValue(Object value){
        if(isRunun){
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out;
            try {
                out = new ObjectOutputStream(bos);
                out.writeObject(value);
                out.flush();
                byte[] bytes = bos.toByteArray();
                wsClient.send(bytes);

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    bos.close();
                } catch (IOException ex) {
                    // ignore close exception
                }
            }
        }
    }
 */