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
public class EditHotelActivity extends AddHotelActivity {
    private long mCurrentHotelRecordId;
    private boolean mIsReadOnly;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            // This code is only executed when the orientation did not change, i.e.
            // savedInstanceState is null. The reason is data for hotels have been
            // populated in the parent activity 'AddHotelActivity'.
            if (getIntent() != null) {
                mCurrentHotelRecordId = getIntent().getLongExtra(
                        Section.EXTRA_KEY_CURRENT_HOTEL_RECORD_ID, -1);

                if (mCurrentHotelRecordId != -1) {
                    initialiseComponents();
                    super.setEnabledStatus(false);
                    mHotelRecord = DbHelper.getInstance(this)
                            .getHotelRecord(mCurrentTripId, mCurrentHotelRecordId);
                    super.populateHotelData();
                } else {
                    super.showErrorDialog(R.string.error_current_hotel_record_not_found, true);
                }
            } else {
                super.showErrorDialog(R.string.error_general, true);
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
                // Show a dialog to let the user confirm deleting the hotel record
                new AlertDialog.Builder(this)
                        .setMessage(R.string.dialog_delete_confirmation)
                        .setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DbHelper.getInstance(EditHotelActivity.this)
                                        .deleteHotelRecord(mCurrentHotelRecordId);
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
            case R.id.hotel_name_value_edit_text:
                super.onClick(v);
                break;
            case R.id.hotel_check_in_date_text_view:
                super.onClick(v);
                break;
            case R.id.hotel_check_out_date_text_view:
                super.onClick(v);
                break;
            case R.id.add_guest_button:
                super.onClick(v);
                break;
            case R.id.minus_guest_button:
                super.onClick(v);
                break;
            case R.id.add_hotel_save_button:
                super.saveHotelRecord(SaveOption.EDIT, v);
                break;
        }
    }

    private void initialiseComponents() {
        mIsReadOnly = true;

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);

        NestedScrollView nestedScrollView = (NestedScrollView) findViewById(R.id.nested_scroll_view);
        nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                // Floating action button is only shown in read only mode
                if (mIsReadOnly) {
                    // Swipe down
                    if (scrollY - oldScrollY > 10) {
                        fab.hide();
                    }
                    // Swipe up
                    else if (scrollY < oldScrollY) {
                        fab.show();
                    }
                }
            }
        });
    }
}