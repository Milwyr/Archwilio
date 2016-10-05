package com.milton.archwilio.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.milton.archwilio.R;

import java.util.Locale;

/**
 * Created by Milton on 19/04/2016.
 */
public abstract class BaseActivity extends AppCompatActivity {
    protected SharedPreferences mSharedPreferences;

    protected enum SaveOption {ADD, EDIT}

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        setLocale();
    }

    // This method returns the currency symbol based on user's choice.
    protected String getCurrencySymbol() {
        String currencyValue = mSharedPreferences.getString(getResources().getString(R.string.pref_key_currency),
                getResources().getStringArray(R.array.pref_currency_list_values)[0]);

        switch (currencyValue) {
            case "GBP":
                return "£";
            case "EUR":
                return "€";
            case "USD":
                return "$";
            case "BDT":
                return "৳";
            case "CZK":
                return "Kč";
            case "CNY":
                return "¥";
            case "GEL":
                return "ლ";
            case "INR":
                return "₹";
            case "ILS":
                return "₪";
            case "UAH":
                return "₴";
            default:
                return "£";
        }
    }

    /**
     * This method creates an error dialog to show the error. The dialog also includes a close button.
     *
     * @param resId          Resource id of the error message
     * @param finishActivity True to call the finish() method when the dialog is closed or dismissed
     */
    protected void showErrorDialog(int resId, final boolean finishActivity) {
        new AlertDialog.Builder(this).setMessage(resId)
                .setNegativeButton(R.string.dialog_close, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (finishActivity) {
                            dialog.dismiss();
                            finish();
                        }
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        if (finishActivity) {
                            dialog.dismiss();
                            finish();
                        }
                    }
                }).create().show();
    }

    private void setLocale() {
        String selectedLanguage = mSharedPreferences.getString(getResources().getString(R.string.pref_key_language), "");
        Configuration configuration = getBaseContext().getResources().getConfiguration();

        if (!selectedLanguage.isEmpty() && !selectedLanguage.equals(configuration.locale.getLanguage())) {
            configuration.setLocale(new Locale(selectedLanguage));
            getBaseContext().getResources().updateConfiguration(
                    configuration, getBaseContext().getResources().getDisplayMetrics());
        }
    }
}