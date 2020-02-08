package lantian.airflowsense.FloatingService;

import android.content.Context;
import android.view.WindowManager;

import org.jetbrains.annotations.NotNull;

public class FloatWindowManager {


    private static WindowManager windowManager; // The WindowManager

    private static FloatWindowView floatWindowView;


    public static void init(@NotNull Context context){
        windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
    }

    public static void createWindow(Context context) {
        if (floatWindowView == null) {
            floatWindowView = new FloatWindowView(context);
            floatWindowView.show();
        }
    }

    public static void removeWindow() {
        if (floatWindowView != null) {
            windowManager.removeView(floatWindowView);
            floatWindowView = null;
        }
    }

    public static void updateFloatWindowData(double new_value) {
        if (floatWindowView != null){
            floatWindowView.updateData(new_value);
        }
    }
}
