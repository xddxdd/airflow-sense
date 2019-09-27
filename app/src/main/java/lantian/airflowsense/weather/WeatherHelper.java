package lantian.airflowsense.weather;

import android.content.Context;

import interfaces.heweather.com.interfacesmodule.bean.air.now.AirNow;
import interfaces.heweather.com.interfacesmodule.bean.weather.now.Now;
import interfaces.heweather.com.interfacesmodule.view.HeConfig;
import interfaces.heweather.com.interfacesmodule.view.HeWeather;

public class WeatherHelper {
    public WeatherHelper() {
        HeConfig.init("HE1909271417191305", "43fb431a8ab046b0b9f547df14f4c6e9");
        HeConfig.switchToFreeServerNode();
    }

    public void fetchWeatherAsync(final Context context, final WeatherCallback callback) {
        final WeatherData data = new WeatherData();

        HeWeather.getAirNow(context, "auto_ip", new HeWeather.OnResultAirNowBeansListener() {
            @Override
            public void onError(Throwable throwable) {
                if (data.isFailed()) return;

                data.error = throwable.getMessage();

                callback.callback(data);
            }

            @Override
            public void onSuccess(AirNow airNow) {
                if (data.isFailed()) return;

                data.aqi = airNow.getAir_now_city().getAqi();
                data.aqi_level = airNow.getAir_now_city().getQlty();

                if (data.isReady()) {
                    callback.callback(data);
                }
            }
        });

        HeWeather.getWeatherNow(context, "auto_ip", new HeWeather.OnResultWeatherNowBeanListener() {
            @Override
            public void onError(Throwable throwable) {
                if (data.isFailed()) return;

                data.error = throwable.getMessage();

                callback.callback(data);
            }

            @Override
            public void onSuccess(Now now) {

                if (data.isFailed()) return;

                data.location = now.getBasic().getLocation();
                data.timestamp = now.getUpdate().getLoc();
                data.weather = now.getNow().getCond_txt();
                data.temperature = now.getNow().getTmp();
                data.humidity = now.getNow().getHum();

                if (data.isReady()) {
                    callback.callback(data);
                }
            }
        });
    }
}
