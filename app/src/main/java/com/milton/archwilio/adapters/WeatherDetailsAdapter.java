package com.milton.archwilio.adapters;

import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.milton.archwilio.R;

import java.util.List;
import java.util.Map;

/**
 * Created by Milton on 12/05/2016.
 */
public class WeatherDetailsAdapter extends RecyclerView.Adapter<WeatherDetailsAdapter.ViewHolder> {
    private List<Pair<String, String>> weatherDetailsPair;

    /**
     * Constructor
     *
     * @param weatherDetailsPair First parameter is row title, second parameter is row value
     */
    public WeatherDetailsAdapter(List<Pair<String, String>> weatherDetailsPair) {
        this.weatherDetailsPair = weatherDetailsPair;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.weather_details_row, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Pair<String, String> weatherDetails = this.weatherDetailsPair.get(position);

        holder.titleTextView.setText(weatherDetails.first);
        holder.valueTextView.setText(weatherDetails.second);
    }

    @Override
    public int getItemCount() {
        return this.weatherDetailsPair.size();
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {
        protected TextView titleTextView;
        protected TextView valueTextView;

        protected ViewHolder(View v) {
            super(v);
            titleTextView = (TextView) v.findViewById(R.id.title);
            valueTextView = (TextView) v.findViewById(R.id.value);
        }
    }
}
