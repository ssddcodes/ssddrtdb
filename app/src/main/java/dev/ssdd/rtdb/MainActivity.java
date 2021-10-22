package dev.ssdd.rtdb;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    EditText editText;
    Button button;
    RecyclerView recyclerView;
    Adapter adapter;

    Interpreter interpreter;

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

        editText.setText("abc/xyz/xyz1");

        interpreter = new Interpreter();

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

        button.setOnClickListener(view -> {

            if(ijCon){
                interpreter.children2.clear();
                String[] x = editText.getText().toString().split("=");
                interpreter.child(x[0]);
                interpreter.setValue(x[1]);
            }else {
                if (!(interpreter.children2.size() < 1)){
                    interpreter.children2.clear();
                }
                interpreter.child(editText.getText().toString());
                interpreter.addSingleValueEventListener(new SingleValueEventListener() {
                    @Override
                    public void onDataChange(@Nullable Object o) {
                        runOnUiThread(() -> {
                            assert o != null;
                            Toast.makeText(MainActivity.this, o.toString(), Toast.LENGTH_SHORT).show();
                        });
                    }

                    @Override
                    public void onError(@Nullable Exception e) {
                    }
                });
                ijCon = true;
            }

/*
            interpreter.child("msgs");
            i++;
            integers.add(i);
            interpreter.setValue(integers);
*/

            //interpreter.child(editText.getText().toString().split("=")[0]).setValue(editText.getText().toString().split("=")[1]);
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