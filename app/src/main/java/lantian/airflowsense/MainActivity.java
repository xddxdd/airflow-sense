package lantian.airflowsense;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements Toolbar.OnMenuItemClickListener {
    DataUpdateReceiver dataUpdateReceiver = new DataUpdateReceiver();
    IntentFilter intentFilter = new IntentFilter();

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if(item.getItemId() == R.id.menu_connect) {
            // TODO: show search for device activity
            return true;
        }
        return false;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        intentFilter.addAction(Common.BROADCAST_DATA_UPDATE);
        intentFilter.addAction(Common.BROADCAST_CONNECTION_STATUS_UPDATE);
        registerReceiver(dataUpdateReceiver, intentFilter);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        startService(new Intent(this, PseudoDataReceiveService.class));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(dataUpdateReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(dataUpdateReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, PseudoDataReceiveService.class));
    }

    public class DataUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(Common.BROADCAST_DATA_UPDATE.equals(intent.getAction())) {
                final double new_value = intent.getDoubleExtra("new_value", 0.0);
                final DataPlotView dpv = findViewById(R.id.dataplot);

                // Ignore the latest data point in calculation purposely
                // To improve UI drawing performance
                // (Data inaccuracy here is insignificant anyways)
                final String recent_max = String.format(
                        Locale.SIMPLIFIED_CHINESE,
                        "%.2f",
                        dpv.getRecentDataMax(0)
                );
                final String recent_min = String.format(
                        Locale.SIMPLIFIED_CHINESE,
                        "%.2f",
                        dpv.getRecentDataMin(0)
                );
                final String recent_average = String.format(
                        Locale.SIMPLIFIED_CHINESE,
                        "%.2f",
                        dpv.getRecentDataAverage(16)
                );

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dpv.addDataPoint(new_value);
                        ((TextView) findViewById(R.id.textbox)).setText(recent_average);
                        ((TextView) findViewById(R.id.text_max_value)).setText(recent_max);
                        ((TextView) findViewById(R.id.text_min_value)).setText(recent_min);
                        ((TextView) findViewById(R.id.text_average_value)).setText(recent_average);
                    }
                });
            } else if(Common.BROADCAST_CONNECTION_STATUS_UPDATE.equals(intent.getAction())) {
                final boolean connected = intent.getBooleanExtra("connected", false);
                final String name = intent.getStringExtra("name");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(connected) {
                            getSupportActionBar().setTitle("Connected: " + name);
                        } else {
                            getSupportActionBar().setTitle("Disconnected: " + name);
                        }
                    }
                });
            }
        }
    }
}
