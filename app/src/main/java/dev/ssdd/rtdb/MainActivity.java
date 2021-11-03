package dev.ssdd.rtdb;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.net.URI;
import java.net.URISyntaxException;

public class MainActivity extends AppCompatActivity {

    TextView t;
    EditText et;
    Button button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        t= findViewById(R.id.tv);
        et = findViewById(R.id.et);
        button = findViewById(R.id.btm);

        try {
            SSDD ssdd = new SSDD(new URI("ws://10.42.0.1:19194/"));
            et.setText("abc/xyz/xyz1");
            ssdd.child(et.getText().toString());

            et.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    ssdd.setValue(s);
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            button.setOnClickListener(v->{
                for (int i = 0; i < 1000; i++) {
                    ssdd.setValue(i);
                }
            });

            ssdd.addSingleValueEventListener(new SingleValueEventListener() {
                @Override
                public void onDataChange(@org.jetbrains.annotations.Nullable Object data) {
                    runOnUiThread(() -> {
                        assert data != null;
                        t.setText(data.toString());
                    });
                }

                @Override
                public void onError(@org.jetbrains.annotations.Nullable Exception e) {

                }
            });
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}