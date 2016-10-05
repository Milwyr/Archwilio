package com.milton.archwilio.models.weather;

/**
 * Created by Milton on 05/05/2016.
 */
public class WeatherCondition {
    private int id;
    private String main;
    private String description;

    public WeatherCondition(int id, String main, String description) {
        this.id = id;
        this.main = main;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public String getMain() {
        return main;
    }

    public String getDescription() {
        return description;
    }
}
