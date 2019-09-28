package lantian.airflowsense.datareceiver;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import lantian.airflowsense.Common;

public class SampleDataReceiveService extends Service {
    public static boolean RUNNING = false;

    private List<Double> sampleData = new ArrayList<>();
    private int sampleDataPos = 0;

    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            double new_value = sampleData.get(sampleDataPos);
            sampleDataPos = (sampleDataPos + 1) % sampleData.size();

            Intent intent = new Intent();
            intent.setAction(Common.BROADCAST_DATA_UPDATE);
            intent.putExtra("new_value", new_value);
            sendBroadcast(intent);
            Log.i(getClass().getSimpleName(), String.valueOf(new_value));

            handler.postDelayed(runnable, 16);
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(getClass().getSimpleName(), "start");

        // Read sample data
        try {
            DataInputStream stream = new DataInputStream(
                    getAssets().open("sample_data/SDS00005.CSV.out")
            );
            Double data;
            while(stream.available() > 0) {
                data = stream.readDouble();
                sampleData.add(data);
            }
            Log.i(getClass().getSimpleName(), "got " + sampleData.size() + " data points");
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }

        handler.removeCallbacks(runnable);
        handler.post(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent();
                intent.setAction(Common.BROADCAST_CONNECTION_STATUS_UPDATE);
                intent.putExtra("connected", true);
                intent.putExtra("name", "演示数据");
                sendBroadcast(intent);
            }
        });
        handler.post(runnable);
        RUNNING = true;
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(runnable);
        handler.post(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent();
                intent.setAction(Common.BROADCAST_CONNECTION_STATUS_UPDATE);
                intent.putExtra("connected", false);
                intent.putExtra("name", "演示数据");
                sendBroadcast(intent);
            }
        });
        RUNNING = false;
    }
}
