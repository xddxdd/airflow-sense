package lantian.airflowsense;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import lantian.airflowsense.receiver.BLEReceiveService;
import lantian.airflowsense.weather.WeatherCallback;
import lantian.airflowsense.weather.WeatherData;
import lantian.airflowsense.weather.WeatherHelper;

public class MainActivity extends AppCompatActivity {
    DataUpdateReceiver dataUpdateReceiver = new DataUpdateReceiver();
    IntentFilter intentFilter = new IntentFilter();
    WeatherHelper weatherHelper = new WeatherHelper();

    private void refreshWeather() {
        findViewById(R.id.button_refresh_weather).setEnabled(false);

        final TextView textWeather = findViewById(R.id.text_weather);
        final TextView textUpdateTime = findViewById(R.id.text_update_time);
        final TextView textTemperature = findViewById(R.id.text_temperature);
        final TextView textLocation = findViewById(R.id.text_location);
        final TextView textHumidity = findViewById(R.id.text_humidity);
        final TextView textAQI = findViewById(R.id.text_aqi);

        textWeather.setText("正在刷新");
        textUpdateTime.setText("---");
        textTemperature.setText("--");
        textLocation.setText("---");
        textHumidity.setText("--");
        textAQI.setText("---");

        weatherHelper.fetchWeatherAsync(this, new WeatherCallback() {
            @Override
            public void callback(final WeatherData data) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (data.isReady()) {
                                textWeather.setText(data.weather);
                                textUpdateTime.setText(data.timestamp);
                                textTemperature.setText(data.temperature);
                                textLocation.setText(data.location);
                                textHumidity.setText(data.humidity);
                                textAQI.setText(data.aqi + " (" + data.aqi_level + ")");
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
                        findViewById(R.id.button_refresh_weather).setEnabled(true);
                    }
                });
            }
        });
    }

    private BLEReceiveService serviceInstance;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            serviceInstance = ((BLEReceiveService.MyBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceInstance = null;
        }
    };

    private void startDataReceiveService() {
        startService(new Intent(this, BLEReceiveService.class));
    }

    private void stopDataReceiveService() {
        stopService(new Intent(this, BLEReceiveService.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        intentFilter.addAction(Common.BROADCAST_DATA_UPDATE);
        intentFilter.addAction(Common.BROADCAST_CONNECTION_STATUS_UPDATE);
        registerReceiver(dataUpdateReceiver, intentFilter);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        startDataReceiveService();

        findViewById(R.id.button_refresh_weather).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshWeather();
            }
        });

        refreshWeather();
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
//        startDataReceiveService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopDataReceiveService();
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
                        dpv.getRecentDataAverage(1)
                );

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dpv.addDataPoint(new_value);
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
                final boolean connected = intent.getBooleanExtra("connected", false);
                final String name = intent.getStringExtra("name");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(connected) {
//                            getSupportActionBar().setTitle("呼吸监测设备 已连接");
                            getSupportActionBar().setTitle(name + " 已连接");
                        } else {
//                            getSupportActionBar().setTitle("呼吸监测设备 连接中断");
                            getSupportActionBar().setTitle(name + " 连接中断");
                        }
                    }
                });
            }
        }
    }
}
