package com.milton.archwilio.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.RadioGroup;

import com.milton.archwilio.R;
import com.milton.archwilio.common.DbHelper;
import com.milton.archwilio.fragments.TutorialFragment;

public class TutorialActivity extends BaseActivity {
    public static final int MAIN_ACTIVITY_REQUEST_CODE = 8001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final String ARG_IS_FIRST_TIME = "IsFirstTime";
        boolean isFirstTime = mSharedPreferences.getBoolean(ARG_IS_FIRST_TIME, true);

        if (isFirstTime) {
            mSharedPreferences.edit().putBoolean(ARG_IS_FIRST_TIME, false).apply();

            setContentView(R.layout.activity_tutorial);

            // Create a new database in background thread
            new Thread(new Runnable() {
                @Override
                public void run() {
                    DbHelper.getInstance(TutorialActivity.this);
                }
            }).run();

            // Initialise view pager
            ViewPager viewPager = (ViewPager) findViewById(R.id.tutorial_view_pager);
            viewPager.setAdapter(new ImagePagerAdapter());

            // Change checked status of the indicator
            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radio_group);
                    radioGroup.clearCheck();
                    if (position == 0) {
                        radioGroup.check(R.id.page1);
                    } else if (position == 1) {
                        radioGroup.check(R.id.page2);
                    } else if (position == 2) {
                        radioGroup.check(R.id.page3);
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        } else {
            startActivityForResult(new Intent(this, MainActivity.class), MAIN_ACTIVITY_REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MAIN_ACTIVITY_REQUEST_CODE) {
            finish();
        }
    }

    private class ImagePagerAdapter extends FragmentPagerAdapter {
        private int[] imageIds = new int[]{
                R.drawable.ic_location_white_128dp,
                R.drawable.ic_no_network_white_128dp,
                R.drawable.ic_cloud_white_128dp
        };

        public ImagePagerAdapter() {
            super(getSupportFragmentManager());
        }

        @Override
        public Fragment getItem(int position) {
            return TutorialFragment.newInstance(position, imageIds[position]);
        }

        @Override
        public int getCount() {
            return imageIds.length;
        }
    }
}