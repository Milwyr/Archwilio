package com.milton.archwilio.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.milton.archwilio.R;
import com.milton.archwilio.common.DbHelper;
import com.milton.archwilio.common.Utility;
import com.milton.archwilio.fragments.DatePickerDialogFragment;
import com.milton.archwilio.fragments.TimePickerDialogFragment;
import com.milton.archwilio.models.TransportRecord;
import com.milton.archwilio.models.Trip;

import org.joda.time.LocalDate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddTransportActivity extends BaseItineraryActivity implements
        View.OnClickListener,
        DatePickerDialogFragment.OnDateSelectedListener,
        TimePickerDialogFragment.OnTimeSelectedListener {

    //region Constants
    private final int DEPARTURE_PLACE_AUTOCOMPLETE_REQUEST_CODE = 9000;
    private final int ARRIVAL_PLACE_AUTOCOMPLETE_REQUEST_CODE = 9001;
    private final String DEPARTURE_DATE = "departure_date";
    private final String DEPARTURE_TIME = "departure_time";
    private final String ARRIVAL_DATE = "arrival_date";
    private final String ARRIVAL_TIME = "arrival_time";
    //endregion

    //region Variables
    protected Button mDepartureDateButton;
    protected Button mDepartureTimeButton;
    protected Button mArrivalDateButton;
    protected Button mArrivalTimeButton;

    private DatePickerDialogFragment mDatePickerDialogFragment;
    private TimePickerDialogFragment mTimePickerDialogFragment;

    protected EditText mDepartureEditText;
    protected EditText mArrivalEditText;
    protected EditText mReferenceCodeEditText;
    protected EditText mPriceEditText;
    protected EditText mNoteEditText;

    protected Spinner mTravelModeSpinner;
    protected Button mSaveButton;

    protected SimpleDateFormat mSimpleDateFormat;
    protected SimpleDateFormat mSimpleTimeFormat;

    protected TransportRecord mTransportRecord;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transport);

        initialiseComponents();

        // Retrieve data before onSaveInstanceState() method was called
        if (savedInstanceState != null) {
            TransportRecord transportRecord = savedInstanceState.getParcelable(TransportRecord.TABLE_NAME);
            if (transportRecord != null) {
                mTransportRecord = transportRecord;
                populateTransportationData();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Place place = PlaceAutocomplete.getPlace(this, data);
        if (requestCode == DEPARTURE_PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (place != null) {
                mTransportRecord.setPlace(place);
                mDepartureEditText.setText(place.getName());
            }
        } else if (requestCode == ARRIVAL_PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (place != null) {
                mTransportRecord.setArrivalPlace(place);
                mArrivalEditText.setText(place.getName());
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(TransportRecord.TABLE_NAME, mTransportRecord);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_transport_departure_edit_text:
                showPlaceAutoCompleteFragment(DEPARTURE_PLACE_AUTOCOMPLETE_REQUEST_CODE);
                break;
            case R.id.add_transport_departure_date_button:
                showDateDialogFragment(mDepartureDateButton.getText().toString(), DEPARTURE_DATE);
                break;
            case R.id.add_transport_departure_time_button:
                showTimeDialogFragment(mDepartureTimeButton.getText().toString(), DEPARTURE_TIME);
                break;
            case R.id.add_transport_arrival_edit_text:
                showPlaceAutoCompleteFragment(ARRIVAL_PLACE_AUTOCOMPLETE_REQUEST_CODE);
                break;
            case R.id.add_transport_arrival_date_button:
                showDateDialogFragment(mArrivalDateButton.getText().toString(), ARRIVAL_DATE);
                break;
            case R.id.add_transport_arrival_time_button:
                showTimeDialogFragment(mArrivalTimeButton.getText().toString(), ARRIVAL_TIME);
                break;
            case R.id.add_transport_save_button:
                if (!mTransportRecord.isDataValid()) {
                    Snackbar.make(v, R.string.error_missing_fields, Snackbar.LENGTH_LONG).show();
                } else {
                    preSave();

                    if (!isStartAndEndDateValid()) {
                        Snackbar.make(v, R.string.error_wrong_start_or_end_date, Snackbar.LENGTH_LONG).show();
                    } else {
                        boolean isSuccess = DbHelper.getInstance(this)
                                .insertTransportRecord(mCurrentTripId, mTransportRecord);

                        if (isSuccess) {
                            setResult(Utility.SUCCESS_RESULT_CODE);
                            finish();
                        } else {
                            Snackbar.make(v, R.string.error_message_sql, Snackbar.LENGTH_SHORT).show();
                        }
                    }
                }

                break;
        }
    }

    @Override
    public void onDateSelected(int year, int month, int day) {
        String tag = mDatePickerDialogFragment.getTag();
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);

        if (tag.equals(DEPARTURE_DATE)) {
            Date selectedDepartureDate = calendar.getTime();

            mTransportRecord.setStartDateTime(calendar.getTime());
            mDepartureDateButton.setText(mSimpleDateFormat.format(calendar.getTime()));

            if (selectedDepartureDate.after(mTransportRecord.getEndDateTime())) {
                mTransportRecord.setEndDateTime(calendar.getTime());
                mArrivalDateButton.setText(mSimpleDateFormat.format(calendar.getTime()));
            }
        } else if (tag.equals(ARRIVAL_DATE)) {
            Date selectedArrivalDate = calendar.getTime();

            mTransportRecord.setEndDateTime(calendar.getTime());
            mArrivalDateButton.setText(mSimpleDateFormat.format(calendar.getTime()));

            if (selectedArrivalDate.before(mTransportRecord.getStartDateTime())) {
                mDepartureDateButton.setText(mSimpleDateFormat.format(calendar.getTime()));
                mTransportRecord.setStartDateTime(calendar.getTime());
            }

            mArrivalDateButton.setText(mSimpleDateFormat.format(calendar.getTime()));
        }
    }

    @Override
    public void onTimeSelected(int hourOfDay, int minute) {
        String tag = mTimePickerDialogFragment.getTag();
        Calendar calendar = Calendar.getInstance();

        if (tag.equals(DEPARTURE_TIME)) {
            calendar.setTime(mTransportRecord.getStartDateTime());
            calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DATE), hourOfDay, minute);

            mTransportRecord.setStartDateTime(calendar.getTime());
            mDepartureTimeButton.setText(mSimpleTimeFormat.format(calendar.getTime()));
        } else if (tag.equals(ARRIVAL_TIME)) {
            calendar.setTime(mTransportRecord.getEndDateTime());
            calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DATE), hourOfDay, minute);

            mTransportRecord.setEndDateTime(calendar.getTime());
            mArrivalTimeButton.setText(mSimpleTimeFormat.format(calendar.getTime()));
        }
    }

    /**
     * This method saves the transport record if the data record is valid, or displays error message.
     *
     * @param saveOption The option that indicates whether this is an add or an edit event
     * @param v          The view to display error message
     */
    protected void saveTransportRecord(SaveOption saveOption, View v) {
        if (!mTransportRecord.isDataValid()) {
            Snackbar.make(v, R.string.error_missing_fields, Snackbar.LENGTH_LONG).show();
        } else {
            preSave();

            if (!isStartAndEndDateValid()) {
                Snackbar.make(v, R.string.error_wrong_start_or_end_date, Snackbar.LENGTH_LONG).show();
            } else {
                boolean isSuccess;
                if (saveOption == SaveOption.ADD) {
                    isSuccess = DbHelper.getInstance(this)
                            .insertTransportRecord(mCurrentTripId, mTransportRecord);
                } else {
                    isSuccess = DbHelper.getInstance(this)
                            .updateTransportRecord(mCurrentTripId, mTransportRecord);
                }

                if (isSuccess) {
                    setResult(Utility.SUCCESS_RESULT_CODE);
                    finish();
                } else {
                    Snackbar.make(v, R.string.error_message_sql, Snackbar.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * This method reads the inputted values and sets them
     * to be the values of the transport record of this activity.
     * The transport record will be saved in SQLite database.
     */
    private void preSave() {
        String message = mTransportRecord.getPlaceImpl().getAddressString().substring(0, 3) + " " +
                getResources().getString(R.string.to) + " " + mTransportRecord.getArrivalPlaceImpl().getAddressString().substring(0, 3);
        mTransportRecord.setDisplayMessage(message);
        mTransportRecord.setTransportMode(mTravelModeSpinner.getSelectedItemPosition());

        if (!Utility.isNullOrEmpty(mReferenceCodeEditText.getText())) {
            mTransportRecord.setReferenceNumber(mReferenceCodeEditText.getText().toString());
        }

        if (!Utility.isNullOrEmpty(mPriceEditText.getText())) {
            double price = Double.parseDouble(mPriceEditText.getText().toString());
            mTransportRecord.setPrice(price);
        }

        if (mNoteEditText.getText() != null) {
            mTransportRecord.setNote(mNoteEditText.getText().toString());
        }
    }

    /**
     * This method compares the start and end dates of transport record and the associated trip.
     *
     * @return True if the start and end dates of the transport record are in the middle of the trip
     */
    private boolean isStartAndEndDateValid() {
        Trip currentTrip = DbHelper.getInstance(this).getTrip(mCurrentTripId);

        // Start and end time of the hotel record
        LocalDate hStart = new LocalDate(mTransportRecord.getStartDateTime());
        LocalDate hEnd = new LocalDate(mTransportRecord.getEndDateTime());

        // Start and end time of the trip record
        LocalDate tStart = new LocalDate(currentTrip.getStartDate());
        LocalDate tEnd = new LocalDate(currentTrip.getEndDate());

        return !hStart.isBefore(tStart) && !hEnd.isAfter(tEnd);
    }

    /**
     * This method fills data to the fields appeared in the page
     * with values in the instance variable mTransportRecord.
     */
    protected void populateTransportationData() {
        if (mTransportRecord != null) {
            mTravelModeSpinner.setSelection(mTransportRecord.getTransportMode());
            if (mTransportRecord.getPlace() != null) {
                mDepartureEditText.setText(mTransportRecord.getPlace().getName());
            }
            mDepartureDateButton.setText(mSimpleDateFormat.format(mTransportRecord.getStartDateTime()));
            mDepartureTimeButton.setText(mSimpleTimeFormat.format(mTransportRecord.getStartDateTime()));
            if (mTransportRecord.getArrivalPlace() != null) {
                mArrivalEditText.setText(mTransportRecord.getArrivalPlace().getName());
            }
            mArrivalDateButton.setText(mSimpleDateFormat.format(mTransportRecord.getEndDateTime()));
            mArrivalTimeButton.setText(mSimpleTimeFormat.format(mTransportRecord.getEndDateTime()));
            mReferenceCodeEditText.setText(mTransportRecord.getReferenceNumber());
            mPriceEditText.setText(String.valueOf(mTransportRecord.getPrice()));
            mNoteEditText.setText(mTransportRecord.getNote());
        } else {
            super.showErrorDialog(R.string.error_current_transport_record_not_found, true);
        }
    }

    private void initialiseComponents() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_close_white_24dp);
        }

        mTransportRecord = new TransportRecord();

        mDepartureEditText = (EditText) findViewById(R.id.add_transport_departure_edit_text);
        mDepartureEditText.setOnClickListener(this);

        mArrivalEditText = (EditText) findViewById(R.id.add_transport_arrival_edit_text);
        mArrivalEditText.setOnClickListener(this);

        mReferenceCodeEditText = (EditText) findViewById(R.id.reference_code_edit_text);
        mPriceEditText = (EditText) findViewById(R.id.price_value);
        mNoteEditText = (EditText) findViewById(R.id.note_edit_text);

        mTravelModeSpinner = (Spinner) findViewById(R.id.add_transport_travel_mode_value);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                this, R.array.travel_mode_options, R.layout.support_simple_spinner_dropdown_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mTravelModeSpinner.setAdapter(spinnerAdapter);

        mSimpleDateFormat = new SimpleDateFormat(Utility.DATE_FORMAT_PATTERN, Locale.ENGLISH);
        mSimpleTimeFormat = new SimpleDateFormat(Utility.TIME_FORMAT_PATTERN, Locale.ENGLISH);

        Calendar calendar = Calendar.getInstance();

        // Set the start date to be the start date of the trip, and time to be now
        if (mCurrentTrip.getStartDate() != null) {
            calendar.setTime(mCurrentTrip.getStartDate());
            calendar.set(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DATE),
                    Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                    Calendar.getInstance().get(Calendar.MINUTE));
        }

        mTransportRecord.setStartDateTime(calendar.getTime());

        mDepartureDateButton = (Button) findViewById(R.id.add_transport_departure_date_button);
        mDepartureDateButton.setText(mSimpleDateFormat.format(calendar.getTime()));
        mDepartureDateButton.setOnClickListener(this);

        mDepartureTimeButton = (Button) findViewById(R.id.add_transport_departure_time_button);
        mDepartureTimeButton.setText(mSimpleTimeFormat.format(calendar.getTime()));
        mDepartureTimeButton.setOnClickListener(this);

        calendar.add(Calendar.HOUR_OF_DAY, 2);
        mTransportRecord.setEndDateTime(calendar.getTime());

        mArrivalDateButton = (Button) findViewById(R.id.add_transport_arrival_date_button);
        mArrivalDateButton.setText(mSimpleDateFormat.format(calendar.getTime()));
        mArrivalDateButton.setOnClickListener(this);

        mArrivalTimeButton = (Button) findViewById(R.id.add_transport_arrival_time_button);
        mArrivalTimeButton.setText(mSimpleTimeFormat.format(calendar.getTime()));
        mArrivalTimeButton.setOnClickListener(this);

        TextView currencyTextView = (TextView) findViewById(R.id.currency_title);
        currencyTextView.setText(super.getCurrencySymbol());

        mSaveButton = (Button) findViewById(R.id.add_transport_save_button);
        mSaveButton.setOnClickListener(this);
    }

    private void showPlaceAutoCompleteFragment(int requestCode) {
        try {
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                            .build(this);
            startActivityForResult(intent, requestCode);

        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    private void showDateDialogFragment(String dateText, String tag) {
        try {
            Calendar calendar = Calendar.getInstance();
            Date date = mSimpleDateFormat.parse(dateText);
            calendar.setTime(date);
            mDatePickerDialogFragment = DatePickerDialogFragment.newInstance(calendar);
        } catch (ParseException e) {
            mDatePickerDialogFragment = new DatePickerDialogFragment();
        }
        mDatePickerDialogFragment.show(getFragmentManager(), tag);
    }

    private void showTimeDialogFragment(String timeText, String tag) {
        try {
            Calendar calendar = Calendar.getInstance();
            Date time = mSimpleTimeFormat.parse(timeText);
            calendar.setTime(time);
            mTimePickerDialogFragment = TimePickerDialogFragment.newInstance(calendar);
        } catch (ParseException e) {
            mTimePickerDialogFragment = new TimePickerDialogFragment();
        }
        mTimePickerDialogFragment.show(getFragmentManager(), tag);
    }
}