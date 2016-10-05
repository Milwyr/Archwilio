package com.milton.archwilio.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.milton.archwilio.R;
import com.milton.archwilio.activities.EditAttractionActivity;
import com.milton.archwilio.activities.EditHotelActivity;
import com.milton.archwilio.activities.EditTransportActivity;
import com.milton.archwilio.activities.ItineraryActivity;
import com.milton.archwilio.common.Utility;
import com.milton.archwilio.models.AttractionRecord;
import com.milton.archwilio.models.HotelRecord;
import com.milton.archwilio.models.ItineraryItem;
import com.milton.archwilio.models.TransportRecord;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.SimpleDateFormat;
import java.util.List;

import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection;

/**
 * A section contains a date and a list of itinerary items that are scheduled on that day.
 */
public class Section extends StatelessSection implements View.OnClickListener {
    public static final String EXTRA_KEY_CURRENT_ATTRACTION_RECORD_ID = "extra_key_current_attraction_id";
    public static final String EXTRA_KEY_CURRENT_HOTEL_RECORD_ID = "extra_key_current_hotel_id";
    public static final String EXTRA_KEY_CURRENT_TRANSPORT_RECORD_ID = "extra_key_current_transport_id";

    private Context context;
    private LocalDate date;
    private List<ItineraryItem> itineraryItems;
    private String datePatternString;

    /**
     * Constructor
     *
     * @param date           The date that appears in the title
     * @param itineraryItems All the itinerary items on that day
     */
    public Section(Context context, LocalDate date, List<ItineraryItem> itineraryItems) {
        super(R.layout.itinerary_header_layout, R.layout.itinerary_item_layout);
        this.context = context;
        this.date = date;
        this.itineraryItems = itineraryItems;
        initialiseDatePatternString();
    }

    @Override
    public int getContentItemsTotal() {
        return this.itineraryItems.size();
    }

    @Override
    public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
        return new HeaderHolder(view);
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
        HeaderHolder headerHolder = (HeaderHolder) holder;
        DateTimeFormatter formatter = DateTimeFormat.forPattern(this.datePatternString)
                .withLocale(Utility.getLocale(this.context));
        headerHolder.headerTextView.setText(formatter.print(this.date));
    }

    @Override
    public RecyclerView.ViewHolder getItemViewHolder(View view) {
        return new ItemHolder(view);
    }

    @Override
    public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
        ItemHolder itemHolder = (ItemHolder) holder;
        ItineraryItem item = this.itineraryItems.get(position);

        if (item instanceof HotelRecord) {
            itemHolder.timeTextView.setVisibility(View.GONE);
        }

        SimpleDateFormat timeFormat = new SimpleDateFormat(
                Utility.TIME_FORMAT_PATTERN, Utility.getLocale(this.context));

        itemHolder.relativeLayout.setOnClickListener(this);
        itemHolder.relativeLayout.setTag(position);
        itemHolder.iconImageView.setImageResource(getIconId(item));
        itemHolder.timeTextView.setText(timeFormat.format(item.getStartDateTime()));

        if (item instanceof TransportRecord) {
            itemHolder.titleTextView.setText(((TransportRecord) item).getDisplayMessage());
        } else {
            itemHolder.titleTextView.setText(item.getPlace().getName());
        }
        itemHolder.addressTextView.setText(item.getPlace().getAddress());
    }

    @Override
    public void onClick(View v) {
        ItineraryItem item = this.itineraryItems.get((int) v.getTag());

        if (item instanceof AttractionRecord) {
            Intent intent = new Intent(this.context, EditAttractionActivity.class);
            intent.putExtra(EXTRA_KEY_CURRENT_ATTRACTION_RECORD_ID, item.getId());
            ((ItineraryActivity) this.context).startActivityForResult(
                    intent, ItineraryActivity.ADD_ATTRACTION_ACTIVITY_REQUEST_CODE);
        } else if (item instanceof HotelRecord) {
            Intent intent = new Intent(this.context, EditHotelActivity.class);
            intent.putExtra(EXTRA_KEY_CURRENT_HOTEL_RECORD_ID, item.getId());
            ((ItineraryActivity) this.context).startActivityForResult(
                    intent, ItineraryActivity.ADD_HOTEL_ACTIVITY_REQUEST_CODE);
        } else if (item instanceof TransportRecord) {
            Intent intent = new Intent(this.context, EditTransportActivity.class);
            intent.putExtra(EXTRA_KEY_CURRENT_TRANSPORT_RECORD_ID, item.getId());
            ((ItineraryActivity) this.context).startActivityForResult(
                    intent, ItineraryActivity.ADD_TRANSPORT_ACTIVITY_REQUEST_CODE);
        }
    }

    protected class HeaderHolder extends RecyclerView.ViewHolder {
        protected TextView headerTextView;

        protected HeaderHolder(View v) {
            super(v);
            headerTextView = (TextView) v.findViewById(R.id.header_text_view);
        }
    }

    protected class ItemHolder extends RecyclerView.ViewHolder {
        protected RelativeLayout relativeLayout;
        protected ImageView iconImageView;
        protected TextView timeTextView;
        protected TextView titleTextView;
        protected TextView addressTextView;

        protected ItemHolder(View v) {
            super(v);
            relativeLayout = (RelativeLayout) v.findViewById(R.id.itinerary_relative_layout);
            iconImageView = (ImageView) v.findViewById(R.id.itinerary_item_icon);
            timeTextView = (TextView) v.findViewById(R.id.itinerary_item_time_text);
            titleTextView = (TextView) v.findViewById(R.id.itinerary_item_title_text);
            addressTextView = (TextView) v.findViewById(R.id.itinerary_item_address_text);
        }
    }

    private int getIconId(ItineraryItem item) {
        if (item instanceof AttractionRecord) {
            return R.drawable.ic_location_indigo_24dp;
        } else if (item instanceof HotelRecord) {
            return R.drawable.ic_hotel_indigo_32dp;
        } else if (item instanceof TransportRecord) {
            int transportMode = ((TransportRecord) item).getTransportMode();
            int[] images = {
                    R.drawable.ic_car_indigo_32dp,
                    R.drawable.ic_coach_indigo_32dp,
                    R.drawable.ic_ferry_indigo_32dp,
                    R.drawable.ic_flight_indigo_32dp,
                    R.drawable.ic_train_indigo_32dp};

            return images[transportMode];
        }
        return R.drawable.ic_hotel_indigo_32dp;
    }

    private void initialiseDatePatternString() {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this.context);
        String selectedLanguage = sharedPreferences.getString(
                this.context.getResources().getString(R.string.pref_key_language), "en");
        if (selectedLanguage.equals("zh") || selectedLanguage.equals("ja")) {
            this.datePatternString = "MMMM dd日      EEEE";
        } else {
            this.datePatternString = "dd MMMM      EEEE";
        }
    }
}