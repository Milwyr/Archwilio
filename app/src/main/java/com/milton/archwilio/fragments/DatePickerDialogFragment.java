package com.milton.archwilio.fragments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.widget.DatePicker;

import java.util.Calendar;

/**
 * Created by Milton on 20/02/2016.
 */
public class DatePickerDialogFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    private static final String ARG_CALENDAR = "calendar";

    //region Interface
    private OnDateSelectedListener mCallback;

    public interface OnDateSelectedListener {
        /**
         * This method returns the date selected on the date picker by the user
         *
         * @param year  Selected year
         * @param month Selected month
         * @param day   Selected day
         */
        void onDateSelected(int year, int month, int day);
    }
    //endregion

    /**
     * This factory method creates a new instance of DatePickerDialogFragment.
     *
     * @param calendar A calendar object with the date to display
     * @return A new instance of DatePickerDialogFragment
     */
    public static DatePickerDialogFragment newInstance(Calendar calendar) {
        DatePickerDialogFragment fragment = new DatePickerDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_CALENDAR, calendar);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallback = (OnDateSelectedListener) activity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Calendar calendar = Calendar.getInstance();
        if (getArguments() != null) {
            calendar = (Calendar) getArguments().getSerializable(ARG_CALENDAR);
        }

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DATE);
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        if (mCallback != null) {
            mCallback.onDateSelected(year, monthOfYear, dayOfMonth);
        }
    }
}