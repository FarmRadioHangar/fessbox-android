package org.farmradio.fessbox;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;

public class LaunchActivity extends AppCompatActivity {

    private final WebSocketConnection connection = new WebSocketConnection();

    private void connect() {
        final String uri = "ws://192.168.1.143:8001";
        try {
            connection.connect(uri, new WebSocketHandler() {

                @Override
                public void onOpen() {
                    Log.d("FessBox", "Status: Connected to " + uri);
                    //connection.sendTextMessage("Hello, world!");
                }

                @Override
                public void onTextMessage(String payload) {
                    Log.d("FessBox", "Got: " + payload);
                    App app = (App) getApplication();
                    app.receiveMessage(payload);
                }

                @Override
                public void onClose(int code, String reason) {
                    Log.d("FessBox", "Connection lost.");
                }

            });
        } catch (WebSocketException exception) {
            Log.d("FessBox", exception.toString());
        }
   }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ProgressDialog progress = new ProgressDialog(this);
        progress.setTitle("Please wait");
        progress.setMessage("Connecting to service.");
        progress.show();

        connect();

        new CountDownTimer(2000, 1000) {

            @Override
            public void onFinish() {
                progress.dismiss();
                finish();

                Intent intent = new Intent(LaunchActivity.this, MainActivity.class);
                startActivity(intent);
            }

            @Override
            public void onTick(long millisUntilFinished) { }

        }.start();

    }

}
