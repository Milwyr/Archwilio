package com.milton.archwilio.adapters;

/**
 * Created by Milton on 07/04/2016.
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.location.places.Place;
import com.milton.archwilio.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This adapter is used for the alert dialog that shows a list of destinations.
 */
public class DestinationsDialogListAdapter extends ArrayAdapter<Place> implements View.OnClickListener {
    private Context context;
    private int resId;
    private List<Place> places;
    private Callback callback;

    public interface Callback {
        void placeDeleted(Place place);
    }

    public DestinationsDialogListAdapter(Context context, int resId, Callback callback) {
        super(context, resId);
        this.context = context;
        this.resId = resId;
        this.places = new ArrayList<>();
        this.callback = callback;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        LayoutInflater inflater = (LayoutInflater) this.context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (view == null) {
            view = inflater.inflate(resId, parent, false);
        }

        TextView destinationsTextView = (TextView) view.findViewById(R.id.dialog_destination_text_view);
        ImageView deleteIcon = (ImageView) view.findViewById(R.id.dialog_delete_row_icon);

        destinationsTextView.setText(getItem(position).getName());
        deleteIcon.setTag(getItem(position).getId());
        deleteIcon.setOnClickListener(this);

        return view;
    }

    @Override
    public int getCount() {
        return this.places.size();
    }

    @Override
    public Place getItem(int position) {
        return this.places.get(position);
    }

    @Override
    public void onClick(View v) {
        for (Place place : this.places) {
            if (place.getId().equals(v.getTag())) {
                this.places.remove(place);
                notifyDataSetChanged();
                this.callback.placeDeleted(place);
                break;
            }
        }
    }

    @Override
    public void add(Place place) {
        this.places.add(place);
        notifyDataSetChanged();
    }

    @Override
    public void addAll(Collection<? extends Place> places) {
        this.places.addAll(places);
    }

    public List<Place> getPlaces() {
        return this.places;
    }
}