package dev.ssdd.rtdb;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    WSClient WSClient;
    TextView textView;
    EditText editText;
    Button button;
    List<String> mesgs;
    RecyclerView recyclerView;

    private Handler handler = new Handler();

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
        recyclerView = findViewById(R.id.recy);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        button.setOnClickListener(view -> {
            if(this.y == 0){
              // yo();
                //yoi();
                yoi();
                this.y++;
            }else {
             //   Log.d(TAG, "onCreate: "+interpreter.push());
                for (int i = 0; i < 1000; i++) {
                    interpreter.semd(String.valueOf(i));
                //    Log.d(TAG, "onCreate: "+i);
                }
               // textView.setText(interpreter.push());
//                Log.d(TAG, "onCreate: "+ interpreter.push());

                // webSocketClient.send(editText.getText().toString());
            }
        });

    }

    void yoi(){
        interpreter = new Interpreter() {
            @Override
            public void onTxt(String msg) {
                handler.post(() -> setT(msg));
                // Log.d(TAG, "onTxt: "+msg);
            }
        };
    }

    private synchronized void setT(String msg) {
        textView.setText(msg);
    }

    void yo(){
        try {
            URI uri = new URI(editText.getText().toString());
            WSClient = new WSClient(uri) {

                @Override
                public void onTextReceived(String message) {
                        textView.setText(message);
                        Log.d(TAG, "onTextReceived: "+message);
                }
            };
            WSClient.enableAutomaticReconnection(5000);
            WSClient.connect();

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

}