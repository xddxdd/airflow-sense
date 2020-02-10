package lantian.airflowsense;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.net.Uri;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import lantian.airflowsense.FileManager.FileManager;
import lantian.airflowsense.authorization.LoginPage;
import lantian.airflowsense.receiver.BLEReceiveService;
import lantian.airflowsense.receiver.SampleDataReceiveService;
import lantian.airflowsense.weather.WeatherCallback;
import lantian.airflowsense.weather.WeatherData;
import lantian.airflowsense.weather.WeatherHelper;
import lantian.airflowsense.FloatingService.FloatWindowService;

public class MainActivity extends AppCompatActivity {

    BLEUpdateReceiver bleUpdateReceiver = new BLEUpdateReceiver(); // The BroadcastReceiver that listens to the new data income and the change of connection status
    FloatWindowReceiver floatWindowReceiver = new FloatWindowReceiver();
    WeatherHelper weatherHelper = new WeatherHelper(); // A manager that get weather information from HeWeather App

    private String UserName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        registerFloatWindowReceiver();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if (BLEReceiveService.isConnected()){ // For real implement
                if (SampleDataReceiveService.isConnected()){ // For test
                    startFloatingService();
                }else {
                    Toast.makeText(MainActivity.this, "蓝牙未连接", Toast.LENGTH_SHORT).show();
                }
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

        switch (item.getItemId()){
            case R.id.login_btn:
                if (UserName.isEmpty()){
                    startActivityForResult(new Intent(MainActivity.this, LoginPage.class), Common.RequestCode.REQ_LOGIN);
                }
                break;
            case R.id.ble_btn:
//                if (BLEReceiveService.isConnected()){ // For real implement
                if (SampleDataReceiveService.isConnected()){ // For test
                    stopBLEReceiveService();
                }else {
                    startBLEReceiveService();
                }
                break;
            case R.id.switch_user:
                if (!UserName.isEmpty()) {
                    startActivityForResult(new Intent(MainActivity.this, LoginPage.class), Common.RequestCode.REQ_LOGIN);
                }
                break;
            case R.id.menu_upload:
                refreshWeather();
                break;
            case R.id.menu_delete:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        if (!UserName.isEmpty()) {
            menu.findItem(R.id.login_btn).setTitle(UserName);
        }else {
            menu.findItem(R.id.login_btn).setTitle("登录");
        }
//        if (BLEReceiveService.isConnected()){ // For real implement
        if (SampleDataReceiveService.isConnected()){ // For test
            menu.findItem(R.id.ble_btn).setTitle("断开蓝牙");
        }else {
            menu.findItem(R.id.ble_btn).setTitle("连接蓝牙");
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    public void onPause(){
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Common.RequestCode.REQ_OVERLAY_PERMISSION) {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "授权失败", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "授权成功", Toast.LENGTH_SHORT).show();
                startService(new Intent(MainActivity.this, FloatWindowService.class));
            }
        }else if (requestCode == Common.RequestCode.REQ_LOGIN){
            if (resultCode == RESULT_OK){
                UserName = data.getStringExtra(Common.PacketParams.USER_NAME);
            }
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        stopFloatingService();
        unRegisterFloatWindowReceiver();
        stopBLEReceiveService();
    }

    /*------------------------------------------------------- Private Helper Functions -------------------------------------------------------*/

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

    private void startFloatingService(){
        if (!Settings.canDrawOverlays(this)){
            Toast.makeText(this, "当前无权限，请授权", Toast.LENGTH_SHORT).show();
            startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())), Common.RequestCode.REQ_OVERLAY_PERMISSION);
        }else {
            startService(new Intent(MainActivity.this, FloatWindowService.class));
        }
    }

    private void stopFloatingService(){
        if (FloatWindowService.isEnabled()){
            stopService(new Intent(MainActivity.this, FloatWindowService.class));
        }
    }

    /**
     * startBLEReceiveService
     * Start the BLEReceiveService
     */
    private void startBLEReceiveService() {
        /* For real implement */
//        if (!BLEReceiveService.RUNNING) {
//            startService(new Intent(this, BLEReceiveService.class));
//        }
        /* For test */
        if (!SampleDataReceiveService.RUNNING){
            startService(new Intent(this, SampleDataReceiveService.class));
        }
    }

    /**
     * stopBLEReceiveService
     * Stop the BLEReceiverService
     */
    private void stopBLEReceiveService() {
        /* For real implement */
//        if (BLEReceiveService.RUNNING){
//            stopService(new Intent(this, BLEReceiveService.class));
//        }
        /* For test */
        if (SampleDataReceiveService.RUNNING){
            stopService(new Intent(this, SampleDataReceiveService.class));
        }
    }

    /**
     * registerBLEReceiver
     * Call the onReceive function in bleUpdateReceiver whenever new data point is received (BROADCAST_DATA_UPDATE)
     * or bluetooth connection status is changed (BROADCAST_CONNECTION_STATUS_UPDATE)
     */
    private void registerBLEReceiver(){
        IntentFilter intentFilter = new IntentFilter();
        /* Set up the intentFilter */
        intentFilter.addAction(Common.Action.BROADCAST_DATA_UPDATE);
        intentFilter.addAction(Common.Action.BROADCAST_CONNECTION_STATUS_UPDATE);
        registerReceiver(bleUpdateReceiver, intentFilter); // Register the BroadcastReceiver
    }

    private void unRegisterBLEReceiver(){
        unregisterReceiver(bleUpdateReceiver); // Unregister the BroadcastReceiver
    }

    private void registerFloatWindowReceiver(){
        IntentFilter intentFilter = new IntentFilter();
        /* Set up the intentFilter */
        intentFilter.addAction(Common.Action.FLOAT_WINDOW_STATUS_UPDATE);
        registerReceiver(floatWindowReceiver, intentFilter); // Register the BroadcastReceiver
    }

    private void unRegisterFloatWindowReceiver(){
        unregisterReceiver(floatWindowReceiver); // Unregister the BroadcastReceiver
    }

    /*------------------------------------------------------- Data Receiver Class -------------------------------------------------------*/

    /**
     * DataUpdateReceiver
     * The BroadcastReceiver that listens to the new data income and the change of connection status
     */
    private class BLEUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(Common.Action.BROADCAST_DATA_UPDATE.equals(intent.getAction())) {
                /* Get the new data point */
                final double new_value = intent.getDoubleExtra(Common.PacketParams.NEW_VALUE, 0.0);
                /* Get the plotter element */
                if (FloatWindowService.isEnabled()){
                    FloatWindowService.setFloatWindowData(new_value);
                    FileManager.addTempData(new_value);
                }

            } else if(Common.Action.BROADCAST_CONNECTION_STATUS_UPDATE.equals(intent.getAction())) {
                /* Connection status is changed */
                /* Get the present status and device's name */
                final boolean connected = intent.getBooleanExtra(Common.PacketParams.CONNECTIVITY, false);
                final String name = intent.getStringExtra("name");

                /* Update the UI */
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (getSupportActionBar() != null) {
                            if (connected) {
                                getSupportActionBar().setTitle(name);
                            } else {
                                getSupportActionBar().setTitle("连接中断");
                            }
                        }
                    }
                });
            }
        }
    }

    private class FloatWindowReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Common.Action.FLOAT_WINDOW_STATUS_UPDATE.equals(intent.getAction())){
                if (intent.getBooleanExtra(Common.PacketParams.FLOAT_WINDOW_SHOW, false)){
                    FileManager.createTempFile();
                    registerBLEReceiver();
                }else {
                    unRegisterBLEReceiver();
                    final EditText fileName = new EditText(MainActivity.this);
                    fileName.setText(FileManager.getDefaultFileName());

                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("测量结束")
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .setView(fileName).
                            setPositiveButton("保存", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    boolean data_saved = FileManager.saveData(UserName, fileName.getText().toString());
                                    if (!data_saved)
                                        Toast.makeText(MainActivity.this, "数据储存失败", Toast.LENGTH_SHORT).show();
                                }})
                            .setNegativeButton("放弃", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    FileManager.dumpData();
                                }})
                            .show();
                }
            }
        }
    }
}