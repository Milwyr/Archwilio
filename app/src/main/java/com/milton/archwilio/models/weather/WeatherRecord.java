package com.milton.archwilio.models.weather;

import java.util.Date;

/**
 * Created by Milton on 04/05/2016.
 */
public class WeatherRecord {
    private Date date;
    private Weather weather;

    public WeatherRecord(Date date, Weather weather) {
        this.date = date;
        this.weather = weather;
    }

    public Date getDate() {
        return this.date;
    }

    public Weather getWeather() {
        return this.weather;
    }
}
