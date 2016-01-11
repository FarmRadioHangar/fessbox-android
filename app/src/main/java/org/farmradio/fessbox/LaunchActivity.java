package org.farmradio.fessbox;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;

public class LaunchActivity extends AppCompatActivity {

    private ProgressDialog progress;

    private ActionReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        progress = new ProgressDialog(this);
        progress.setTitle("Please wait");
        progress.setMessage("Connecting to service.");
        progress.show();

        receiver = new ActionReceiver();

        IntentFilter filter = new IntentFilter();
        filter.addAction(App.LAUNCH_MAIN);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(receiver, filter);

        Intent intent = new Intent(this, WebSocketService.class);
        startService(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(receiver);
    }

    class ActionReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(App.LAUNCH_MAIN)) {

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

                /*
                progress.dismiss();
                finish();

                Intent main = new Intent(LaunchActivity.this, MainActivity.class);
                startActivity(main);
                */

            }
        }

    }

}
