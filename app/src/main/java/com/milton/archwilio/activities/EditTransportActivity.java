package com.milton.archwilio.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.NestedScrollView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.milton.archwilio.R;
import com.milton.archwilio.adapters.Section;
import com.milton.archwilio.common.DbHelper;

/**
 * Created by Milton on 09/04/2016.
 */
public class EditTransportActivity extends AddTransportActivity {
    private FloatingActionButton mFab;
    private long mCurrentTransportRecordId;
    private boolean mIsReadOnly;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            // This code is only executed when the orientation did not change, i.e.
            // savedInstanceState is null. The reason is data for transportation
            // have been populated in the parent activity 'AddTransportActivity'.
            if (getIntent() != null) {
                mCurrentTransportRecordId = getIntent()
                        .getLongExtra(Section.EXTRA_KEY_CURRENT_TRANSPORT_RECORD_ID, -1);

                if (mCurrentTransportRecordId != -1) {
                    initialiseComponents();
                    setEnabledStatus(false);
                    mTransportRecord = DbHelper.getInstance(this)
                            .getTransportRecord(mCurrentTripId, mCurrentTransportRecordId);
                    super.populateTransportationData();
                } else {
                    showErrorDialog(R.string.error_current_transport_record_not_found, true);
                }
            } else {
                showErrorDialog(R.string.error_general, true);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_itinerary_item, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                // Show a dialog to let the user confirm deleting the transport record
                new AlertDialog.Builder(this)
                        .setMessage(R.string.dialog_delete_confirmation)
                        .setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DbHelper.getInstance(EditTransportActivity.this)
                                        .deleteTransportRecord(mCurrentTransportRecordId);
                                finish();
                            }
                        })
                        .setNegativeButton(R.string.dialog_no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).create().show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                mIsReadOnly = false;
                setEnabledStatus(true);
                break;
            case R.id.add_transport_departure_edit_text:
                super.onClick(v);
                break;
            case R.id.add_transport_departure_date_button:
                super.onClick(v);
                break;
            case R.id.add_transport_departure_time_button:
                super.onClick(v);
                break;
            case R.id.add_transport_arrival_edit_text:
                super.onClick(v);
                break;
            case R.id.add_transport_arrival_date_button:
                super.onClick(v);
                break;
            case R.id.add_transport_arrival_time_button:
                super.onClick(v);
                break;
            case R.id.add_transport_save_button:
                super.saveTransportRecord(SaveOption.EDIT, v);
                break;
        }
    }

    private void initialiseComponents() {
        mIsReadOnly = true;

        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(this);

        NestedScrollView nestedScrollView = (NestedScrollView) findViewById(R.id.nested_scroll_view);
        nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                // Floating action button is only shown in read only mode
                if (mIsReadOnly) {
                    // Swipe down
                    if (scrollY - oldScrollY > 10) {
                        mFab.hide();
                    }
                    // Swipe up
                    else if (scrollY < oldScrollY) {
                        mFab.show();
                    }
                }
            }
        });
    }

    private void setEnabledStatus(boolean isEnabled) {
        mTravelModeSpinner.setEnabled(isEnabled);
        mDepartureEditText.setEnabled(isEnabled);
        mDepartureDateButton.setEnabled(isEnabled);
        mDepartureTimeButton.setEnabled(isEnabled);
        mArrivalEditText.setEnabled(isEnabled);
        mArrivalDateButton.setEnabled(isEnabled);
        mArrivalTimeButton.setEnabled(isEnabled);
        mReferenceCodeEditText.setEnabled(isEnabled);
        mPriceEditText.setEnabled(isEnabled);
        mNoteEditText.setEnabled(isEnabled);

        if (isEnabled) {
            mFab.setVisibility(View.GONE);
            mSaveButton.setVisibility(View.VISIBLE);
        } else {
            mFab.setVisibility(View.VISIBLE);
            mSaveButton.setVisibility(View.GONE);
        }

    }
}