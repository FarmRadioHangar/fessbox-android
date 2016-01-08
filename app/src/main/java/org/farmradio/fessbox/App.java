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

    public static final String LAUNCH_MAIN = "org.farmradio.fessbox.intent.action.LAUNCH_MAIN";

    public static final String NOTIFY = "org.farmradio.fessbox.intent.action.NOTIFY";

    public static final String CHANGE = "org.farmradio.fessbox.intent.action.CHANGE";

    private JSONObject state;

    private ActionReceiver receiver;

    public App() {
        super();

        try {
            state = new JSONObject(
                "{\"channels\": { \"chan_1\": {\"number\": \"+255 123 123 132\"}, \"chan_2\": {}, \"chan_3\": {} } }"
            );
        } catch (JSONException exception) {
            state = new JSONObject();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        receiver = new ActionReceiver();

        IntentFilter filter = new IntentFilter();
        filter.addAction(App.NOTIFY);
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

    private void notifyChange() {
        Intent intent = new Intent(App.CHANGE);
        sendBroadcast(intent);
    }

    class ActionReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(App.NOTIFY)) {
                try {
                    JSONObject message = new JSONObject(intent.getStringExtra("payload"));

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
                            notifyChange();

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
                            notifyChange();

                            Log.d("FessBox", "channelVolumeChange");
                            break;
                        }
                        case "masterUpdate": {
                            JSONObject data = message.getJSONObject("data");
                            state.put("master", data);
                            notifyChange();

                            Log.d("FessBox", "masterUpdate");
                            break;
                        }
                        case "masterVolumeChange": {
                            JSONObject master = state.optJSONObject("master");
                            if (master == null) {
                                master = new JSONObject();
                            }
                            master.put("level", message.optInt("data"));
                            notifyChange();

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
                } catch (JSONException exception) {
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
