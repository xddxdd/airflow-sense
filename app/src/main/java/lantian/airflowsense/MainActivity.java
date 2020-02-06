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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import lantian.airflowsense.receiver.BLEReceiveService;
import lantian.airflowsense.weather.WeatherCallback;
import lantian.airflowsense.weather.WeatherData;
import lantian.airflowsense.weather.WeatherHelper;

public class MainActivity extends AppCompatActivity {

    WeatherHelper weatherHelper = new WeatherHelper(); // A manager that get weather information from HeWeather App

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        refreshWeather();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id){
            case R.id.action_settings:
                return true;
            case R.id.menu_upload:
                refreshWeather();
                break;
            case R.id.menu_delete:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * refreshWeather
     * Refresh the weather information when needed
     */
    private void refreshWeather() {

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
                    }
                });
            }
        });
    }
}