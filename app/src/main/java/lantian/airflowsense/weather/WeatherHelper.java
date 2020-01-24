package lantian.airflowsense.weather;

import android.content.Context;

import interfaces.heweather.com.interfacesmodule.bean.air.now.AirNow;
import interfaces.heweather.com.interfacesmodule.bean.weather.now.Now;
import interfaces.heweather.com.interfacesmodule.view.HeConfig;
import interfaces.heweather.com.interfacesmodule.view.HeWeather;

public class WeatherHelper {
    public WeatherHelper() {
        /* Initialize the HeWeather App */
        HeConfig.init("HE1909271417191305", "43fb431a8ab046b0b9f547df14f4c6e9"); // HeConfig.init("Your ID", "Your Key");
        HeConfig.switchToFreeServerNode(); // Free server for individual developer
    }

    /**
     * void fetchWeatherAsync
     * Use HeWeather App to get air and weather data and return back data using callback function
     * @param context The context
     * @param callback The callback object
     */
    public void fetchWeatherAsync(final Context context, final WeatherCallback callback) {
        final WeatherData data = new WeatherData();

        /* Get the present air condition and store it to data */
        HeWeather.getAirNow(context, "auto_ip", new HeWeather.OnResultAirNowBeansListener() {
            @Override
            public void onError(Throwable throwable) {
                if (data.isFailed()) return;
                /* Fill in the error message */
                data.error = throwable.getMessage();
                /* Return the data */
                callback.callback(data);
            }

            @Override
            public void onSuccess(AirNow airNow) {
                if (data.isFailed()) return;
                /* Fill in the air data message */
                data.aqi = airNow.getAir_now_city().getAqi();
                data.aqi_level = airNow.getAir_now_city().getQlty();

                if (data.isReady()) {
                    /* Return the data */
                    callback.callback(data);
                }
            }
        }); // HeWeather.getAirNow(Context context, String location, HeWeather.OnResultAirNowBeansListener listener)

        /* Get the present weather condition and store it to data */
        HeWeather.getWeatherNow(context, "auto_ip", new HeWeather.OnResultWeatherNowBeanListener() {
            @Override
            public void onError(Throwable throwable) {
                if (data.isFailed()) return;
                /* Fill in the error message */
                data.error = throwable.getMessage();
                /* Return the data */
                callback.callback(data);
            }

            @Override
            public void onSuccess(Now now) {
                if (data.isFailed()) return;
                /* Fill in the weather data message */
                data.location = now.getBasic().getLocation();
                data.timestamp = now.getUpdate().getLoc();
                data.weather = now.getNow().getCond_txt();
                data.temperature = now.getNow().getTmp();
                data.humidity = now.getNow().getHum();

                if (data.isReady()) {
                    /* Return the data */
                    callback.callback(data);
                }
            }
        }); // HeWeather.getWeatherNow(Context context, String location, HeWeather.OnResultAirNowBeansListener listener)
    }
}
