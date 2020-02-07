package lantian.airflowsense.FloatingService;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class FloatWindowService extends Service {

    @Override
    public void onCreate(){
        super.onCreate();
        FloatWindowManager.init(getApplicationContext());
    }
    @Override
    public IBinder onBind(Intent intent){return null;}

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        FloatWindowManager.createWindow(getApplicationContext());
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        FloatWindowManager.removeWindow();
    }
}
