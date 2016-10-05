package com.milton.archwilio.activities;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.milton.archwilio.R;
import com.milton.archwilio.adapters.Section;
import com.milton.archwilio.common.DbHelper;
import com.milton.archwilio.common.Utility;
import com.milton.archwilio.models.AttractionRecord;
import com.milton.archwilio.models.HotelRecord;
import com.milton.archwilio.models.ItineraryItem;
import com.milton.archwilio.models.TransportRecord;
import com.milton.archwilio.models.Trip;

import org.joda.time.Days;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

@TargetApi(Build.VERSION_CODES.M)
public class ItineraryActivity extends BaseActivity implements
        View.OnClickListener, View.OnScrollChangeListener, SwipeRefreshLayout.OnRefreshListener {

    // This code applies for add attraction, hotel and transport
    public static final int ADD_ATTRACTION_ACTIVITY_REQUEST_CODE = 9000;
    public static final int ADD_HOTEL_ACTIVITY_REQUEST_CODE = 9001;
    public static final int ADD_TRANSPORT_ACTIVITY_REQUEST_CODE = 9002;

    private long mTripId;
    private SectionedRecyclerViewAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private FloatingActionMenu mFam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_itinerary);

        initialiseComponents();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        DbHelper dbHelper = DbHelper.getInstance(this);
        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);

        if (resultCode == Utility.SUCCESS_RESULT_CODE) {
            if (requestCode == ADD_HOTEL_ACTIVITY_REQUEST_CODE) {
                // Update the list view to show the newly added hotel
                long lastHotelRecordId = dbHelper.getMaximum(
                        HotelRecord.TABLE_NAME, HotelRecord.COLUMN_ID);
                HotelRecord hotelRecord = dbHelper.getHotelRecord(this.mTripId, lastHotelRecordId);

                if (hotelRecord != null) {
                    // TODO: Increase efficiency by not refreshing the all the sections
//                    mAdapter.add(hotelRecord);
                    mAdapter.removeAllSections();
                    initialiseAdapter();
                }
            } else if (requestCode == ADD_TRANSPORT_ACTIVITY_REQUEST_CODE) {
                // Update the list view to show the newly added transport
                long lastTransportRecordId = dbHelper.getMaximum(
                        TransportRecord.TABLE_NAME, TransportRecord.COLUMN_ID);
                TransportRecord transportRecord = dbHelper
                        .getTransportRecord(mTripId, lastTransportRecordId);

                if (transportRecord != null) {
                    // TODO: Increase efficiency by not refreshing the all the sections
//                    mAdapter.add(transportRecord);
                    mAdapter.removeAllSections();
                    initialiseAdapter();
                }
            } else if (requestCode == ADD_ATTRACTION_ACTIVITY_REQUEST_CODE) {
                long lastAttractionRecordId = dbHelper.getMaximum(
                        AttractionRecord.TABLE_NAME, AttractionRecord.COLUMN_ID);
                AttractionRecord attractionRecord = dbHelper
                        .getAttractionRecord(mTripId, lastAttractionRecordId);

                if (attractionRecord != null) {
                    // TODO: Increase efficiency by not refreshing the all the sections
//                    mAdapter.add(attractionRecord);
                    mAdapter.removeAllSections();
                    initialiseAdapter();
                }
            }
            mAdapter.notifyDataSetChanged();
            Snackbar.make(coordinatorLayout, R.string.saved_successfully, Snackbar.LENGTH_LONG).show();
        }

        // Close the floating action menu button
        FloatingActionMenu fam = (FloatingActionMenu) findViewById(R.id.floating_action_menu);
        fam.close(true);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.header_text_view:
                Intent intent = new Intent(this, WeatherActivity.class);
                intent.putExtra(Trip.COLUMN_ID, mTripId);
                startActivity(intent);
                break;
            case R.id.add_hotel_fab:
                intent = new Intent(this, AddHotelActivity.class);
                intent.putExtra(Trip.COLUMN_ID, mTripId);
                startActivityForResult(intent, ADD_HOTEL_ACTIVITY_REQUEST_CODE);
                break;
            case R.id.add_attraction_fab:
                intent = new Intent(this, AddAttractionActivity.class);
                intent.putExtra(Trip.COLUMN_ID, mTripId);
                intent.putExtra("Mode", "Add");
                startActivityForResult(intent, ADD_ATTRACTION_ACTIVITY_REQUEST_CODE);
                break;
            case R.id.add_transport_fab:
                intent = new Intent(this, AddTransportActivity.class);
                intent.putExtra(Trip.COLUMN_ID, mTripId);
                startActivityForResult(intent, ADD_TRANSPORT_ACTIVITY_REQUEST_CODE);
                break;
        }
    }

    @Override
    public void onRefresh() {
        mAdapter.removeAllSections();
        initialiseAdapter();
        mAdapter.notifyDataSetChanged();
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        // Swipe down
        if (scrollY - oldScrollY > 10) {
            mFam.hideMenuButton(true);
        }
        // Swipe up
        else if (scrollY < oldScrollY) {
            mFam.showMenu(true);
        }
    }

    private void initialiseComponents() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mFam = (FloatingActionMenu) findViewById(R.id.floating_action_menu);

        FloatingActionButton addHotelFab = (FloatingActionButton) findViewById(R.id.add_hotel_fab);
        addHotelFab.setOnClickListener(this);

        FloatingActionButton addAttractionFab = (FloatingActionButton) findViewById(R.id.add_attraction_fab);
        addAttractionFab.setOnClickListener(this);

        FloatingActionButton addTransportFab = (FloatingActionButton) findViewById(R.id.add_transport_fab);
        addTransportFab.setOnClickListener(this);

        TextView headerTextView = (TextView) findViewById(R.id.header_text_view);
        headerTextView.setOnClickListener(this);

        mTripId = mSharedPreferences.getLong(Utility.PREF_KEY_CURRENT_TRIP_ID, -1);
        mAdapter = new SectionedRecyclerViewAdapter();
        initialiseAdapter();

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.itinerary_recycler_view);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            recyclerView.setOnScrollChangeListener(this);
        } else {
            // Support scroll change for devices with Android version older than Marshmallow
            recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    // Swipe down
                    if (dy > 5) {
                        mFam.hideMenuButton(true);
                    }
                    // Swipe up
                    else if (dy < -5) {
                        mFam.showMenuButton(true);
                    }
                }
            });
        }

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
    }

    private List<ItineraryItem> getAllItineraryItems() {
        List<ItineraryItem> itineraryItems = new ArrayList<>();
        DbHelper dbHelper = DbHelper.getInstance(this);

        List<AttractionRecord> attractionRecords = dbHelper.getAttractionRecords(mTripId);
        for (AttractionRecord record : attractionRecords) {
            itineraryItems.add(record);
        }

        // Retrieve all hotel records and save them to itineraryItems
        List<HotelRecord> records = dbHelper.getHotelRecords(mTripId);
        for (HotelRecord record : records) {
            itineraryItems.add(record);
        }

        // Retrieve all transport records and save them to itineraryItems
        List<TransportRecord> transportRecords = dbHelper.getTransportRecords(mTripId);
        for (TransportRecord record : transportRecords) {
            itineraryItems.add(record);
        }

        // Sort the itinerary items by start date ascendingly
        Collections.sort(itineraryItems, ItineraryItem.DateComparator);

        return itineraryItems;
    }

    private void initialiseAdapter() {
        List<ItineraryItem> itineraryItems = getAllItineraryItems();

        Trip trip = DbHelper.getInstance(this).getTrip(mTripId);

        LocalDate startDate = new LocalDate(trip.getStartDate());
        LocalDate endDate = new LocalDate(trip.getEndDate());

        // Number of days between start and end date of the trip
        int numberOfDays = Days.daysBetween(startDate, endDate).getDays();

        // Add sections with the date and all the itinerary items happened on that day
        LocalDate date = new LocalDate(startDate);
        for (int i = 0; i < numberOfDays + 1; i++) {
            List<ItineraryItem> itemsOnThatDay = new ArrayList<>();

            for (ItineraryItem item : itineraryItems) {
                if (Days.daysBetween(new LocalDate(item.getStartDateTime()), date).getDays() == 0) {
                    itemsOnThatDay.add(item);
                } else {
                    // Break the loop when the itinerary item is not on that day,
                    // as the items are sorted and the subsequent items must not be
                    // earlier than the current item.
                    break;
                }
            }

            mAdapter.addSection(new Section(this, date, itemsOnThatDay));
            date = date.plusDays(1);

            // Remove the itinerary items that have already been added
            itineraryItems.removeAll(itemsOnThatDay);
        }
    }
}