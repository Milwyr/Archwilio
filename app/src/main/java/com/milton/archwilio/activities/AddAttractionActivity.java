package com.milton.archwilio.activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TimePicker;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.milton.archwilio.R;
import com.milton.archwilio.common.DbHelper;
import com.milton.archwilio.common.PhotoWorker;
import com.milton.archwilio.common.Utility;
import com.milton.archwilio.common.VolleySingleton;
import com.milton.archwilio.models.AttractionRecord;

import org.joda.time.LocalDate;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class AddAttractionActivity extends BaseItineraryActivity implements
        View.OnClickListener, DialogInterface.OnClickListener,
        TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener {
    private final int PLACE_PICKER_REQUEST_CODE = 8001;
    private final String IMAGES_FOLDER_NAME = "images";

    //region Instance variables
    private CoordinatorLayout mCoordinatorLayout;
    private ImageView mHeaderImageView;
    private TextView mHeaderImageTitleTextView;
    private Button mStartDateButton;
    private Button mStartTimeButton;
    private SeekBar mSeekBar;
    private TextView mSeekBarValueTextView;
    private EditText mNoteEditText;
    private Button mSaveButton;

    private SimpleDateFormat mSimpleDateFormat;
    private SimpleDateFormat mSimpleTimeFormat;
    private VolleySingleton mVolleyInstance;

    private List<String> mPhotoUrls;
    protected AttractionRecord mAttractionRecord;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_attraction);

        initialiseComponents();

        if (savedInstanceState != null) {
            // Populate data if there is an orientation change, i.e. savedInstanceState is not null
            AttractionRecord attractionRecord = savedInstanceState.getParcelable(AttractionRecord.TABLE_NAME);
            if (attractionRecord != null) {
                mAttractionRecord = attractionRecord;
                populateAttractionData();
                showAllViews();
                updateUserInterface();
            }
        } else {
            // Only show the place picker dialog if the activity is not called by
            // EditAttractionActivity, i.e. A string with key 'Mode' = 'Add'
            // as it was added in ItineraryActivity
            String mode = getIntent().getStringExtra("Mode");
            if (mode != null && mode.equals("Add")) {
                showSelectPlaceDialog();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST_CODE) {
            if (data == null) {
                // Exit the activity if the user does not select an attraction
                setResult(Utility.FAIL_RESULT_CODE);
                finish();
            } else {
                // Display the hidden views when the user has selected a place
                Place place = PlacePicker.getPlace(this, data);
                if (place != null) {
                    mAttractionRecord.setPlace(PlacePicker.getPlace(this, data));
                    updateUserInterface();
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(AttractionRecord.TABLE_NAME, mAttractionRecord);
    }

    /**
     * This method does two things. First, it searches for the header image in the local directory,
     * or download from Wikipedia if not found. Second, it sets all the views to be visible.
     */
    protected void updateUserInterface() {
        String folderPath = IMAGES_FOLDER_NAME + File.separator + mAttractionRecord.getPlace().getId();
        File photoDirectory = new File(getExternalFilesDir(null), folderPath);

        // If the directory which contains the photos is found
        if (photoDirectory.exists() && photoDirectory.isDirectory() &&
                photoDirectory.listFiles().length > 0) {
            File photoFile = photoDirectory.listFiles()[0];
            Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getPath());
            mHeaderImageView.setImageBitmap(bitmap);
            showAllViews();
        } else {
            fetchPhotoUrlsFromWikimedia(mAttractionRecord.getPlace());
        }
    }

    /**
     * This method fetches photo urls from Wikimedia, displays it as the header image
     * and displays the name as well.
     *
     * @param place Place
     */
    private void fetchPhotoUrlsFromWikimedia(final Place place) {
        final PhotoWorker worker = new PhotoWorker(this);

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getResources().getString(R.string.progressing));
        progressDialog.setCancelable(false);
        progressDialog.show();

        worker.fetchPhotoUrls(5, place.getLatLng(), 3d / 2d, new PhotoWorker.ResultsCallback() {
            @Override
            public void onFinish(List<String> photoUrls) {
                if (photoUrls.size() == 0) {
                    progressDialog.dismiss();
                    mHeaderImageView.setImageResource(R.drawable.ic_indigo_light);
                    showAllViews();
                } else {
                    String url = photoUrls.get(0);
                    int widthPixels = getResources().getDisplayMetrics().widthPixels;

                    ImageRequest imageRequest = new ImageRequest(
                            url, new Response.Listener<Bitmap>() {
                        @Override
                        public void onResponse(Bitmap response) {
                            progressDialog.dismiss();
                            mHeaderImageView.setImageBitmap(response);
                            showAllViews();
                        }
                    }, widthPixels, widthPixels * 3 / 2, ImageView.ScaleType.CENTER_CROP, null,
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    progressDialog.dismiss();
                                    mHeaderImageView.setImageResource(R.drawable.ic_indigo_light);
                                    showAllViews();
                                }
                            }
                    );
                    mVolleyInstance.addToRequestQueue(imageRequest);
                    mPhotoUrls = photoUrls;
                }
            }

            @Override
            public void onError(Exception e) {
                progressDialog.hide();
                mHeaderImageView.setImageResource(R.drawable.ic_indigo_light);
                showAllViews();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_date_button:
                // Show date picker dialog
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(mAttractionRecord.getStartDateTime());
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int date = calendar.get(Calendar.DATE);
                new DatePickerDialog(this, this, year, month, date).show();
                break;
            case R.id.start_time_button:
                // Show time picker dialog
                Calendar c = Calendar.getInstance();
                c.setTime(mAttractionRecord.getStartDateTime());
                int hourOfDay = c.get(Calendar.HOUR_OF_DAY);
                int minute = c.get(Calendar.MINUTE);
                new TimePickerDialog(this, this, hourOfDay, minute, true).show();
                break;
            case R.id.save_button:
                saveAttractionRecord(SaveOption.ADD);
                break;
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int position) {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

        // Set the initial latitude and longitude boundaries
        if (mCurrentTrip != null && mCurrentTrip.getDestinations().size() > 0) {
            Place destination = mCurrentTrip.getDestinations().get(position);

            LatLngBounds bounds = destination.getViewport();
            if (bounds != null) {
                builder.setLatLngBounds(bounds);
            } else {
                LatLng latLng = destination.getLatLng();
                builder.setLatLngBounds(calculateLatLngBounds(latLng));
            }
        }

        try {
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            Snackbar.make(mCoordinatorLayout, e.getMessage(), Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, monthOfYear, dayOfMonth);

        mAttractionRecord.setStartDateTime(calendar.getTime());
        mStartDateButton.setText(mSimpleDateFormat.format(calendar.getTime()));
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        // This calendar instance has the current year, month and date entered by the user
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(mAttractionRecord.getStartDateTime());

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DATE);
        calendar.set(year, month, day, hourOfDay, minute);

        mAttractionRecord.setStartDateTime(calendar.getTime());
        mStartTimeButton.setText(mSimpleTimeFormat.format(calendar.getTime()));
    }

    private void initialiseComponents() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_close_white_24dp);
        }
        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);

        mVolleyInstance = VolleySingleton.getInstance(this);
        mAttractionRecord = new AttractionRecord();

        mHeaderImageView = (ImageView) findViewById(R.id.add_attraction_header_image);
        mHeaderImageTitleTextView = (TextView) findViewById(R.id.add_attraction_header_image_title);

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

        mStartDateButton = (Button) findViewById(R.id.start_date_button);
        mStartDateButton.setText(mSimpleDateFormat.format(calendar.getTime()));
        mStartDateButton.setOnClickListener(this);

        mStartTimeButton = (Button) findViewById(R.id.start_time_button);
        mStartTimeButton.setText(mSimpleTimeFormat.format(Calendar.getInstance().getTime()));
        mStartTimeButton.setOnClickListener(this);
        mAttractionRecord.setStartDateTime(calendar.getTime());

        mSeekBarValueTextView = (TextView) findViewById(R.id.duration_value_text_view);
        mSeekBar = (SeekBar) findViewById(R.id.duration_seek_bar);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String text = Integer.toString(progress) + " " + getResources().getString(R.string.hour);
                if (progress > 1) {
                    text += "s";
                }
                mSeekBarValueTextView.setText(text);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mNoteEditText = (EditText) findViewById(R.id.note_edit_text);

        mSaveButton = (Button) findViewById(R.id.save_button);
        mSaveButton.setOnClickListener(this);
    }

    /**
     * This method sets all the views in this Activity to be visible.
     */
    protected void showAllViews() {
        mHeaderImageTitleTextView.setText(mAttractionRecord.getPlace().getName());

        displayPlaceDetails();

        NestedScrollView nestedScrollView = (NestedScrollView) findViewById(R.id.nested_scroll_view);
        nestedScrollView.setVisibility(View.VISIBLE);
    }

    protected void setEnabledStatus(boolean isEnabled) {
        mStartDateButton.setEnabled(isEnabled);
        mStartTimeButton.setEnabled(isEnabled);
        mSeekBar.setEnabled(isEnabled);
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

    /**
     * This method sets the values on the activity to
     * the values stored in the given AttractionRecord object.
     */
    protected void populateAttractionData() {
        if (mAttractionRecord != null) {
            mStartDateButton.setText(mSimpleDateFormat.format(mAttractionRecord.getStartDateTime()));
            mStartTimeButton.setText(mSimpleTimeFormat.format(mAttractionRecord.getStartDateTime()));
            mSeekBar.setProgress(mAttractionRecord.getDuration());
            mNoteEditText.setText(mAttractionRecord.getNote());

            String text = Integer.toString(mAttractionRecord.getDuration()) + " " + getResources().getString(R.string.hour);
            if (mAttractionRecord.getDuration() > 1) {
                text += "s";
            }
            mSeekBarValueTextView.setText(text);
        }
    }

    /**
     * This method saves the attraction record if the data record is valid, or displays error message.
     *
     * @param saveOption The option that indicates whether this is an add or an edit event
     */
    protected void saveAttractionRecord(SaveOption saveOption) {
        preSave();

        if (!isStartAndEndDateValid()) {
            Snackbar.make(mCoordinatorLayout,
                    R.string.error_wrong_start_or_end_date, Snackbar.LENGTH_SHORT).show();
        } else {
            boolean isSuccess;
            if (saveOption == SaveOption.ADD) {
                isSuccess = DbHelper.getInstance(this)
                        .insertAttractionRecord(mCurrentTripId, mAttractionRecord);
            } else {
                isSuccess = DbHelper.getInstance(this)
                        .updateAttractionRecord(mCurrentTripId, mAttractionRecord);
            }

            if (isSuccess) {
                setResult(Utility.SUCCESS_RESULT_CODE);
                finish();
            } else {
                Snackbar.make(mCoordinatorLayout,
                        R.string.error_message_sql, Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * This method must be executed being saving the attraction record.
     * It reads the inputted values and sets them to the attraction record to be saved in database.
     */
    private void preSave() {
        // Save the photo to local directory
        if (mPhotoUrls != null && mPhotoUrls.size() > 0) {
            File placePhotosFolder = new File(IMAGES_FOLDER_NAME,
                    mAttractionRecord.getPlace().getId());

            if (!placePhotosFolder.exists()) {
                downloadPhotos(placePhotosFolder.getPath());
            }
        }

        if (mNoteEditText != null && mNoteEditText.getText() != null) {
            mAttractionRecord.setNote(mNoteEditText.getText().toString());
        }

        mAttractionRecord.setDuration(mSeekBar.getProgress());
    }

    /**
     * This method compares the start and end dates of attraction record and
     * the associated trip.
     *
     * @return True if the start and end dates of the attraction record are in the middle of the trip
     */
    private boolean isStartAndEndDateValid() {
        // Start and end time of the hotel record
        LocalDate hStart = new LocalDate(mAttractionRecord.getStartDateTime());
        LocalDate hEnd = new LocalDate(mAttractionRecord.getEndDateTime());

        // Start and end time of the trip record
        LocalDate tStart = new LocalDate(mCurrentTrip.getStartDate());
        LocalDate tEnd = new LocalDate(mCurrentTrip.getEndDate());

        return !hStart.isBefore(tStart) && !hEnd.isAfter(tEnd);
    }

    // Display the place details which have values
    private void displayPlaceDetails() {
        View view = findViewById(R.id.place_details_layout);

        if (mAttractionRecord.getPlace().getAddress() != null &&
                !mAttractionRecord.getPlace().getAddress().toString().isEmpty()) {
            TextView addressTextView = (TextView) view.findViewById(R.id.address_text_view);
            addressTextView.setText(mAttractionRecord.getPlace().getAddress().toString());

            TableRow addressRow = (TableRow) view.findViewById(R.id.address_table_row);
            addressRow.setVisibility(View.VISIBLE);
        }

        if (mAttractionRecord.getPlace().getPhoneNumber() != null &&
                !mAttractionRecord.getPlace().getPhoneNumber().toString().isEmpty()) {
            TextView phoneNumberTextView = (TextView) view.findViewById(R.id.phone_number_text_view);
            phoneNumberTextView.setText(mAttractionRecord.getPlace().getPhoneNumber().toString());

            TableRow phoneNumberRow = (TableRow) view.findViewById(R.id.phone_number_table_row);
            phoneNumberRow.setVisibility(View.VISIBLE);
        }

        if (mAttractionRecord.getPlace().getWebsiteUri() != null &&
                !mAttractionRecord.getPlace().getWebsiteUri().toString().isEmpty()) {
            TextView websiteUriTextView = (TextView) view.findViewById(R.id.website_uri_text_view);
            websiteUriTextView.setText(mAttractionRecord.getPlace().getWebsiteUri().toString());

            TableRow websiteUriRow = (TableRow) view.findViewById(R.id.website_uri_table_row);
            websiteUriRow.setVisibility(View.VISIBLE);
        }
    }

    /**
     * This method shows a dialog which contains a list of places of the trip.
     * When the user selects a place, a place picker is shown.
     */
    private void showSelectPlaceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.title_destinations)
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        finish();
                    }
                });

        if (mCurrentTrip != null && mCurrentTrip.getDestinations().size() > 0) {
            // Retrieve a list of names of destinations
            List<String> destinations = new ArrayList<>();
            for (Place destination : mCurrentTrip.getDestinations()) {
                if (destination.getName() != null)
                    destinations.add(destination.getName().toString());
            }

            String[] destinationArray = new String[destinations.size()];

            builder.setItems(destinations.toArray(destinationArray), this);
            builder.create().show();
        } else {
            Snackbar.make(mCoordinatorLayout, R.string.error_no_destination_found, Snackbar.LENGTH_LONG).show();
        }
    }

    private LatLngBounds calculateLatLngBounds(LatLng centre) {
        // 0.01 is a hard coded value that moves the latitude and longitude
        LatLng northEastLatLng = new LatLng(centre.latitude + 0.01, centre.longitude + 0.01);
        LatLng southWestLatLng = new LatLng(centre.latitude - 0.01, centre.longitude - 0.01);
        return new LatLngBounds(southWestLatLng, northEastLatLng);
    }

    /**
     * This download task is done in a separate thread, so it does not block the user interface.
     *
     * @param photosFolderPath File path of the photo to save to
     */
    private void downloadPhotos(final String photosFolderPath) {
        final PhotoWorker worker = new PhotoWorker(this);

        for (String url : mPhotoUrls) {
            // Request the image from the url
            ImageRequest imageRequest = new ImageRequest(
                    url, new Response.Listener<Bitmap>() {
                @Override
                public void onResponse(Bitmap response) {
                    String fileName = Integer.toString(
                            new Random().nextInt(1000000000) + 10000000);

                    try {
                        // Download the image to local storage
                        worker.savePhoto(response, photosFolderPath, fileName + ".jpg");
                    } catch (IOException e) {
//                        Log.e(ERROR_TAG, e.getMessage());
                    }
                }
            }, 1080, 810, ImageView.ScaleType.CENTER_CROP, null,
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
//                            Log.e(ERROR_TAG, error.getMessage());
                        }
                    }
            );
            mVolleyInstance.addToRequestQueue(imageRequest);
        }
    }
}