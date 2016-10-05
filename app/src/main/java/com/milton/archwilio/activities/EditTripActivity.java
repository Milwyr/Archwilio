package com.milton.archwilio.activities;

import android.os.Bundle;
import android.view.View;

import com.milton.archwilio.R;
import com.milton.archwilio.common.DbHelper;
import com.milton.archwilio.common.Utility;
import com.milton.archwilio.models.Trip;

/**
 * Created by Milton on 07/04/2016.
 */
public class EditTripActivity extends AddTripActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            if (getIntent() != null) {
                long currentTripId = getIntent().getLongExtra(Utility.PREF_KEY_CURRENT_TRIP_ID, -1);

                if (currentTripId != -1) {
                    (findViewById(R.id.fab)).setOnClickListener(this);
                    super.setEnabledStatus(false);

                    Trip trip = DbHelper.getInstance(this).getTrip(currentTripId);
                    super.populateTripData(trip);
                } else {
                    super.showErrorDialog(R.string.error_current_trip_not_found, true);
                }
            } else {
                super.showErrorDialog(R.string.error_current_trip_not_found, true);
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
                setEnabledStatus(true);
                break;
            case R.id.add_trip_add_destination_image_view:
                super.onClick(view);
                break;
            case R.id.add_trip_destinations_edit_text:
                super.onClick(view);
                break;
            case R.id.add_trip_start_date_value:
                super.onClick(view);
                break;
            case R.id.add_trip_end_date_value:
                super.onClick(view);
                break;
            case R.id.add_trip_save_button:
                saveTrip(SaveOption.EDIT);
                break;
        }
    }
}