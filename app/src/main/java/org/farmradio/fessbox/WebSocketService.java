package org.farmradio.fessbox;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;

public class WebSocketService extends Service {

    private WebSocketConnection connection;

    private final IBinder binder = new WebSocketServiceBinder();

    private WebSocketServiceMessageListener listener;

	@Override
	public void onCreate() {
        super.onCreate();

        connection = new WebSocketConnection();
        connectWebSocket();
    }

    @Override
	public IBinder onBind(Intent intent) {
        return binder;
    }

	@Override
	public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

	@Override
	public boolean onUnbind(Intent intent) {
        return true;
    }

    @Override
	public void onDestroy() {
        super.onDestroy();
        connection.disconnect();
    }

    public void send(String payload) {
        connection.sendTextMessage(payload);
    }

    public class WebSocketServiceBinder extends Binder {
		public WebSocketService getService() {
			return WebSocketService.this;
		}

        public void setListener(WebSocketServiceMessageListener listener) {
            WebSocketService.this.listener = listener;
        }
	}

    private void connectWebSocket() {

        Log.d("FessBox", "Connect WebSocket");

        try {
            //192.168.1.38:19998
            //192.168.1.143:8001
            connection.connect("ws://172.17.0.1:8001", new WebSocketHandler() {

                @Override
                public void onOpen() {
                    Intent intent = new Intent(App.LAUNCH_MAIN);
                    sendBroadcast(intent);

                    Log.d("FessBox", "Status: Connected");
                }

                @Override
                public void onTextMessage(String payload) {
                    if (listener != null) {
                        listener.onMessage(payload);
                    }
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

}

/*
public class WebSocketService extends IntentService {

    private final WebSocketConnection connection = new WebSocketConnection();

    public WebSocketService() {
        super("websocket-service");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        connectWebSocket();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("FessBox", "Intent received!!!");
    }

    private void connectWebSocket() {

        Log.d("FessBox", "connect websocket");

        try {
            //192.168.1.38:19998
            //192.168.1.143:8001
            connection.connect("ws://192.168.1.38:19998", new WebSocketHandler() {

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

}
*/
