package com.milton.archwilio.fragments;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.milton.archwilio.R;

/**
 * This fragment displays the attributions for the resources used in this app.
 */
public class AboutFragment extends Fragment {
    public AboutFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AboutFragment.
     */
    public static AboutFragment newInstance() {
        return new AboutFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle(R.string.title_about);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, container, false);

        // Load the local html page that displays information about this app
        WebView webView = (WebView) view.findViewById(R.id.web_view);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String selectedLanguage = sharedPreferences.getString(getResources().getString(R.string.pref_key_language), "en");

        // Load the html file with the selected language
        String url = "file:///android_asset/about.html";
        if (!selectedLanguage.equals("en")) {
            url = "file:///android_asset/about-" + selectedLanguage + ".html";
        }
        webView.loadUrl(url);

        return view;
    }
}