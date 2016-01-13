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
            connection.connect("ws://192.168.1.143:8001", new WebSocketHandler() {

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

