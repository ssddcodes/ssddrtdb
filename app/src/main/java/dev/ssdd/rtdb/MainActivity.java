package dev.ssdd.rtdb;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.net.URI;
import java.net.URISyntaxException;

public class MainActivity extends AppCompatActivity {

    WebSocketClient webSocketClient;
    TextView textView;
    EditText editText;
    Button button;
    int y = 0;

    Interpreter interpreter;

    String TAG = "MainAc";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String TAG = "MainAc";
        textView = findViewById(R.id.txt);
        editText = findViewById(R.id.et);
        button = findViewById(R.id.btn);

        button.setOnClickListener(view -> {
            if(this.y == 0){
              // yo();
                //yoi();
                yoi();
                this.y++;
            }else {
                for (int i = 0; i < 1000; i++) {
                    interpreter.semd(String.valueOf(i));
                }
//                Log.d(TAG, "onCreate: "+ interpreter.push());

                // webSocketClient.send(editText.getText().toString());
            }
        });

    }

    void yoi(){
        interpreter = new Interpreter() {
            @Override
            public void onTxt(String msg) {
                runOnUiThread(()->{
                    textView.setText(msg);
                });
            }

            @Override
            public void onTxt(int msg) {
            }
        };
    }

    void yo(){
        try {
            URI uri = new URI(editText.getText().toString());
            webSocketClient = new WebSocketClient(uri) {

                @Override
                public void onTextReceived(String message) {
                    runOnUiThread(()->{
                        textView.setText(message);
                        Log.d(TAG, "onTextReceived: "+message);
                    });
                }

                @Override
                public void onTextReceived(int message) {
                }
            };
            webSocketClient.enableAutomaticReconnection(5000);
            webSocketClient.connect();

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

}