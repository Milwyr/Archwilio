package com.milton.archwilio.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.milton.archwilio.R;
import com.milton.archwilio.activities.MainActivity;
import com.milton.archwilio.activities.TutorialActivity;

/**
 * This fragment is used to display for for the Viewpager of TutorialActivity.
 *
 * TutorialActivity displays three pages to introduce the app
 * when the user installs this app for the first time.
 */
public class TutorialFragment extends Fragment {
    private static final String ARG_POSITION = "Position";
    private static final String ARG_IMAGE_ID = "ImageId";

    private int position;
    private int imageId;

    public static TutorialFragment newInstance(int position, int imageId) {
        TutorialFragment fragment = new TutorialFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_POSITION, position);
        args.putInt(ARG_IMAGE_ID, imageId);
        fragment.setArguments(args);
        return fragment;
    }

    public TutorialFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.position = getArguments().getInt(ARG_POSITION);
            this.imageId = getArguments().getInt(ARG_IMAGE_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tutorial_pager, container, false);

        ImageView imageView = (ImageView) view.findViewById(R.id.pager_image_view);
        imageView.setImageResource(imageId);

        TextView textView = (TextView) view.findViewById(R.id.pager_text_view);
        textView.setText(getResources().getTextArray(R.array.tutorial_messages)[this.position]);

        // Show the button if this is the rightest page
        if (this.position == 2) {
            Button continueButton = (Button) view.findViewById(R.id.pager_continue_button);
            continueButton.setVisibility(View.VISIBLE);
            continueButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    getActivity().startActivityForResult(
                            intent, TutorialActivity.MAIN_ACTIVITY_REQUEST_CODE);
                }
            });
        }

        return view;
    }
}