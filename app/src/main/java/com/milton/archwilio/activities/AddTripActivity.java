package com.milton.archwilio.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.milton.archwilio.R;
import com.milton.archwilio.adapters.DestinationsDialogListAdapter;
import com.milton.archwilio.common.DbHelper;
import com.milton.archwilio.common.Utility;
import com.milton.archwilio.fragments.DatePickerDialogFragment;
import com.milton.archwilio.models.Trip;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddTripActivity extends BaseActivity
        implements View.OnClickListener, DatePickerDialogFragment.OnDateSelectedListener {
    private final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 8001;
    private final String START_DATE = "start";
    private final String END_DATE = "end";

    //region Instance variables
    private DatePickerDialogFragment mDatePickerDialogFragment;
    private ImageView mAddDestinationImageView;
    private TextView mDestinationsEditText;
    private EditText mTitleEditText;
    protected TextView mStartDateTextView;
    protected TextView mEndDateTextView;
    private EditText mNoteEditText;
    private Button mSaveButton;

    protected SimpleDateFormat mSimpleDateFormat;
    protected Trip mTrip;

    private AlertDialog mDestinationsDialog;
    protected DestinationsDialogListAdapter mDestinationListAdapter;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_trip);

        initialiseComponents();

        // Retrieve data before onSaveInstanceState() method was called
        if (savedInstanceState != null) {
            Trip trip = savedInstanceState.getParcelable(Trip.TABLE_NAME);
            if (trip != null) {
                mTrip = trip;
                populateTripData(trip);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Place place = PlaceAutocomplete.getPlace(this, data);

        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (place != null) {
                mDestinationListAdapter.add(place);
                mTrip.addDestination(place);

                updateDestinationsTextView(mDestinationListAdapter.getPlaces());

                // Set the default title if it has not been set before
                if (mTitleEditText.getText().length() == 0) {
                    String defaultTitle = getResources().getString(
                            R.string.title_trip_default_prefix) + " " + place.getName();
                    mTitleEditText.setText(defaultTitle);
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(Trip.TABLE_NAME, mTrip);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.add_trip_add_destination_image_view:
                try {
                    Intent intent =
                            new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                                    .build(this);
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);

                } catch (GooglePlayServicesRepairableException |
                        GooglePlayServicesNotAvailableException e) {
                    super.showErrorDialog(R.string.error_google_play_services_not_available, false);
                }
                break;
            case R.id.add_trip_destinations_edit_text:
                mDestinationsDialog.show();
                break;
            case R.id.add_trip_start_date_value:
                try {
                    Calendar calendar = Calendar.getInstance();
                    Date date = mSimpleDateFormat.parse(mStartDateTextView.getText().toString());
                    calendar.setTime(date);
                    mDatePickerDialogFragment = DatePickerDialogFragment.newInstance(calendar);
                } catch (ParseException e) {
                    mDatePickerDialogFragment = new DatePickerDialogFragment();
                    e.printStackTrace();
                }

                mDatePickerDialogFragment.show(getFragmentManager(), START_DATE);
                break;
            case R.id.add_trip_end_date_value:
                try {
                    Calendar calendar = Calendar.getInstance();
                    Date date = mSimpleDateFormat.parse(mEndDateTextView.getText().toString());
                    calendar.setTime(date);
                    mDatePickerDialogFragment = DatePickerDialogFragment.newInstance(calendar);
                } catch (ParseException e) {
                    mDatePickerDialogFragment = new DatePickerDialogFragment();
                    e.printStackTrace();
                }

                mDatePickerDialogFragment.show(getFragmentManager(), END_DATE);
                break;
            case R.id.add_trip_save_button:
                saveTrip(SaveOption.ADD);
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Show an alert dialog to let user confirm whether to discard the changes
            new AlertDialog.Builder(this)
                    .setMessage(R.string.dialog_close_confirmation)
                    .setPositiveButton(R.string.dialog_keep_editing, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton(R.string.dialog_discard, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }).create().show();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDateSelected(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);

        String tag = mDatePickerDialogFragment.getTag();
        if (tag.equals(START_DATE)) {
            Date newStartDate = calendar.getTime();
            Date endDate = mTrip.getEndDate();

            mStartDateTextView.setText(mSimpleDateFormat.format(newStartDate));
            mTrip.setStartDate(newStartDate);

            if (newStartDate.after(endDate)) {
                // Set end date to be one day after start date
                calendar.add(Calendar.DATE, 1);
                mEndDateTextView.setText(mSimpleDateFormat.format(calendar.getTime()));
                mTrip.setEndDate(calendar.getTime());
            }
        } else if (tag.equals(END_DATE)) {
            Date startDate = mTrip.getStartDate();
            Date endDate = calendar.getTime();

            mEndDateTextView.setText(mSimpleDateFormat.format(endDate));
            mTrip.setEndDate(endDate);

            if (endDate.before(startDate)) {
                // Set start date to be one day before end date
                calendar.add(Calendar.DATE, -1);
                mStartDateTextView.setText(mSimpleDateFormat.format(calendar.getTime()));
                mTrip.setStartDate(calendar.getTime());
            }
        }
    }

    // This method is only used in the child activity, but not in this activity
    protected void setEnabledStatus(boolean isEnabled) {
        mAddDestinationImageView.setEnabled(isEnabled);
        mDestinationsEditText.setEnabled(isEnabled);
        mTitleEditText.setEnabled(isEnabled);
        mStartDateTextView.setEnabled(isEnabled);
        mEndDateTextView.setEnabled(isEnabled);
        mNoteEditText.setEnabled(isEnabled);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (isEnabled) {
            mSaveButton.setVisibility(View.VISIBLE);
            fab.setVisibility(View.GONE);
        } else {
            mSaveButton.setVisibility(View.GONE);
            fab.setVisibility(View.VISIBLE);
        }
    }

    protected void populateTripData(Trip trip) {
        if (trip == null) {
            super.showErrorDialog(R.string.error_current_trip_not_found, false);
        } else {
            mTrip = trip;

            mTitleEditText.setText(trip.getTitle());
            mStartDateTextView.setText(mSimpleDateFormat.format(trip.getStartDate()));
            mEndDateTextView.setText(mSimpleDateFormat.format(trip.getEndDate()));
            mNoteEditText.setText(trip.getNote());

            mDestinationListAdapter.addAll(trip.getDestinations());
            updateDestinationsTextView(trip.getDestinations());
        }

    }

    protected void saveTrip(SaveOption saveOption) {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        if (mTrip.getDestinations().size() == 0) {
            Snackbar.make(fab, R.string.error_no_destination_found, Snackbar.LENGTH_SHORT).show();
        } else {
            try {
                // Read field data from views
                mTrip.setTitle(mTitleEditText.getText().toString());
                mTrip.setStartDate(mSimpleDateFormat.parse(mStartDateTextView.getText().toString()));
                mTrip.setEndDate(mSimpleDateFormat.parse(mEndDateTextView.getText().toString()));
                mTrip.setNote(mNoteEditText.getText().toString());

                if (saveOption == SaveOption.ADD) {
                    mTrip.setIsDeleted(false);
                }

                boolean isSuccess;
                if (saveOption == SaveOption.ADD) {
                    isSuccess = DbHelper.getInstance(this).insertTrip(mTrip);
                } else {
                    isSuccess = DbHelper.getInstance(this).updateTrip(mTrip);
                }

                if (isSuccess) {
                    Intent intent = getIntent().putExtra(Trip.TABLE_NAME, mTrip);
                    setResult(Utility.SUCCESS_RESULT_CODE, intent);
                } else {
                    setResult(Utility.FAIL_RESULT_CODE);
                }
                finish();
            } catch (ParseException e) {
                Snackbar.make(fab, R.string.error_wrong_start_or_end_date, Snackbar.LENGTH_LONG).show();
            }
        }
    }

    private void initialiseComponents() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_close_white_24dp);
        }

        mAddDestinationImageView = (ImageView) findViewById(R.id.add_trip_add_destination_image_view);
        if (mAddDestinationImageView != null) {
            mAddDestinationImageView.setOnClickListener(this);
        }

        mDestinationsEditText = (TextView) findViewById(R.id.add_trip_destinations_edit_text);
        if (mDestinationsEditText != null) {
            mDestinationsEditText.setOnClickListener(this);
        }

        //region Destinations dialog
        mDestinationListAdapter = new DestinationsDialogListAdapter(
                this, R.layout.destinations_dialog_row, new DestinationsDialogListAdapter.Callback() {
            @Override
            public void placeDeleted(Place place) {
                mTrip.getDestinations().remove(place);
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View dialogView = getLayoutInflater().inflate(R.layout.destinations_dialog_layout, null);
        ListView listView = (ListView) dialogView.findViewById(R.id.dialog_destination_list_view);
        listView.setAdapter(mDestinationListAdapter);
        builder.setNegativeButton(R.string.dialog_close, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                updateDestinationsTextView(mDestinationListAdapter.getPlaces());
            }
        }).setTitle(R.string.title_destinations).setView(dialogView);
        mDestinationsDialog = builder.create();
        //endregion

        mTitleEditText = (EditText) findViewById(R.id.add_trip_title_value);

        mDatePickerDialogFragment = new DatePickerDialogFragment();

        mSimpleDateFormat = new SimpleDateFormat(Utility.DATE_FORMAT_PATTERN, Locale.ENGLISH);
        mTrip = new Trip();

        mStartDateTextView = (TextView) findViewById(R.id.add_trip_start_date_value);
        if (mStartDateTextView != null) {
            mStartDateTextView.setOnClickListener(this);
        }

        mEndDateTextView = (TextView) findViewById(R.id.add_trip_end_date_value);
        if (mEndDateTextView != null) {
            mEndDateTextView.setOnClickListener(this);
        }

        initialiseDateTextViews();

        mNoteEditText = (EditText) findViewById(R.id.add_trip_note_edit_text);

        mSaveButton = (Button) findViewById(R.id.add_trip_save_button);
        if (mSaveButton != null) {
            mSaveButton.setOnClickListener(this);
        }
    }

    private void initialiseDateTextViews() {
        // Set start date to today
        Calendar calendar = Calendar.getInstance();
        mStartDateTextView.setText(mSimpleDateFormat.format(calendar.getTime()));
        mTrip.setStartDate(calendar.getTime());

        // Set end date to tomorrow
        calendar.add(Calendar.DATE, 1);
        mEndDateTextView.setText(mSimpleDateFormat.format(calendar.getTime()));
        mTrip.setEndDate(calendar.getTime());
    }

    // This method displays all the destinations in the destinations text view.
    private void updateDestinationsTextView(List<Place> destinations) {
        String text = "";
        for (Place destination : destinations) {
            text += destination.getName() + ", ";
        }

        // Remove the ending ', '
        if (text.length() > 2) {
            text = text.substring(0, text.length() - 2);
        }

        mDestinationsEditText.setText(text);
    }
}