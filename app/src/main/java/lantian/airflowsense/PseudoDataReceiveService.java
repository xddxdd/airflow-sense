package lantian.airflowsense;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.util.Random;

public class PseudoDataReceiveService extends Service {
    public static boolean RUNNING = false;

    private Random random = new Random();
    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            double new_value = random.nextFloat();
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

        handler.removeCallbacks(runnable);
        handler.post(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent();
                intent.setAction(Common.BROADCAST_CONNECTION_STATUS_UPDATE);
                intent.putExtra("connected", true);
                intent.putExtra("name", "Pseudo Sensor");
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
                intent.putExtra("name", "Pseudo Sensor");
                sendBroadcast(intent);
            }
        });
        RUNNING = false;
    }
}
