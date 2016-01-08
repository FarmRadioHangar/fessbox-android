package org.farmradio.fessbox;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class LaunchActivity extends AppCompatActivity {

    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        progress = new ProgressDialog(this);
        progress.setTitle("Please wait");
        progress.setMessage("Connecting to service.");
        progress.show();

        new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(App.LAUNCH_MAIN_ACTIVITY)) {
                    progress.dismiss();
                    finish();

                    Intent main = new Intent(LaunchActivity.this, MainActivity.class);
                    startActivity(main);
                }
            }
        };

        //connect();
    }

}

