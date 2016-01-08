package org.farmradio.fessbox;

import android.app.Application;
import android.content.Intent;
import android.os.CountDownTimer;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;

import java.util.Iterator;

public class App extends Application {

    public static final String LAUNCH_MAIN_ACTIVITY = "org.farmradio.fessbox.intent.LAUNCH_MAIN_ACTIVITY";

    private JSONObject state;

    private final WebSocketConnection connection = new WebSocketConnection();

    private final ConnectionHandler connectionHandler = new ConnectionHandler();

    public App() {
        try {
            state = new JSONObject(
                "{\"channels\": { \"chan_1\": {}, \"chan_2\": {}, \"chan_3\": {} } }"
            );
        } catch (JSONException exception) {
            state = new JSONObject();
        }
    }

    public void startMainActivity() {

        Intent intent = new Intent();
        intent.setAction(LAUNCH_MAIN_ACTIVITY);
        sendBroadcast(intent);

        /*
        new CountDownTimer(2000, 1000) {

            @Override
            public void onFinish() {

                Intent intent = new Intent();
                intent.setAction(LAUNCH_MAIN_ACTIVITY);
                sendBroadcast(intent);

                //progress.dismiss();
                //finish();

                //Intent intent = new Intent(LaunchActivity.this, MainActivity.class);
                //startActivity(intent);
            }

            @Override
            public void onTick(long millisUntilFinished) { }

        }.start();
        */

        /*
        progress.dismiss();
        finish();

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        */
    }

    public int getChannelCount() {
        try {
            return state.getJSONObject("channels").length();
        } catch (JSONException exception) {
            return 0;
        }
    }

    public JSONObject getChannel(int position) {
        try {
            JSONObject channels = state.getJSONObject("channels");
            Iterator<String> it = channels.keys();
            for (int i = 0; i < position; i++) it.next();
            String key = it.next();
            return channels.getJSONObject(key).put("id", key);
        } catch (JSONException exception) {
            return new JSONObject();
        }
    }

    public void receiveMessage(String payload) {
        try {
            JSONObject message = new JSONObject(payload);

            switch (message.optString("event")) {

                case "channelUpdate": {
                    final JSONObject data = message.getJSONObject("data");
                    updateChannels(data, new ChannelUpdateHandler() {

                        @Override
                        public void update(String key, JSONObject channel) {
                            JSONObject json = data.optJSONObject(key);
                            if (json != null) {
                                try {
                                    channels.put(key, json);
                                } catch (JSONException e) {
                                    Log.e("FessBox", e.toString());
                                }
                            } else {
                                channels.remove(key);
                            }
                        }

                    });
                    //this.adapter.notifyDataSetChanged();

                    Log.d("FessBox", "channelUpdate");
                    break;
                }
                case "channelVolumeChange": {
                    final JSONObject data = message.getJSONObject("data");
                    updateChannels(data, new ChannelUpdateHandler() {

                        @Override
                        public void update(String key, JSONObject channel) {
                            if (channel == null) {
                                Log.e("FessBox", "No such channel: " + key);
                                return;
                            }
                            int value = data.optInt(key);
                            try {
                                channel.put("level", value);
                            } catch (JSONException e) {
                                Log.e("FessBox", e.toString());
                            }
                        }

                    });
                    //this.adapter.notifyDataSetChanged();

                    Log.d("FessBox", "channelVolumeChange");
                    break;
                }
                case "masterUpdate": {
                    JSONObject data = message.getJSONObject("data");
                    state.put("master", data);

                    Log.d("FessBox", "masterUpdate");
                    break;
                }
                case "masterVolumeChange": {
                    JSONObject master = state.optJSONObject("master");
                    if (master == null) {
                        master = new JSONObject();
                    }
                    master.put("level", message.optInt("data"));

                    Log.d("FessBox", "masterVolumeChange");
                    break;
                }
                case "userUpdate":
                    break;

                case "channelContactInfo":
                    break;

                case "inboxUpdate":
                    break;

                default:
                    break;
            }

        } catch (JSONException exception) { }
    }

    private void updateChannels(JSONObject data, ChannelUpdateHandler updater) {
        JSONObject channels = state.optJSONObject("channels");
        if (channels == null) {
            return;
        }
        updater.setChannels(channels);
        Iterator<String> it = data.keys();
        while (it.hasNext()) {
            String key = it.next();
            updater.update(key, channels.optJSONObject(key));
        }
    }

    private void connectWebSocket() {
        try {
            connection.connect("ws://192.168.1.143:8001", connectionHandler);
        } catch (WebSocketException exception) {
            Log.d("FessBox", exception.toString());
        }
    }

    class ConnectionHandler extends WebSocketHandler {

        @Override
        public void onOpen() {
            App.this.startMainActivity();

            Log.d("FessBox", "Status: Connected");
        }

        @Override
        public void onTextMessage(String payload) {
            App.this.receiveMessage(payload);

            Log.d("FessBox", "Got: " + payload);
        }

        @Override
        public void onClose(int code, String reason) {
            Log.d("FessBox", "Connection lost.");
        }

    }

}

abstract class ChannelUpdateHandler {

    protected JSONObject channels;

    public void setChannels(JSONObject channels) {
        this.channels = channels;
    }

    abstract void update(String key, JSONObject channel);

}
