package lantian.airflowsense;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.util.LongSparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.net.Uri;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import lantian.airflowsense.FileManager.FileManager;
import lantian.airflowsense.RecyclerView.RecyclerViewManager;
import lantian.airflowsense.RecyclerView.SlideBlock;
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
    private RecyclerViewManager recyclerViewManager;

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
        recyclerViewManager = new RecyclerViewManager(this);
        retrieveFiles();
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
                uploadFiles();
                break;
            case R.id.menu_delete:
                /* Delete the data with checkbox checked */
                ArrayList<SlideBlock> checkedList = recyclerViewManager.getCheckedList();
                for (int i = 0; i < checkedList.size(); i++){
                    SlideBlock block = checkedList.get(i);
                    if (FileManager.removeFile(MainActivity.getUserName(), block.getFileName(), block.getPostfix())){
                        recyclerViewManager.removeSlideBlock(checkedList.remove(i--));
                    }
                }
                recyclerViewManager.updateView();
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
        /* Update the weather information */
        refreshWeather();
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
                retrieveFiles();
            }
            refreshWeather();
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

    private void retrieveFiles(){
        recyclerViewManager.clearView();
        File directory = FileManager.getDirectory(UserName);
        if (directory == null)
            return;
        String[] fileList = directory.list();
        if (fileList == null || fileList.length == 0)
            return;

        long[] dateArray = new long[fileList.length];
        LongSparseArray<String> date_file_map = new LongSparseArray<>();
        for (int i = 0; i < fileList.length; i++) {
            String s = fileList[i];
            int split_point = s.lastIndexOf('_');
            long postfix_num = Long.parseLong(s.substring(split_point + 1, s.length() - FileManager.fileType.length()));
            dateArray[i] = postfix_num;
            date_file_map.append(postfix_num, s);
        }
        Arrays.sort(dateArray);
        for (long i : dateArray) {
            String file_full_name = date_file_map.get(i);
            int split_point = file_full_name.lastIndexOf('_');
            Date date = new Date(i);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Common.Norms.DateFormat, Locale.CHINA);
            recyclerViewManager.addSlideView(new SlideBlock(
                    simpleDateFormat.format(date), // Date text
                    file_full_name.substring(0, split_point), // File name
                    file_full_name.substring(split_point, file_full_name.length() - FileManager.fileType.length()) // Postfix
            ));
        }
        recyclerViewManager.updateView();
    }

    private void uploadFiles(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<SlideBlock> blocks = recyclerViewManager.getCheckedList();
                try {
                    JSONObject send_msg = new JSONObject();
                    String user_name = UserName.isEmpty() ? Common.Norms.DEFAULT_USER_NAME : UserName;
                    send_msg.put(Common.PacketParams.USER_NAME, user_name);
                    send_msg.put(Common.PacketParams.OPERATION, Common.Operation.UPLOAD);
                    send_msg.put(Common.PacketParams.PACKET_NUM, blocks.size());

                    JSONArray packets = new JSONArray();
                    for (int i = 0; i < blocks.size(); i++){
                        SlideBlock block = blocks.get(i);
                        File file = FileManager.getFile(UserName, block.getFileName(), block.getPostfix());
                        if (file == null){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, "文件加载失败", Toast.LENGTH_SHORT).show();
                                }
                            });
                            return;
                        }
                        FileInputStream inputStream = new FileInputStream(file);
                        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                        byte[] input_byte = new byte[1024];
                        int len;
                        while ((len = inputStream.read(input_byte)) != -1){
                            buffer.write(input_byte, 0, len);
                        }
                        inputStream.close();
                        JSONObject object = new JSONObject();
                        object.put(Common.PacketParams.AIRFLOW_DATA, buffer.toString("UTF-8"));
                        packets.put(object);
                    }
                    send_msg.put(Common.PacketParams.PACKETS, packets);

                    Socket socket = new Socket(Common.Norms.SERVER_IP, Common.Norms.SERVER_PORT);
                    try {
                        OutputStream os = socket.getOutputStream();
                        os.write(send_msg.toString().getBytes(StandardCharsets.UTF_8));
                        socket.shutdownOutput();

                        InputStream is = socket.getInputStream();
                        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                        byte[] reply_byte = new byte[128];
                        int len;
                        while ((len = is.read(reply_byte)) != -1){
                            buffer.write(reply_byte, 0, len);
                        }

                        JSONObject reply_msg = new JSONObject(buffer.toString());
                        final int result = reply_msg.getInt(Common.PacketParams.INSTRUCTION);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (result == Common.Instruction.APPROVED){
                                    Toast.makeText(MainActivity.this, "上传成功", Toast.LENGTH_SHORT).show();
                                }else {
                                    Toast.makeText(MainActivity.this, "上传失败", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    socket.close();

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();

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
                    Date date = new Date(System.currentTimeMillis());
                    final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Common.Norms.DateFormat, Locale.CHINA);
                    String date_text = simpleDateFormat.format(date);
                    fileName.setText(date_text);

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
                                        Date file_date = new Date(Long.parseLong(postfix.substring(1)));
                                        recyclerViewManager.addSlideView(new SlideBlock(simpleDateFormat.format(file_date), file_name, postfix));
                                        recyclerViewManager.updateView();
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