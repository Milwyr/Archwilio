package com.milton.archwilio.adapters;

import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.milton.archwilio.R;
import com.milton.archwilio.models.weather.WeatherRecord;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Created by Milton on 04/05/2016.
 */
public class WeatherAdapter extends RecyclerView.Adapter<WeatherAdapter.ViewHolder> implements View.OnClickListener {
    private int selectedPosition = 0;
    private List<WeatherRecord> weatherRecords;
    private OnItemClickListener onItemClickListener;

    /**
     * This interface definition for a callback is invoked when an item in this WeatherAdapter has been clicked.
     */
    public interface OnItemClickListener {
        /**
         * This callback method is invoked when an item in this WeatherAdapter has been clicked.
         *
         * @param weatherRecord The weather record of the row that was clicked
         */
        void onItemClick(WeatherRecord weatherRecord);
    }

    /**
     * Constructor
     *
     * @param onItemClickListener An OnItemClickListener
     */
    public WeatherAdapter(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public WeatherAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.weather_recycler_view_row, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final WeatherAdapter.ViewHolder holder, int position) {
        WeatherRecord weatherRecord = this.weatherRecords.get(position);

        if (this.selectedPosition == position) {
            holder.rootLayout.setBackgroundColor(Color.CYAN);
        } else {
            holder.rootLayout.setBackgroundColor(Color.WHITE);
        }
        holder.rootLayout.setTag(position);
        holder.rootLayout.setOnClickListener(this);

        SimpleDateFormat sdf = new SimpleDateFormat("dd  MMMM", Locale.ENGLISH);
        holder.dateTextView.setText(sdf.format(weatherRecord.getDate()));

        holder.weatherIcon.setImageResource(getIconId(weatherRecord.getWeather().getWeatherCondition().getId()));

        String minTemperatureMessage = String.valueOf(
                Math.round(weatherRecord.getWeather().getTemperature().getMin())) + "°C";
        holder.minTemperatureTextView.setText(minTemperatureMessage);

        String maxTemperatureMessage = String.valueOf(
                Math.round(weatherRecord.getWeather().getTemperature().getMax())) + "°C";
        holder.maxTemperatureTextView.setText(maxTemperatureMessage);
    }

    @Override
    public int getItemCount() {
        return this.weatherRecords.size();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.root_layout) {
            int position = (int) v.getTag();

            notifyItemChanged(this.selectedPosition);
            notifyItemChanged(position);
            this.selectedPosition = position;

            WeatherRecord selectedWeatherRecord = this.weatherRecords.get(this.selectedPosition);
            this.onItemClickListener.onItemClick(selectedWeatherRecord);
        }
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {
        protected LinearLayout rootLayout;
        protected TextView dateTextView;
        protected ImageView weatherIcon;
        protected TextView minTemperatureTextView;
        protected TextView maxTemperatureTextView;

        protected ViewHolder(View v) {
            super(v);
            rootLayout = (LinearLayout) v.findViewById(R.id.root_layout);
            dateTextView = (TextView) v.findViewById(R.id.date_text_view);
            weatherIcon = (ImageView) v.findViewById(R.id.weather_icon);
            minTemperatureTextView = (TextView) v.findViewById(R.id.min_temperature_text_view);
            maxTemperatureTextView = (TextView) v.findViewById(R.id.max_temperature_text_view);
        }
    }

    /**
     * This method sets the weather records within the adapter, and calls notifyDataSetChanged method.
     *
     * @param weatherRecords A list of weather records
     */
    public void setWeatherRecords(List<WeatherRecord> weatherRecords) {
        this.weatherRecords = weatherRecords;
        notifyDataSetChanged();
    }

    // Reference: http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
    private int getIconId(int conditionCode) {
        if (conditionCode >= 200 && conditionCode <= 232) {
            return R.drawable.ic_thunderstorm_24dp;
        } else if (conditionCode >= 300 && conditionCode <= 321) {
            return R.drawable.ic_fog_24dp;
        } else if (conditionCode >= 500 && conditionCode <= 531) {
            return R.drawable.ic_rain_24dp;
        } else if (conditionCode == 600 || conditionCode == 601) {
            return R.drawable.ic_light_snow_24dp;
        } else if (conditionCode >= 602 && conditionCode <= 622) {
            return R.drawable.ic_heavy_snow_24dp;
        } else if (conditionCode == 781 || conditionCode == 900 || conditionCode == 901) {
            return R.drawable.ic_tornado_24dp;
        } else if (conditionCode == 904) {
            return R.drawable.ic_hot_24dp;
        }
        return R.drawable.ic_clear_sky;
    }
}