package dev.ssdd.rtdb;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.jetbrains.annotations.Nullable;
import org.json.JSONException;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    TextView t;
    EditText et;
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        t = findViewById(R.id.tv);
        et = findViewById(R.id.et);
        button = findViewById(R.id.btm);

        try {
            SSDD ssdd = new SSDD("10.42.0.1:19194", "ws");

            et.setText("abc/xyz/xyz1");

            et.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                  //  ssdd.setValue(s);
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            button.setOnClickListener(v -> {
//                for (int i = 0; i < 1000; i++) {
//                    ssdd.setValue(i);
//                }
                ssdd.child(et.getText().toString());

                List<Model> models = new ArrayList<>();

                ssdd.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@Nullable List<DataSnapshot> dataSnapshots) {
                        if(dataSnapshots != null){
                            for (DataSnapshot d: dataSnapshots) {
                                models.add(d.getValue(Model.class));
                            }
                            for (Model m: models) {
                                runOnUiThread(()->{
                                    Toast.makeText(MainActivity.this, m.getXyz1(), Toast.LENGTH_LONG).show();
                                });
                                Log.d("TAG", "onDataChange: "+ m.getXyz1() + " "+ dataSnapshots.size());
                            }
                        }
                    }

                    @Override
                    public void onError(@Nullable JSONException e) {

                    }
                });
            });

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}