package dev.ssdd.rtdb;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    TextView textView;
    EditText editText;
    Button button, button2, button3;

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
        button3 = findViewById(R.id.btn3);

        editText.setText("abc/xyz");

        interpreter = new Interpreter();

        List<Model> models = new ArrayList<>();

        button3.setOnClickListener(v -> {
            interpreter.children2.clear();
            interpreter.child(editText.getText().toString());
            interpreter.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@Nullable List<DataSnapshot> dataSnapshots) {
                    if (dataSnapshots != null) {
                        for (DataSnapshot d : dataSnapshots) {
                            Model m = d.getValue(Model.class);
                            models.add(m);
                            Log.d(TAG, "onDataChange: "+m.getXyz());
                        }
                    }
                }

                @Override
                public void onError(@Nullable JSONException e) {

                }
            });

//            JSONObject j = new JSONObject();
//            String x = "{\"root\":{\"id\":\"id1\"},\"root1\":{\"id\":\"id2\"}}";
//
//            List<Model> models = new ArrayList<>();
//            DocumentContext context = JsonPath.parse(x);
//            List<String> xs = context.read("$.*");
//
//            String[] xx = context.read("$.*").toString().replace("[", "").replace("]", "").split(",");
//
//            try {
//                for (String xsa : xx) {
//                    JSONObject object = new JSONObject(xsa);
//                    Model m = get(xsa, Model.class);
//                    Log.d(TAG, "onCreate: " + m.getId());
//                }
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }

        });
        button.setOnClickListener(view -> {
            interpreter.children2.clear();
            if (editText.getText().toString().contains("=")) {
                interpreter.child(editText.getText().toString().split("=")[0]).setValue(editText.getText().toString().split("=")[1]);
            }
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