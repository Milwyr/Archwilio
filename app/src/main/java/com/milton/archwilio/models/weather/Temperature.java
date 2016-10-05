package com.milton.archwilio.models.weather;

/**
 * This class stores temperature at different time in a day in celsius.
 */
public class Temperature {
    private double day;
    private double min;
    private double max;
    private double night;
    private double evening;
    private double morning;

    public Temperature(double day, double min, double max, double night, double evening, double morning) {
        this.day = day;
        this.min = min;
        this.max = max;
        this.night = night;
        this.evening = evening;
        this.morning = morning;
    }

    /**
     * @return Day temperature
     */
    public double getDay() {
        return day;
    }

    /**
     * @return Minimum daily temperature
     */
    public double getMin() {
        return min;
    }

    /**
     * @return Maximum daily temperature
     */
    public double getMax() {
        return max;
    }

    /**
     * @return Night temperature
     */
    public double getNight() {
        return night;
    }

    /**
     * @return Evening temperature
     */
    public double getEvening() {
        return evening;
    }

    /**
     * @return Morning temperature
     */
    public double getMorning() {
        return morning;
    }
}