package lantian.airflowsense;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;

import android.net.Uri;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Calendar;

import lantian.airflowsense.FileManager.FileManager;
import lantian.airflowsense.ListView.ListViewManager;
import lantian.airflowsense.ListView.SlideBlock;
import lantian.airflowsense.authorization.LoginPage;
import lantian.airflowsense.receiver.SampleDataReceiveService;
import lantian.airflowsense.weather.WeatherCallback;
import lantian.airflowsense.weather.WeatherData;
import lantian.airflowsense.weather.WeatherHelper;
import lantian.airflowsense.FloatingService.FloatWindowService;

public class MainActivity extends AppCompatActivity {

    private BLEUpdateReceiver bleUpdateReceiver = new BLEUpdateReceiver(); // The BroadcastReceiver that listens to the new data income and the change of connection status
    private FloatWindowReceiver floatWindowReceiver = new FloatWindowReceiver();
    private WeatherHelper weatherHelper = new WeatherHelper(); // A manager that get weather information from HeWeather App

    private static String UserName = "";   // If the UserName is empty, it means no real user is accessing.
                                    // Empty UserName is a default user, which can access all functionality as a real user

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Setup toolbar */
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /* Request for Read and Write permission from the user */
        FileManager.init(this);

        /* Register the float window receiver */
        registerFloatWindowReceiver();

        /* Set the FloatingActionButton as a trigger of float window service */
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /* Start the float window service if the BLE device is connected */

//                if (BLEReceiveService.isConnected()){ // For real implement
                if (SampleDataReceiveService.isConnected()){ // For test
                    startFloatingService();
                }else {
                    Toast.makeText(MainActivity.this, "蓝牙未连接", Toast.LENGTH_SHORT).show();
                }
            }
        });

        /* Initialize the ListView manager */
        NestedScrollView scrollView = findViewById(R.id.scrollView);
        scrollView.smoothScrollTo(0, 0);
        ListViewManager.ListViewInit(this);

        /* Update the weather information */
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

        switch (item.getItemId()){
            case R.id.login_btn:
                /* If the login_btn is clicked and the UserName is empty(no real user), go to the Login page */
                if (UserName.isEmpty()){
                    startActivityForResult(new Intent(MainActivity.this, LoginPage.class), Common.RequestCode.REQ_LOGIN);
                }
                break;
            case R.id.ble_btn:
                /*
                 * When the ble_btn is clicked, two situations to be considered:
                 * 1. If no BLE device is not connected, start to find and connect one.
                 * 2. If the BLE device is connected, disconnect the device
                 */

//                if (BLEReceiveService.isConnected()){ // For real implement
                if (SampleDataReceiveService.isConnected()){ // For test
                    stopBLEReceiveService();
                }else {
                    startBLEReceiveService();
                }
                break;
            case R.id.switch_user:
                /* When the switch_user button is clicked, go to the Login page */
                startActivityForResult(new Intent(MainActivity.this, LoginPage.class), Common.RequestCode.REQ_LOGIN);
                break;
            case R.id.menu_upload:
                /* Upload the data with checkbox checked */
                refreshWeather();
                break;
            case R.id.menu_delete:
                /* Delete the data with checkbox checked */
                ListViewManager.removeSelectedSlideView();
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
            /* Get result from requesting overlay permission */
            if (!Settings.canDrawOverlays(this)) {
                /* If permission denied */
                Toast.makeText(this, "授权失败", Toast.LENGTH_SHORT).show();
            } else {
                /* If permission allowed */
                Toast.makeText(this, "授权成功", Toast.LENGTH_SHORT).show();
                /* Start the float window service */
                startService(new Intent(MainActivity.this, FloatWindowService.class));
            }
        }else if (requestCode == Common.RequestCode.REQ_LOGIN){
            /* Get result from login page */
            if (resultCode == RESULT_OK){
                /* If login successfully, update the UserName */
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Common.RequestCode.REQUEST_READ_WRITE_PERMISSION_CODE){
            for (int i = 0; i < permissions.length; i++){
                Log.i("MainActivity", "permission:" + permissions[i] + " " + grantResults[i]);
            }
        }
    }

    public static String getUserName(){
        return UserName;
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
            /* If access to overlay is denied, go to request for permission */
            Toast.makeText(this, "当前无权限，请授权", Toast.LENGTH_SHORT).show();
            startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())), Common.RequestCode.REQ_OVERLAY_PERMISSION);
        }else {
            /* If access to overlay is allowed, start the float window service */
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

    private String getCurrentDate(){
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.YEAR) + "年" + (calendar.get(Calendar.MONTH) + 1) + "月" + calendar.get(Calendar.DAY_OF_MONTH) + "日 ";
    }

    private String getCurrentTime(){
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.HOUR) + ":" + calendar.get(Calendar.MINUTE);
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
                /* If new data is received */
                /* Get the new data point */
                double new_value = intent.getDoubleExtra(Common.PacketParams.NEW_VALUE, 0.0);
                /* Get the plotter element */
                if (FloatWindowService.isEnabled()){
                    /* Send the new data to float window to update the view */
                    FloatWindowService.setFloatWindowData(new_value);
                    /* Add the new data to temporary storage */
                    FileManager.addTempData(new_value);
                }

            } else if(Common.Action.BROADCAST_CONNECTION_STATUS_UPDATE.equals(intent.getAction())) {
                /* If connection status is changed */
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
                /* If float window state(on-show or off-show) is changed */
                if (intent.getBooleanExtra(Common.PacketParams.FLOAT_WINDOW_SHOW, false)){
                    /* If float window state is switched on-show */

                    /* Create a temporary storage */
                    FileManager.createTempFile();
                    /* Enable BLE service broadcast receiver */
                    registerBLEReceiver();
                }else {
                    /* If float window state is switched off-show */

                    /* Unable BLE service broadcast receiver */
                    unRegisterBLEReceiver();
                    /* Create a EditText view for a AlertDialog to get file name */
                    final EditText fileName = new EditText(MainActivity.this);
                    /* Set a default filename */
                    final String date = getCurrentDate();
                    final String time = getCurrentTime();
                    fileName.setText(String.format("%s %s", date, time));

                    /* Launch a AlertDialog ask whether to save the data */
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("测量结束")
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .setView(fileName).
                            setPositiveButton("保存", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    /* If the user wants to save the file */
                                    String file_name = fileName.getText().toString();
                                    String postfix = FileManager.saveData(UserName, file_name);
                                    if (postfix != null) {
                                        ListViewManager.addSlideView(new SlideBlock(date, file_name, postfix));
                                    }else {
                                        Toast.makeText(MainActivity.this, "数据储存失败", Toast.LENGTH_SHORT).show();
                                    }
                                }})
                            .setNegativeButton("放弃", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    /* If the user doesn't want to save the file */
                                    FileManager.dumpData();
                                }})
                            .show();
                }
            }
        }
    }
}