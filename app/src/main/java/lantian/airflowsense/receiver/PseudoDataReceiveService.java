package lantian.airflowsense.receiver;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.util.Random;

import lantian.airflowsense.norm.Common;

public class PseudoDataReceiveService extends Service {
    public static boolean RUNNING = false;

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (RUNNING) {
                double new_value = random.nextFloat();
                Intent intent = new Intent();
                intent.setAction(Common.BROADCAST_DATA_UPDATE);
                intent.putExtra("new_value", new_value);
                sendBroadcast(intent);
                Log.i(getClass().getSimpleName(), String.valueOf(new_value));

                handler.postDelayed(runnable, 16);
            } else {
                onDestroy();
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    private Random random = new Random();
    private Handler handler = new Handler();

    public class MyBinder extends Binder {
        public PseudoDataReceiveService getService() {
            return PseudoDataReceiveService.this;
        }
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
                intent.putExtra("name", "随机数产生器");
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
                intent.putExtra("name", "随机数产生器");
                sendBroadcast(intent);
            }
        });
        RUNNING = false;
    }
}
