package com.milton.archwilio.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MenuItem;

import com.milton.archwilio.R;
import com.milton.archwilio.common.DbHelper;
import com.milton.archwilio.common.Utility;
import com.milton.archwilio.models.Trip;

/**
 * Created by Milton on 16/04/2016.
 */
public abstract class BaseItineraryActivity extends BaseActivity {
    protected long mCurrentTripId;
    protected Trip mCurrentTrip;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCurrentTripId = mSharedPreferences.getLong(Utility.PREF_KEY_CURRENT_TRIP_ID, -1);

        if (mCurrentTripId == -1) {
            showErrorDialog(R.string.error_current_trip_not_found, true);
        } else {
            mCurrentTrip = DbHelper.getInstance(this).getTrip(mCurrentTripId);
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
                            dialog.dismiss();
                            finish();
                        }
                    }).create().show();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}