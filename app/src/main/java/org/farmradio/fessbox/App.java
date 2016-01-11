package org.farmradio.fessbox;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class App extends Application {

    public static final String ACTION_LAUNCH_MAIN = "org.farmradio.fessbox.intent.action.ACTION_LAUNCH_MAIN";

    public static final String MESSAGE = "org.farmradio.fessbox.intent.action.MESSAGE";

    public static final String CHANNEL_LIST_UPDATE = "org.farmradio.fessbox.intent.action.CHANNEL_LIST_UPDATE";

    public static final String MASTER_UPDATE = "org.farmradio.fessbox.intent.action.MASTER_UPDATE";

    private JSONObject state;

    private ActionReceiver receiver;

    public App() {
        super();

        state = new JSONObject();
        /*
        try {
            state = new JSONObject(
                "{\"channels\": { \"chan_1\": {\"number\": \"+255 123 123 132\"}, \"chan_2\": {}, \"chan_3\": {} } }"
            );
        } catch (JSONException exception) {
            state = new JSONObject();
        }
        */
    }

    @Override
    public void onCreate() {
        super.onCreate();

        receiver = new ActionReceiver();

        IntentFilter filter = new IntentFilter();
        filter.addAction(App.MESSAGE);
        filter.addCategory(Intent.CATEGORY_DEFAULT);

        registerReceiver(receiver, filter);
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

    public JSONObject getMaster() {
        try {
            return state.getJSONObject("master");
        } catch (JSONException exception) {
            return new JSONObject();
        }
    }

    private void updateChannels(JSONObject data, ChannelUpdateHandler updater) {
        JSONObject channels = state.optJSONObject("channels");
        if (channels == null) {
            channels = new JSONObject();
        }
        updater.setChannels(channels);
        Iterator<String> it = data.keys();
        while (it.hasNext()) {
            String key = it.next();
            updater.update(key, channels.optJSONObject(key));
        }
        try {
            state.put("channels", channels);
        } catch (JSONException e) {
            Log.e("FessBox", e.toString());
        }
    }

    class ActionReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(App.MESSAGE)) {
                try {
                    JSONObject message = new JSONObject(intent.getStringExtra("payload"));

                    Log.d("FessBox", message.toString());

                    switch (message.optString("event")) {

                        case "initialize": {
                            JSONObject data = message.getJSONObject("data");
                            state = data.getJSONObject("mixer");
                            sendBroadcast(new Intent(App.CHANNEL_LIST_UPDATE));
                            sendBroadcast(new Intent(App.MASTER_UPDATE));

                            Log.d("FessBox", "state : " + state.toString());

                            break;
                        }
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
                            sendBroadcast(new Intent(App.CHANNEL_LIST_UPDATE));

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
                            sendBroadcast(new Intent(App.CHANNEL_LIST_UPDATE));

                            Log.d("FessBox", "channelVolumeChange");
                            break;
                        }
                        case "masterUpdate": {
                            JSONObject data = message.getJSONObject("data");
                            state.put("master", data);
                            sendBroadcast(new Intent(App.MASTER_UPDATE));

                            Log.d("FessBox", "masterUpdate");
                            break;
                        }
                        case "masterVolumeChange": {
                            JSONObject master = state.optJSONObject("master");
                            if (master == null) {
                                master = new JSONObject();
                            }
                            int level = message.optInt("data");
                            master.put("level", level);
                            state.put("master", master);
                            sendBroadcast(new Intent(App.MASTER_UPDATE));

                            Log.d("FessBox", "masterVolumeChange: " + level);
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
                } catch (JSONException exception) {
                    Log.e("FessBox", exception.toString());
                }
            }
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
