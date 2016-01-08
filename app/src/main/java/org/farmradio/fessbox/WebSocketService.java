package org.farmradio.fessbox;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;

public class WebSocketService extends IntentService {

    private final WebSocketConnection connection = new WebSocketConnection();

    public WebSocketService() {
        super("websocket-service");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            //192.168.1.38:19998
            connection.connect("ws://192.168.1.143:8001", new WebSocketHandler() {

                @Override
                public void onOpen() {
                    Intent intent = new Intent(App.ACTION_LAUNCH_MAIN);
                    sendBroadcast(intent);

                    Log.d("FessBox", "Status: Connected");
                }

                @Override
                public void onTextMessage(String payload) {
                    Intent intent = new Intent(App.MESSAGE);
                    intent.putExtra("payload", payload);
                    sendBroadcast(intent);

                    Log.d("FessBox", "Got: " + payload);
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
    protected void onHandleIntent(Intent intent) { }

}
