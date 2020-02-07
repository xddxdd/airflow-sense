package lantian.airflowsense.FloatingService;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import lantian.airflowsense.DataPlotView;
import lantian.airflowsense.R;

class FloatWindowView extends RelativeLayout{

    WindowManager windowManager;
    WindowManager.LayoutParams params;

    private float xInScreen; // The present X value of the finger in the screen
    private float yInScreen; // The present Y value of the finger in the screen
    private float xInWindow; // The present X value of the finger in the view
    private float yInWindow; // The present Y value of the finger in the view

    public FloatWindowView(Context context){
        super(context);

        /* Get the window manager */
        windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);

        /* Get the View object of the Float Window */
        LayoutInflater.from(context).inflate(R.layout.float_window, this);
        final View window = findViewById(R.id.float_window);

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

        findViewById(R.id.control_btn).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                FloatWindowManager.removeWindow();
            }
        });
        window.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                if (action == MotionEvent.ACTION_DOWN) {
                    // Record the position where the finger pressed on
                    xInWindow = event.getX();
                    yInWindow = event.getY();
                    xInScreen = event.getRawX();
                    yInScreen = event.getRawY();
                }else if (action == MotionEvent.ACTION_MOVE){
                    float move_x = (event.getRawX() - xInScreen) - (event.getX() - xInWindow);
                    float move_y = (event.getRawY() - yInScreen) - (event.getY() - yInWindow);
                    params.x += (int) move_x;
                    params.y += (int) move_y;
                    windowManager.updateViewLayout(window, params);
                }
                return false;
            }
        });
    }

    public void show(){
        windowManager.addView(this, params);
    }

    public void updateText (String text_max, String text_min, String text_ave, String text_now){
        ((TextView)findViewById(R.id.text_max)).setText(text_max);
        ((TextView)findViewById(R.id.text_min)).setText(text_min);
        ((TextView)findViewById(R.id.text_ave)).setText(text_ave);
        ((TextView)findViewById(R.id.text_now)).setText(text_now);
    }
}

