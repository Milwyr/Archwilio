package com.milton.archwilio.fragments;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.milton.archwilio.R;
import com.milton.archwilio.adapters.TripRecycleViewAdapter;
import com.milton.archwilio.common.DbHelper;
import com.milton.archwilio.models.Trip;

import java.util.Collections;
import java.util.List;

/**
 * This class is the default fragment of this mobile application.
 */
@TargetApi(Build.VERSION_CODES.M)
public class CardViewFragment extends Fragment implements TripRecycleViewAdapter.Callback, View.OnScrollChangeListener {
    private static final String ARG_TITLE_ID = "title_id";
    private static final String ARG_IS_DELETED = "is_deleted";

    private RecyclerView mRecyclerView;
    private TripRecycleViewAdapter mAdapter;
    private FloatingActionButton mFab;

    public CardViewFragment() {
        // Required empty public constructor
    }

    /**
     * This factory method creates a new instance of this fragment.
     *
     * @param titleId   Resource id of the title of the action bar
     * @param isDeleted True if the trip is moved to trash bin
     * @return A new instance of fragment CardViewFragment.
     */
    public static CardViewFragment newInstance(int titleId, boolean isDeleted) {
        CardViewFragment fragment = new CardViewFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TITLE_ID, titleId);
        args.putBoolean(ARG_IS_DELETED, isDeleted);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            int actionBarTitleId = getArguments().getInt(ARG_TITLE_ID);
            getActivity().setTitle(actionBarTitleId);

            boolean isDeleted = getArguments().getBoolean(ARG_IS_DELETED);

            List<Trip> trips = DbHelper.getInstance(getActivity()).getTrips(isDeleted);
            Collections.sort(trips, Collections.reverseOrder(new Trip.DateComparator()));

            mAdapter = new TripRecycleViewAdapter(getActivity(), isDeleted, trips, this);
            mFab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_card_view, container, false);

        mRecyclerView = (RecyclerView) v.findViewById(R.id.fragment_recycler_view);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Improve performance as long as changes in content
        // do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mRecyclerView.setOnScrollChangeListener(this);
        } else {
            mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    // Swipe down
                    if (dy > 5) {
                        mFab.hide();
                    }
                    // Swipe up
                    else if (dy < -5) {
                        mFab.show();
                    }
                }
            });
        }

        return v;
    }

    @Override
    public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        // Swipe down
        if (scrollY - oldScrollY > 10) {
            mFab.hide();
        }
        // Swipe up
        else if (scrollY < oldScrollY) {
            mFab.show();
        }
    }

    /**
     * This method adds a trip to the list of trips, and the list is sorted in descending order.
     *
     * @param trip A new trip
     */
    public void add(Trip trip) {
        mAdapter.add(trip);
        mRecyclerView.setAdapter(mAdapter);
    }

    /**
     * This method updates the given trip found from the list of trips.
     *
     * @param updatedTrip The updated trip
     */
    public void update(Trip updatedTrip) {
        mAdapter.update(updatedTrip);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onTripMovedToTrash(long deletedTripId) {
        removeTrip(deletedTripId, R.string.moved_to_trash);
    }

    @Override
    public void onTripDeletedPermanently(long deletedTripId) {
        removeTrip(deletedTripId, R.string.permanently_deleted);
    }

    @Override
    public void onTripRestored(long restoredTripId) {
        removeTrip(restoredTripId, R.string.restored);
    }

    private void removeTrip(long tripId, int resId) {
        boolean isSuccess = mAdapter.remove(tripId);

        if (isSuccess) {
            mRecyclerView.setAdapter(mAdapter);
            Snackbar.make(mFab, resId, Snackbar.LENGTH_SHORT).show();
        } else {
            Snackbar.make(mFab, R.string.error_general, Snackbar.LENGTH_SHORT).show();
        }
    }
}