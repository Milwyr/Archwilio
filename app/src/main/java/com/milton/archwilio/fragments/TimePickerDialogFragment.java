package com.milton.archwilio.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.TimePicker;

import java.util.Calendar;

/**
 * Created by Milton on 25/03/2016.
 */
public class TimePickerDialogFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

    private static final String ARG_CALENDAR = "calendar";

    //region Interface
    private OnTimeSelectedListener mCallback;

    public interface OnTimeSelectedListener {
        /**
         * This method returns the date selected on the date picker by the user
         *
         * @param hourOfDay  Hour of the day
         * @param minute Minute of the hour
         */
        void onTimeSelected(int hourOfDay, int minute);
    }
    //endregion

    /**
     * This factory method creates a new instance of TimePickerDialogFragment.
     *
     * @param calendar A calendar object with the time to display
     * @return A new instance of TimePickerDialogFragment
     */
    public static TimePickerDialogFragment newInstance(Calendar calendar) {
        TimePickerDialogFragment fragment = new TimePickerDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_CALENDAR, calendar);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallback = (OnTimeSelectedListener) activity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Calendar calendar = Calendar.getInstance();
        if (getArguments() != null) {
            calendar = (Calendar) getArguments().getSerializable(ARG_CALENDAR);
        }

        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        return new TimePickerDialog(getActivity(), this, hourOfDay, minute, true);
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        if (mCallback != null) {
            mCallback.onTimeSelected(hourOfDay, minute);
        }
    }
}
