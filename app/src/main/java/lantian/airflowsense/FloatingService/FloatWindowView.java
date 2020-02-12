package lantian.airflowsense.FloatingService;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Locale;

import lantian.airflowsense.Common;
import lantian.airflowsense.DataPlotView;
import lantian.airflowsense.R;

class FloatWindowView extends RelativeLayout{

    private WindowManager windowManager;
    private WindowManager.LayoutParams params;
    private Context myContext;
    private Handler handler = new Handler();
    DataPlotView dataPlotView;

    private float xInScreen; // The present X value of the finger in the screen
    private float yInScreen; // The present Y value of the finger in the screen
    private int xViewInWindow; // The present X value of the finger in the view
    private int yViewInWindow; // The present Y value of the finger in the view

    public FloatWindowView(Context context){
        super(context);

        myContext = context;

        /* Get the window manager */
        windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);

        /* Get the View object of the Float Window */
        LayoutInflater.from(context).inflate(R.layout.float_window, this);
        final View window = findViewById(R.id.float_window);

        dataPlotView = findViewById(R.id.float_window_data_plot);
        DisplayMetrics dm = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(dm);
        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;

        /* Initialize the LayoutParams */
        params = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            params.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        params.format = PixelFormat.RGBA_8888;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        params.gravity = Gravity.START | Gravity.TOP;
        int viewWidth = window.getLayoutParams().width;
        int viewHeight = window.getLayoutParams().height;
        params.width = viewWidth;
        params.height = viewHeight;
        params.x = (screenWidth - viewWidth) / 2;
        params.y = (screenHeight - viewHeight) / 2;

        findViewById(R.id.float_window_control_btn).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent();
                        intent.setAction(Common.Action.FLOAT_WINDOW_STATUS_UPDATE);
                        intent.putExtra(Common.PacketParams.FLOAT_WINDOW_SHOW, false);
                        myContext.sendBroadcast(intent);
                    }
                });
                FloatWindowManager.removeWindow();
            }
        });

        window.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                if (action == MotionEvent.ACTION_DOWN) {
                    // Record the position where the finger pressed on
                    xViewInWindow = params.x;
                    yViewInWindow = params.y;
                    xInScreen = event.getRawX();
                    yInScreen = event.getRawY();
                }else if (action == MotionEvent.ACTION_MOVE){
                    float move_x = (event.getRawX() - xInScreen);
                    float move_y = (event.getRawY() - yInScreen);
                    params.x = (int) (xViewInWindow + move_x);
                    params.y = (int) (yViewInWindow + move_y);
                    windowManager.updateViewLayout(FloatWindowView.this, params);
                }
                return true;
            }
        });
    }

    public void show(){
        windowManager.addView(this, params);
        handler.post(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent();
                intent.setAction(Common.Action.FLOAT_WINDOW_STATUS_UPDATE);
                intent.putExtra(Common.PacketParams.FLOAT_WINDOW_SHOW, true);
                myContext.sendBroadcast(intent);
            }
        });
    }

    public void updateData (double new_data){
        // Ignore the latest data point in calculation purposely
        // To improve UI drawing performance
        // (Data inaccuracy here is insignificant anyways)
        final double now_text = new_data;

        post(new Runnable() {
            @Override
            public void run() {
                ((TextView)findViewById(R.id.float_window_text_max)).setText(String.format(
                        Locale.SIMPLIFIED_CHINESE,
                        "%.2f",
                        dataPlotView.getRecentDataMax(0)));

                ((TextView)findViewById(R.id.float_window_text_min)).setText(String.format(
                        Locale.SIMPLIFIED_CHINESE,
                        "%.2f",
                        dataPlotView.getRecentDataMin(0)));

                ((TextView)findViewById(R.id.float_window_text_ave)).setText(String.format(
                        Locale.SIMPLIFIED_CHINESE,
                        "%.2f",
                        dataPlotView.getRecentDataAverage(0)));

                ((TextView)findViewById(R.id.float_window_text_now)).setText(String.format(
                        Locale.SIMPLIFIED_CHINESE,
                        "%.2f",
                        now_text));
            }
        });
        dataPlotView.addDataPoint(new_data);
    }
}

