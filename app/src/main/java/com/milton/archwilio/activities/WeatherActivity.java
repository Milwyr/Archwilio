package com.milton.archwilio.activities;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.model.LatLng;
import com.milton.archwilio.R;
import com.milton.archwilio.adapters.WeatherAdapter;
import com.milton.archwilio.adapters.WeatherDetailsAdapter;
import com.milton.archwilio.common.DbHelper;
import com.milton.archwilio.common.Utility;
import com.milton.archwilio.common.VolleySingleton;
import com.milton.archwilio.models.PlaceImpl;
import com.milton.archwilio.models.Trip;
import com.milton.archwilio.models.weather.Temperature;
import com.milton.archwilio.models.weather.Weather;
import com.milton.archwilio.models.weather.WeatherCondition;
import com.milton.archwilio.models.weather.WeatherRecord;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Reference: http://openweathermap.org/weather-data
 */
public class WeatherActivity extends BaseActivity implements View.OnClickListener, WeatherAdapter.OnItemClickListener {
    //region Instance variables
    private int mSpinnerPosition;
    private List<PlaceImpl> mDestinations;
    private WeatherAdapter mWeatherAdapter;

    private enum NumberOfDaysEnum {SEVEN, FOURTEEN}

    private RecyclerView mRecyclerView;
    private TextView mSevenDaysTextView;
    private TextView mFourteenDaysTextView;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        initialiseComponents();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.seven_days_text_view:
                fetchWeatherInformation(mSpinnerPosition, NumberOfDaysEnum.SEVEN);
                break;
            case R.id.fourteen_days_text_view:
                fetchWeatherInformation(mSpinnerPosition, NumberOfDaysEnum.FOURTEEN);
                break;
        }
    }

    @Override
    public void onItemClick(WeatherRecord weatherRecord) {
        displayWeatherDetails(weatherRecord);
    }

    private void initialiseComponents() {
        // Set up toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayShowCustomEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_close_white_24dp);
        }

        // Error checking for current trip id
        if (getIntent() == null) {
            setResult(Utility.FAIL_RESULT_CODE);
            finish();
        }
        long currentTripId = getIntent().getLongExtra(Trip.COLUMN_ID, -1);
        if (currentTripId == -1) {
            setResult(Utility.FAIL_RESULT_CODE);
            finish();
        }

        // Read a list of destination names from SQLite database
        mDestinations = DbHelper.getInstance(this).getTrip(currentTripId).getDestinationImpls();
        List<String> destinationNames = new ArrayList<>();
        for (PlaceImpl d : mDestinations) {
            destinationNames.add(d.getNameString());
        }

        // Set up the custom view that contains a spinner for action bar
        View actionBarView = getLayoutInflater().inflate(R.layout.weather_actionbar, null);
        Spinner spinner = (Spinner) actionBarView.findViewById(R.id.spinner);
        spinner.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, R.layout.spinner_item_white, destinationNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mSpinnerPosition = position;
                fetchWeatherInformation(position, NumberOfDaysEnum.SEVEN);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Display the custom spinner view on action bar
        getSupportActionBar().setCustomView(actionBarView);

        // Set up the recycler view
        mRecyclerView = (RecyclerView) findViewById(R.id.weather_forecast_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Display the weather information for the default choice
        mSpinnerPosition = 0;
        mWeatherAdapter = new WeatherAdapter(this);
        fetchWeatherInformation(0, NumberOfDaysEnum.SEVEN);

        // Add listener for the number of days text views
        mSevenDaysTextView = (TextView) findViewById(R.id.seven_days_text_view);
        mSevenDaysTextView.setOnClickListener(this);
        mFourteenDaysTextView = (TextView) findViewById(R.id.fourteen_days_text_view);
        mFourteenDaysTextView.setOnClickListener(this);
    }

    private void fetchWeatherInformation(final int position, final NumberOfDaysEnum numberOfDaysEnum) {
        // Create a query on coordinates where the latitude and longitude are up to ten digits
        LatLng latLng = mDestinations.get(position).getLatLng();
        String coordinatesQuery =
                "lat=" + String.format(Locale.ENGLISH, "%.10f", latLng.latitude) +
                        "&lon=" + String.format(Locale.ENGLISH, "%.10f", latLng.longitude);

        final int numberOfDays = (numberOfDaysEnum == NumberOfDaysEnum.SEVEN) ? 7 : 14;

        // Fetch weather information from OpenWeatherMap for the next 7 or 14 days
        String url = "http://api.openweathermap.org/data/2.5/forecast/daily?" + coordinatesQuery +
                "&cnt=" + numberOfDays + "&units=metric&APPID=3e24436be23889d4a9c5698e8286ed21";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Pass the weather records to weather adapter
                            List<WeatherRecord> weatherRecords = parseWeatherJson(response);
                            mWeatherAdapter.setWeatherRecords(weatherRecords);
                            mRecyclerView.setAdapter(mWeatherAdapter);

                            // Highlight the text view that says 7 days
                            if (numberOfDaysEnum == NumberOfDaysEnum.SEVEN) {
                                mSevenDaysTextView.setTextColor(
                                        ContextCompat.getColor(WeatherActivity.this, R.color.colorPrimary));
                                mFourteenDaysTextView.setTextColor(
                                        ContextCompat.getColor(WeatherActivity.this, R.color.grey500));
                            }
                            // Highlight the textview that says 14 days
                            else if (numberOfDaysEnum == NumberOfDaysEnum.FOURTEEN) {
                                mFourteenDaysTextView.setTextColor(
                                        ContextCompat.getColor(WeatherActivity.this, R.color.colorPrimary));
                                mSevenDaysTextView.setTextColor(
                                        ContextCompat.getColor(WeatherActivity.this, R.color.grey500));
                            }

                            displayWeatherDetails(weatherRecords.get(position));
                        } catch (JSONException e) {
                            Snackbar.make(findViewById(R.id.coordinator_layout),
                                    R.string.error_general, Snackbar.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error instanceof NoConnectionError) {
                            Snackbar.make(findViewById(R.id.coordinator_layout),
                                    R.string.error_no_internet_connection, Snackbar.LENGTH_LONG).show();
                        } else {
                            Snackbar.make(findViewById(R.id.coordinator_layout),
                                    R.string.error_general, Snackbar.LENGTH_LONG).show();
                        }

                    }
                });
        RequestQueue requestQueue = VolleySingleton.getInstance(this).getRequestQueue();
        requestQueue.add(jsonObjectRequest);
    }

    // This method parses the JSON object fetched from OpenWeatherMap and returns a list of weather records.
    private List<WeatherRecord> parseWeatherJson(JSONObject response) throws JSONException {
        List<WeatherRecord> weatherRecords = new ArrayList<>();
        JSONArray jsonWeathers = response.getJSONArray("list");
        for (int i = 0; i < jsonWeathers.length(); i++) {
            JSONObject jsonWeather = (JSONObject) jsonWeathers.get(i);

            // Convert from Unix UTC timestamp to Java Date object
            long timeMillis = jsonWeather.getLong("dt");
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timeMillis * 1000);

            JSONObject jsonTemperature = jsonWeather.getJSONObject("temp");
            Temperature temperature = new Temperature(
                    jsonTemperature.getDouble("day"),
                    jsonTemperature.getDouble("min"),
                    jsonTemperature.getDouble("max"),
                    jsonTemperature.getDouble("night"),
                    jsonTemperature.getDouble("eve"),
                    jsonTemperature.getDouble("morn")
            );

            JSONObject weatherConditionJsonObject =
                    ((JSONObject) jsonWeather.getJSONArray("weather").get(0));
            WeatherCondition weatherCondition = new WeatherCondition(
                    weatherConditionJsonObject.getInt("id"),
                    weatherConditionJsonObject.getString("main"),
                    weatherConditionJsonObject.getString("description")
            );

            double pressure = jsonWeather.getDouble("pressure");
            int humidity = jsonWeather.getInt("humidity");
            double windSpeed = jsonWeather.getDouble("speed");
            int windDirection = jsonWeather.getInt("deg");
            int cloudiness = jsonWeather.getInt("clouds");

            Weather weather = new Weather(temperature, weatherCondition,
                    pressure, humidity, windSpeed, windDirection, cloudiness);
            weatherRecords.add(new WeatherRecord(calendar.getTime(), weather));
        }
        return weatherRecords;
    }

    private void displayWeatherDetails(WeatherRecord weatherRecord) {
        Weather weather = weatherRecord.getWeather();

        // Add weather details to the list
        List<Pair<String, String>> weatherDetailsPair = new ArrayList<>();
        weatherDetailsPair.add(new Pair<>(getResources().getString(R.string.humidity), weather.getHumidity() + "%"));
        weatherDetailsPair.add(new Pair<>(getResources().getString(R.string.cloudiness), weather.getCloudiness() + "%"));
        weatherDetailsPair.add(new Pair<>(getResources().getString(R.string.wind_direction), weather.getWindDirection() + "°"));
        weatherDetailsPair.add(new Pair<>(getResources().getString(R.string.wind_speed), weather.getWindSpeed() + " m/s"));
        weatherDetailsPair.add(new Pair<>(getResources().getString(R.string.pressure), Math.round(weather.getPressure()) + " hPa"));

        // Set up the recycler view
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.weather_details_recycler_view);
        recyclerView.setAdapter(new WeatherDetailsAdapter(weatherDetailsPair));
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Display the corresponding icon
        ImageView weatherDetailsIcon = (ImageView) findViewById(R.id.weather_details_icon);
        weatherDetailsIcon.setImageResource(getIconId(weather.getWeatherCondition().getId()));

        // Display the views
        findViewById(R.id.weather_details_title_text_view).setVisibility(View.VISIBLE);
        weatherDetailsIcon.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    // Reference: http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
    private int getIconId(int conditionCode) {
        if (conditionCode >= 200 && conditionCode <= 232) {
            return R.drawable.ic_thunderstorm_72dp;
        } else if (conditionCode >= 300 && conditionCode <= 321) {
            return R.drawable.ic_fog_72dp;
        } else if (conditionCode >= 500 && conditionCode <= 531) {
            return R.drawable.ic_rain_72dp;
        } else if (conditionCode == 600 || conditionCode == 601) {
            return R.drawable.ic_light_snow_72dp;
        } else if (conditionCode >= 602 && conditionCode <= 622) {
            return R.drawable.ic_heavy_snow_72dp;
        } else if (conditionCode == 781 || conditionCode == 900 || conditionCode == 901) {
            return R.drawable.ic_tornado_72dp;
        } else if (conditionCode == 904) {
            return R.drawable.ic_hot_72dp;
        }
        return R.drawable.ic_clear_sky_72dp;
    }
}