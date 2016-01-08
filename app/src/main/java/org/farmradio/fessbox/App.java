package org.farmradio.fessbox;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class App extends Application {

    private JSONObject state;

    private ChannelAdapter adapter;

    public App() {
        try {
            state = new JSONObject(
                "{\"channels\": { \"chan_1\": {}, \"chan_2\": {}, \"chan_3\": {} } }"
            );
        } catch (JSONException exception) {
            state = new JSONObject();
        }
    }

    public void setAdapter(ChannelAdapter adapter) {
        this.adapter = adapter;
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
            final JSONObject data = message.getJSONObject("data");

            switch (message.optString("event")) {

                case "channelUpdate":
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
                    this.adapter.notifyDataSetChanged();

                    Log.d("FessBox", "channelUpdate");
                    break;

                case "channelVolumeChange":
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
                    this.adapter.notifyDataSetChanged();

                    Log.d("FessBox", "channelVolumeChange");
                    break;

                case "masterUpdate":
                    break;

                case "masterVolumeChange":
                    break;

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

}

abstract class ChannelUpdateHandler {

    protected JSONObject channels;

    public void setChannels(JSONObject channels) {
        this.channels = channels;
    }

    abstract void update(String key, JSONObject channel);

}

class ChannelAdapter extends BaseAdapter {

    private static class ViewHolder {
        TextView textView;
        TextView channelNumber;
        Switch toggle;
        SeekBar seekBar;
        Spinner spinner;
    }

    private LayoutInflater inflater;

    private ArrayAdapter<CharSequence> modeSwitchAdapter;

    private App app;

    public ChannelAdapter(Context context, App app) {
        super();

        app.setAdapter(this);

        this.app = app;
        inflater = LayoutInflater.from(context);
        modeSwitchAdapter = ArrayAdapter.createFromResource(
                context,
                R.array.modes,
                android.R.layout.simple_spinner_dropdown_item);
    }

    @Override
    public int getCount() {
        return app.getChannelCount();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        return app.getChannel(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ViewHolder holder;
        if (view == null) {
            view = inflater.inflate(R.layout.list_view_item, parent, false);
            holder = new ViewHolder();
            holder.textView = (TextView) view.findViewById(R.id.textView);
            holder.channelNumber = (TextView) view.findViewById(R.id.channelNumber);
            holder.toggle = (Switch) view.findViewById(R.id.toggle);
            holder.seekBar = (SeekBar) view.findViewById(R.id.seekBar);
            holder.spinner = (Spinner) view.findViewById(R.id.spinner);
            holder.spinner.setAdapter(modeSwitchAdapter);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        JSONObject channel = (JSONObject) getItem(position);
        holder.textView.setText(channel.optString("id"));
        holder.channelNumber.setText(channel.optString("number"));
        holder.seekBar.setProgress(channel.optInt("level"));

        return view;
    }

}
