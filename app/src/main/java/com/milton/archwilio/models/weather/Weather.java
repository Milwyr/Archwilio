package com.milton.archwilio.models.weather;

/**
 * Created by Milton on 03/05/2016.
 */
public class Weather {
    private Temperature temperature;
    private WeatherCondition weatherCondition;
    private double pressure;
    private int humidity;
    private double windSpeed;
    private int windDirection;
    private int cloudiness;

    public Weather(Temperature temperature, WeatherCondition weatherCondition, double pressure,
                   int humidity, double windSpeed, int windDirection, int cloudiness) {
        this.temperature = temperature;
        this.weatherCondition = weatherCondition;
        this.pressure = pressure;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
        this.windDirection = windDirection;
        this.cloudiness = cloudiness;
    }

    public Temperature getTemperature() {
        return temperature;
    }

    public WeatherCondition getWeatherCondition() {
        return weatherCondition;
    }

    public double getPressure() {
        return pressure;
    }

    public int getHumidity() {
        return humidity;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public int getWindDirection() {
        return windDirection;
    }

    public int getCloudiness() {
        return cloudiness;
    }
}