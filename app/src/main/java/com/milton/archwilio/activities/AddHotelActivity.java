package com.milton.archwilio.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.MarkerOptions;
import com.milton.archwilio.R;
import com.milton.archwilio.common.DbHelper;
import com.milton.archwilio.common.Utility;
import com.milton.archwilio.fragments.DatePickerDialogFragment;
import com.milton.archwilio.models.HotelRecord;
import com.milton.archwilio.models.Trip;

import org.joda.time.LocalDate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddHotelActivity extends BaseItineraryActivity implements
        OnMapReadyCallback, View.OnClickListener,
        DatePickerDialogFragment.OnDateSelectedListener {

    //region Constants
    private final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 9000;
    private final String CHECK_IN_DATE = "check_in";
    private final String CHECK_OUT_DATE = "check_out";
    //endregion

    //region Instance variables
    private GoogleMap mGoogleMap;
    private EditText mHotelNameEditText;
    private EditText mHotelAddressEditText;
    private TextView mCheckInDateTextView;
    private TextView mCheckOutDateTextView;
    private TextView mNumberOfGuestsValueTextView;
    private EditText mTotalPriceEditText;
    private EditText mNoteEditText;
    private Button mSaveButton;

    private DatePickerDialogFragment mDatePickerDialogFragment;
    protected SimpleDateFormat mDateFormat;

    // The hotel record constructed based on the values of the input fields
    protected HotelRecord mHotelRecord;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_hotel);

        initialiseComponents();

        // Retrieve data before onSaveInstanceState() method was called
        if (savedInstanceState != null) {
            HotelRecord hotelRecord = savedInstanceState.getParcelable(HotelRecord.TABLE_NAME);
            if (hotelRecord != null) {
                mHotelRecord = hotelRecord;
                populateHotelData();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            Place place = PlaceAutocomplete.getPlace(this, data);
            if (place != null) {
                mHotelRecord.setPlace(place);
                mHotelNameEditText.setText(place.getName());
                mHotelAddressEditText.setText(place.getAddress());

                updateGoogleMap();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(HotelRecord.TABLE_NAME, mHotelRecord);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.hotel_name_value_edit_text:
                try {
                    // Show place autocomplete fragment
                    Intent intent =
                            new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                                    .build(this);
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);

                } catch (GooglePlayServicesRepairableException |
                        GooglePlayServicesNotAvailableException e) {
                    Snackbar.make(v, e.getMessage(), Snackbar.LENGTH_SHORT).show();
                }
                break;
            case R.id.hotel_check_in_date_text_view:
                try {
                    Calendar calendar = Calendar.getInstance();
                    Date date = mDateFormat.parse(mCheckInDateTextView.getText().toString());
                    calendar.setTime(date);
                    mDatePickerDialogFragment = DatePickerDialogFragment.newInstance(calendar);
                } catch (ParseException e) {
                    mDatePickerDialogFragment = new DatePickerDialogFragment();
                    e.printStackTrace();
                }
                mDatePickerDialogFragment.show(getFragmentManager(), CHECK_IN_DATE);
                break;
            case R.id.hotel_check_out_date_text_view:

                try {
                    Calendar calendar = Calendar.getInstance();
                    Date date = mDateFormat.parse(mCheckOutDateTextView.getText().toString());
                    calendar.setTime(date);
                    mDatePickerDialogFragment = DatePickerDialogFragment.newInstance(calendar);
                } catch (ParseException e) {
                    mDatePickerDialogFragment = new DatePickerDialogFragment();
                }

                mDatePickerDialogFragment.show(getFragmentManager(), CHECK_OUT_DATE);
                break;
            case R.id.add_guest_button:
                int numberOfGuests = Integer.parseInt(mNumberOfGuestsValueTextView.getText().toString());
                mHotelRecord.setNumberOfGuests(numberOfGuests + 1);
                mNumberOfGuestsValueTextView.setText(String.valueOf(numberOfGuests + 1));
                break;
            case R.id.minus_guest_button:
                // Minimum number of guests is 1, therefore 2 is the minimum requirement
                numberOfGuests = Integer.parseInt(mNumberOfGuestsValueTextView.getText().toString());
                numberOfGuests = Math.max(numberOfGuests, 2);
                mHotelRecord.setNumberOfGuests(numberOfGuests - 1);
                mNumberOfGuestsValueTextView.setText(String.valueOf(numberOfGuests - 1));
                break;
            case R.id.add_hotel_save_button:
                saveHotelRecord(SaveOption.ADD, v);
                break;
        }
    }

    @Override
    public void onDateSelected(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);

        String tag = mDatePickerDialogFragment.getTag();
        if (tag.equals(CHECK_IN_DATE)) {
            mHotelRecord.setStartDateTime(calendar.getTime());

            Date selectedCheckedInDate = calendar.getTime();
            Date checkoutDate = mHotelRecord.getEndDateTime();

            mCheckInDateTextView.setText(mDateFormat.format(selectedCheckedInDate));
            mHotelRecord.setStartDateTime(selectedCheckedInDate);

            if (selectedCheckedInDate.after(checkoutDate)) {
                calendar.add(Calendar.DATE, 1); // Set date to tomorrow
                mCheckOutDateTextView.setText(mDateFormat.format(calendar.getTime()));
                mHotelRecord.setEndDateTime(calendar.getTime());
            }
        } else if (tag.equals(CHECK_OUT_DATE)) {
            mHotelRecord.setEndDateTime(calendar.getTime());

            Date checkInDate = mHotelRecord.getStartDateTime();
            Date selectedCheckoutDate = calendar.getTime();

            mCheckOutDateTextView.setText(mDateFormat.format(selectedCheckoutDate));
            mHotelRecord.setEndDateTime(selectedCheckoutDate);

            if (selectedCheckoutDate.before(checkInDate)) {
                calendar.add(Calendar.DATE, -1); // Set date to yesterday
                mCheckInDateTextView.setText(mDateFormat.format(calendar.getTime()));
                mHotelRecord.setStartDateTime(calendar.getTime());
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        updateGoogleMap();
    }

    protected void setEnabledStatus(boolean isEnabled) {
        mHotelNameEditText.setEnabled(isEnabled);
        mHotelAddressEditText.setEnabled(isEnabled);
        mCheckInDateTextView.setEnabled(isEnabled);
        mCheckOutDateTextView.setEnabled(isEnabled);
        findViewById(R.id.minus_guest_button).setEnabled(isEnabled);
        findViewById(R.id.add_guest_button).setEnabled(isEnabled);
        mNumberOfGuestsValueTextView.setEnabled(isEnabled);
        mTotalPriceEditText.setEnabled(isEnabled);
        mNoteEditText.setEnabled(isEnabled);

        if (isEnabled) {
            mSaveButton.setVisibility(View.VISIBLE);
            findViewById(R.id.fab).setVisibility(View.GONE);
        } else {
            mSaveButton.setVisibility(View.GONE);
            findViewById(R.id.fab).setVisibility(View.VISIBLE);
        }
    }

    /**
     * This method fills data to the fields appeared in the page
     * with values in the instance variable mHotelRecord.
     */
    protected void populateHotelData() {
        if (mHotelRecord != null) {
            if (mHotelRecord.getPlace() != null) {
                mHotelNameEditText.setText(mHotelRecord.getPlace().getName());
                mHotelAddressEditText.setText(mHotelRecord.getPlace().getAddress());
            }
            mCheckInDateTextView.setText(mDateFormat.format(mHotelRecord.getStartDateTime()));
            mCheckOutDateTextView.setText(mDateFormat.format(mHotelRecord.getEndDateTime()));
            mNumberOfGuestsValueTextView.setText(String.valueOf(mHotelRecord.getNumberOfGuests()));
            mTotalPriceEditText.setText(String.valueOf(mHotelRecord.getTotalPrice()));
            mNoteEditText.setText(mHotelRecord.getNote());
        } else {
            super.showErrorDialog(R.string.error_current_hotel_record_not_found, true);
        }
    }

    /**
     * This method zooms to the place stored in mHotelRecord instance and adds a marker.
     */
    protected void updateGoogleMap() {
        mGoogleMap.clear();
        if (mHotelRecord.getPlace() != null && mHotelRecord.getPlace().getLatLng() != null
                && mHotelRecord.getPlace().getName() != null) {
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(mHotelRecord.getPlace().getLatLng())
                    .title(mHotelRecord.getPlace().getName().toString());
            mGoogleMap.addMarker(markerOptions);
            mGoogleMap.moveCamera(CameraUpdateFactory
                    .newLatLngZoom(mHotelRecord.getPlace().getLatLng(), 15));
        }
    }

    /**
     * This method compares the start and end dates of hotel record and the associated trip.
     *
     * @return True if the start and end dates of the hotel record are in the middle of the trip
     */
    protected boolean isStartAndEndDateValid() {
        Trip currentTrip = DbHelper.getInstance(this).getTrip(mCurrentTripId);

        // Start and end time of the hotel record
        LocalDate hStart = new LocalDate(mHotelRecord.getStartDateTime());
        LocalDate hEnd = new LocalDate(mHotelRecord.getEndDateTime());

        // Start and end time of the trip record
        LocalDate tStart = new LocalDate(currentTrip.getStartDate());
        LocalDate tEnd = new LocalDate(currentTrip.getEndDate());

        return !hStart.isBefore(tStart) && !hEnd.isAfter(tEnd);
    }

    /**
     * This method saves the hotel record if the data record is valid, or displays error message.
     *
     * @param saveOption The option that indicates whether this is an add or an edit event
     * @param v          The view to display error message
     */
    protected void saveHotelRecord(SaveOption saveOption, View v) {
        if (mHotelRecord.getPlace() == null) {
            Snackbar.make(v, R.string.error_no_place_selected, Snackbar.LENGTH_SHORT).show();
        } else {
            try {
                preSave();

                if (!isStartAndEndDateValid()) {
                    Snackbar.make(v, R.string.error_wrong_start_or_end_date, Snackbar.LENGTH_SHORT).show();
                } else {
                    boolean isSuccess = false;
                    if (saveOption == SaveOption.ADD) {
                        isSuccess = DbHelper.getInstance(this)
                                .insertHotelRecord(mCurrentTripId, mHotelRecord);
                    } else if (saveOption == SaveOption.EDIT) {
                        isSuccess = DbHelper.getInstance(this)
                                .updateHotelRecord(mCurrentTripId, mHotelRecord);
                    }

                    if (isSuccess) {
                        setResult(Utility.SUCCESS_RESULT_CODE);
                        finish();
                    } else {
                        Snackbar.make(v, R.string.error_message_sql, Snackbar.LENGTH_SHORT).show();
                    }
                }
            } catch (NumberFormatException e) {
                Snackbar.make(v, R.string.error_wrong_number_format, Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * This method reads the inputted values and sets them
     * to be the values of the hotel record of this activity.
     * The hotel record will be saved in SQLite database.
     *
     * @throws NumberFormatException
     */
    private void preSave() throws NumberFormatException {
        String note = "";
        if (mNoteEditText != null && mNoteEditText.getText() != null) {
            note = mNoteEditText.getText().toString();
        }
        mHotelRecord.setNote(note);

        int numberOfGuests = Integer.parseInt(
                mNumberOfGuestsValueTextView.getText().toString());
        mHotelRecord.setNumberOfGuests(numberOfGuests);

        double totalPrice = Double.parseDouble(mTotalPriceEditText.getText().toString());
        mHotelRecord.setTotalPrice(totalPrice);
    }

    private void initialiseComponents() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_close_white_24dp);
        }

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);

        mHotelNameEditText = (EditText) findViewById(R.id.hotel_name_value_edit_text);
        mHotelNameEditText.setOnClickListener(this);
        mHotelAddressEditText = (EditText) findViewById(R.id.hotel_address_edit_text);

        mHotelRecord = new HotelRecord();

        mDateFormat = new SimpleDateFormat(Utility.DATE_FORMAT_PATTERN, Locale.ENGLISH);

        // Set date for the check in date text view that is the start date of the trip
        mCheckInDateTextView = (TextView) findViewById(R.id.hotel_check_in_date_text_view);
        mCheckInDateTextView.setText(mDateFormat.format(mCurrentTrip.getStartDate()));
        mCheckInDateTextView.setOnClickListener(this);
        mHotelRecord.setStartDateTime(mCurrentTrip.getStartDate());

        // Set check out date one day after check in day if end date of the trip is after start date
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(mCurrentTrip.getStartDate());
        if (new LocalDate(mCurrentTrip.getEndDate()).isAfter(
                new LocalDate(mCurrentTrip.getStartDate()))) {
            calendar.add(Calendar.DATE, 1);
        }

        // Set date for the check out date text view
        mCheckOutDateTextView = (TextView) findViewById(R.id.hotel_check_out_date_text_view);
        mCheckOutDateTextView.setText(mDateFormat.format(calendar.getTime()));
        mCheckOutDateTextView.setOnClickListener(this);
        mHotelRecord.setEndDateTime(calendar.getTime());

        mNumberOfGuestsValueTextView = (TextView) findViewById(R.id.hotel_guests_value_text_view);

        ImageButton addGuestButton = (ImageButton) findViewById(R.id.add_guest_button);
        addGuestButton.setOnClickListener(this);
        ImageButton minusGuestButton = (ImageButton) findViewById(R.id.minus_guest_button);
        minusGuestButton.setOnClickListener(this);

        TextView currencyTextView = (TextView) findViewById(R.id.hotel_currency_text_view);
        currencyTextView.setText(super.getCurrencySymbol());

        mTotalPriceEditText = (EditText) findViewById(R.id.hotel_total_price_value_edit_text);
        mNoteEditText = (EditText) findViewById(R.id.note_edit_text);

        mSaveButton = (Button) findViewById(R.id.add_hotel_save_button);
        mSaveButton.setOnClickListener(this);
    }
}