package lantian.airflowsense;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import lantian.airflowsense.norm.Common;
import lantian.airflowsense.receiver.BLEReceiveService;
import lantian.airflowsense.weather.WeatherCallback;
import lantian.airflowsense.weather.WeatherData;
import lantian.airflowsense.weather.WeatherHelper;

public class MainActivity extends AppCompatActivity {
    DataUpdateReceiver dataUpdateReceiver = new DataUpdateReceiver(); // The BroadcastReceiver that listens to the new data income and the change of connection status
    IntentFilter intentFilter = new IntentFilter();
    WeatherHelper weatherHelper = new WeatherHelper(); // A manager that get weather information from HeWeather App

    /**
     * refreshWeather
     * Refresh the weather information when needed
     */
    private void refreshWeather() {
        /* Disable the refresh_weather button */
        findViewById(R.id.button_refresh_weather).setEnabled(false);

        /* Get the entities of weather related TextView components */
        final TextView textWeather = findViewById(R.id.text_weather);
        final TextView textUpdateTime = findViewById(R.id.text_update_time);
        final TextView textTemperature = findViewById(R.id.text_temperature);
        final TextView textLocation = findViewById(R.id.text_location);
        final TextView textHumidity = findViewById(R.id.text_humidity);
        final TextView textAQI = findViewById(R.id.text_aqi);

        /* Print the waiting information */
        textWeather.setText("正在刷新");
        textUpdateTime.setText("---");
        textTemperature.setText("--");
        textLocation.setText("---");
        textHumidity.setText("--");
        textAQI.setText("---");

        /* Get the information */
        weatherHelper.fetchWeatherAsync(this, new WeatherCallback() { // WeatherCallback is an abstract class with only a callback function header
            @Override
            public void callback(final WeatherData data) {
                /* Run something on the main thread (UI Thread) */
                runOnUiThread(new Runnable() {
                    @Override
                    /* Code to be run on main thread */
                    public void run() {
                        /* Display the data/failure information onto the screen */
                        try {
                            if (data.isReady()) {
                                textWeather.setText(data.weather);
                                textUpdateTime.setText(data.timestamp);
                                textTemperature.setText(data.temperature);
                                textLocation.setText(data.location);
                                textHumidity.setText(data.humidity);
                                textAQI.setText(String.format("%s (%s)", data.aqi, data.aqi_level));
                            } else {
                                textWeather.setText("获取失败");
                                textUpdateTime.setText("---");
                                textTemperature.setText("--");
                                textLocation.setText("---");
                                textHumidity.setText("--");
                                textAQI.setText("---");
                                Toast.makeText(getApplicationContext(), data.error, Toast.LENGTH_LONG).show();
                            }
                        } catch (NullPointerException npe) {
                            npe.printStackTrace();
                        }
                        /* ReEnable the refresh_weather button */
                        findViewById(R.id.button_refresh_weather).setEnabled(true);
                    }
                });
            }
        });
    }

    /**
     * startDataReceiveService
     * Start the BLEReceiveService
     */
    private void startDataReceiveService() {
        if (!BLEReceiveService.RUNNING) {
            startService(new Intent(this, BLEReceiveService.class));
        }
    }

    /**
     * stopDataReceiveService
     * Stop the BLEReceiverService
     */
    private void stopDataReceiveService() {
        if (BLEReceiveService.RUNNING){
            stopService(new Intent(this, BLEReceiveService.class));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_connect:
                startDataReceiveService();
                return true;
            case R.id.menu_disconnect:
                stopDataReceiveService();
                return true;
            default:
                return false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /* Automatically generated code */
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Bind the activity_main.xml layout

        /* Set up the intentFilter (Registered in onResume function) */
        // Call the onReceive function in dataUpdateReceiver whenever new data point is received (BROADCAST_DATA_UPDATE)
        // or bluetooth connection status is changed (BROADCAST_CONNECTION_STATUS_UPDATE)
        intentFilter.addAction(Common.BROADCAST_DATA_UPDATE);
        intentFilter.addAction(Common.BROADCAST_CONNECTION_STATUS_UPDATE);

        /* Set up the toolbar */
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar); // Replace action bar with toolbar

        /* Set the onClick event handler of refresh_weather button */
        findViewById(R.id.button_refresh_weather).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshWeather();
            }
        });

        /* Refresh the weather when initializing */
        refreshWeather();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(dataUpdateReceiver, intentFilter); // Register the BroadcastReceiver with the well-setting intentFilter
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(dataUpdateReceiver); // Unregister the BroadcastReceiver
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopDataReceiveService();
    }

    /**
     * DataUpdateReceiver
     * The BroadcastReceiver that listens to the new data income and the change of connection status
     */
    public class DataUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(Common.BROADCAST_DATA_UPDATE.equals(intent.getAction())) {
                /* Get the new data point */
                final double new_value = intent.getDoubleExtra("new_value", 0.0);
                /* Get the plotter element */
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
                        dpv.getRecentDataAverage(1)
                );

                /* Process the new data point */
                dpv.addDataPoint(new_value);

                /* Update the UI */
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((TextView) findViewById(R.id.textbox)).setText(recent_average);
                        ((TextView) findViewById(R.id.text_max_value)).setText(recent_max);
                        ((TextView) findViewById(R.id.text_min_value)).setText(recent_min);
                        ((TextView) findViewById(R.id.text_average_value)).setText(recent_average);

                        ToggleButton tb = findViewById(R.id.measurement_button);
                        if (tb.isChecked()) {
                            ((TextView) findViewById(R.id.text_measurement_value)).setText(recent_max);
                        }
                    }
                });
            } else if(Common.BROADCAST_CONNECTION_STATUS_UPDATE.equals(intent.getAction())) {
                /* Connection status is changed */
                /* Get the present status and device's name */
                final boolean connected = intent.getBooleanExtra("connected", false);
                final String name = intent.getStringExtra("name");

                /* Update the UI */
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (getSupportActionBar() != null) {
                            if (connected) {
                                getSupportActionBar().setTitle(String.format("%s 已连接", name));
                            } else {
                                getSupportActionBar().setTitle(String.format("%s 连接中断", name));
                            }
                        }
                    }
                });
            }
        }
    }
}
