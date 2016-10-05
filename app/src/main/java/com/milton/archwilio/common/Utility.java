package com.milton.archwilio.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.milton.archwilio.R;

import java.util.Locale;

/**
 * Created by Milton on 13/02/2016.
 */
public class Utility {
    // Preference keys
    public static final String PREF_KEY_IS_SIGNED_IN_WITH_GOOGLE = "is_signed_in_with_google";
    public static final String PREF_KEY_CURRENT_TRIP_ID = "current_trip_id";

    // Information about user's credentials
    public static final String PREF_KEY_USER_ID = "pref_key_user_id";
    public static final String PREF_KEY_USER_NAME = "pref_key_user_name";
    public static final String PREF_KEY_USER_EMAIL = "pref_key_user_email";
    public static final String PREF_KEY_USER_PHOTO_URL = "pref_key_user_photo_url";

    // Texts of date formats
    public static final String DATE_FORMAT_PATTERN = "dd MMMM yyyy";
    public static final String TIME_FORMAT_PATTERN = "h:mm a";
    public static final String DATE_TIME_FORMAT_PATTERN = "E MMM dd HH:mm:ss z yyyy";
    public static final String DATE_FORMAT_PATTERN_ASIA = "yyyy年MMMMdd日";

    // Request and result codes
    public static final int SIGN_IN_REQUEST_CODE = 6000;
    public static final int SUCCESS_RESULT_CODE = 7000;
    public static final int FAIL_RESULT_CODE = 7001;

    public static Locale getLocale(Context context) {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        String selectedLanguage = sharedPreferences.getString(
                context.getResources().getString(R.string.pref_key_language), "en");
        Locale locale;
        switch (selectedLanguage) {
            case "es":
                locale = new Locale("es", "ES");
                break;
            case "it":
                locale = new Locale("it", "IT");
                break;
            case "sv":
                locale = new Locale("sv", "SE");
                break;
            case "zh":
                locale = new Locale("zh", "TW");
                break;
            case "ja":
                locale = new Locale("ja", "JP");
                break;
            default:
                locale = Locale.ENGLISH;
        }
        return locale;
    }

    /**
     * This method returns true if the string is either null or only consists of white spaces.
     *
     * @param string Input string
     * @return true if the string is either null or only consists of white spaces
     */
    public static boolean isNullOrEmpty(String string) {
        return string == null || string.trim().isEmpty();
    }

    /**
     * This method returns true if the string is either null or only consists of white spaces.
     *
     * @param charSequence Input char sequence
     * @return true if the string is either null or only consists of white spaces
     */
    public static boolean isNullOrEmpty(CharSequence charSequence) {
        return charSequence == null || isNullOrEmpty(charSequence.toString());
    }
}