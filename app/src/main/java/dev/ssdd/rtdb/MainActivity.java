package dev.ssdd.rtdb;

import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    WSClient WSClient;
    TextView textView;
    EditText editText;
    Button button, button2;

    private final Handler handler = new Handler();

    int y = 0;

    Interpreter interpreter;

    String TAG = "MainAc";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.txt);
        editText = findViewById(R.id.et);
        button = findViewById(R.id.btn);
        button2 = findViewById(R.id.btn2);

        editText.setText("abc/xyz");

        button2.setOnClickListener(view -> {
            interpreter = new Interpreter();
        });
        button.setOnClickListener(view -> {
            interpreter.child(editText.getText().toString()).setValue("yo");
        });


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