package org.farmradio.fessbox;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.widget.ListView;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private ChannelAdapter adapter;

    private ActionReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        adapter = new ChannelAdapter(this, (App) getApplication());
        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);

        receiver = new ActionReceiver();

        IntentFilter filter = new IntentFilter();
        filter.addAction(App.MASTER_UPDATE);
        filter.addAction(App.CHANNEL_LIST_UPDATE);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(receiver, filter);

        updateMaster();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private void updateMaster() {
        //Switch toggle = (Switch) findViewById(R.id.masterSwitch);
        SeekBar seekBar = (SeekBar) findViewById(R.id.masterSeekBar);
        App app = (App) getApplication();
        JSONObject master = app.getMaster();

        int level = master.optInt("level");

        Log.d("FessBox", "master level set to " + level);

        seekBar.setProgress(level);
    }

    class ActionReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(App.CHANNEL_LIST_UPDATE)) {

                Log.d("FessBox", "received CHANNEL_LIST_UPDATE");

                adapter.notifyDataSetChanged();
            } else if (action.equals(App.MASTER_UPDATE)) {

                Log.d("FessBox", "received MASTER_UPDATE");

                updateMaster();
            }
        }

    }

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
