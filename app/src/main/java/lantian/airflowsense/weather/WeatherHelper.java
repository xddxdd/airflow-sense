package lantian.airflowsense.weather;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WeatherHelper {
    public static void fetchWeatherAsync(final WeatherCallback callback) {
        (new Thread(new Runnable() {
            @Override
            public void run() {
                fetchWeather(callback);
            }
        })).start();
    }

    public static void fetchWeather(WeatherCallback callback) {
        WeatherData data = new WeatherData();
        try {
            OkHttpClient client = new OkHttpClient();
            Request generalDataRequest = new Request.Builder()
                    .url("https://free-api.heweather.net/s6/weather/now?location=auto_ip&key=db86a5196f304e52a4369818c5182e60")
                    .build();
            Response generalDataResponse = client.newCall(generalDataRequest).execute();

            Request airQualityDataRequest = new Request.Builder()
                    .url("https://free-api.heweather.net/s6/air/now?location=auto_ip&key=db86a5196f304e52a4369818c5182e60")
                    .build();
            Response airQualityDataResponse = client.newCall(airQualityDataRequest).execute();

            JSONObject generalDataJson = new JSONObject(generalDataResponse.body().string()).getJSONArray("HeWeather6").getJSONObject(0);
            JSONObject airQualityDataJson = new JSONObject(airQualityDataResponse.body().string()).getJSONArray("HeWeather6").getJSONObject(0);

            data.location = generalDataJson.getJSONObject("basic").getString("location");
            data.timestamp = generalDataJson.getJSONObject("update").getString("loc");
            data.weather = generalDataJson.getJSONObject("now").getString("cond_txt");
            data.temperature = generalDataJson.getJSONObject("now").getString("tmp");
            data.humidity = generalDataJson.getJSONObject("now").getString("hum");
            data.aqi = airQualityDataJson.getJSONObject("air_now_city").getString("aqi");
            data.aqi_level = airQualityDataJson.getJSONObject("air_now_city").getString("qlty");
            data.success = true;

        } catch (IOException | JSONException | NullPointerException e) {
            e.printStackTrace();

            data.success = false;
        }
        callback.callback(data);
    }
}
