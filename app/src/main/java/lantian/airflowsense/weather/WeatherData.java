package lantian.airflowsense.weather;

public class WeatherData {
    public String location = "";
    public String timestamp = "";
    public String weather = "";
    public String temperature = "";
    public String humidity = "";
    public String aqi = "";
    public String aqi_level = "";
    public String error = "";

    public boolean isFailed() {
        return !error.isEmpty();
    }

    public boolean isReady() {
        if (location.isEmpty()) return false;
        if (timestamp.isEmpty()) return false;
        if (weather.isEmpty()) return false;
        if (temperature.isEmpty()) return false;
        if (humidity.isEmpty()) return false;
        if (aqi.isEmpty()) return false;
        return !aqi_level.isEmpty();
    }
}
