package com.example.quietframe.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.quietframe.database.MyDatabase;
import com.example.quietframe.database.dao.PhotoDao;
import com.example.quietframe.database.entity.PhotoEntity;
import com.example.quietframe.adapter.PhotosAdapter;
import com.example.quietframe.R;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements PhotosAdapter.OnItemDeleteListener {
    private ViewPager2 viewPager;
    private PhotosAdapter photosAdapter;
    private List<PhotoEntity> photos = new ArrayList<>();

    private LinearLayout layoutIndicator;
    private List<ImageView> indicators;
    private long userId;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        viewPager = view.findViewById(R.id.viewPager);
        layoutIndicator = view.findViewById(R.id.layoutIndicator);
//        viewPager = view.findViewById(R.id.viewPager);
        indicators = new ArrayList<>();
        createIndicators();
        setCurrentIndicator(0);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                setCurrentIndicator(position);
            }
        });
        if (getActivity().getIntent().hasExtra("ID")) {
            userId = getActivity().getIntent().getExtras().getLong("ID");
        }
        populatePreviousEdits();
        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    private void createIndicators() {
        layoutIndicator.removeAllViews(); // Clear existing indicators
        for (int i = 0; i < photos.size(); i++) {
            ImageView indicatorItem = new ImageView(getActivity());
            indicatorItem.setImageResource(R.drawable.circle);

            int indicatorSize = getResources().getDimensionPixelSize(R.dimen.indicator_size);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    indicatorSize,
                    indicatorSize
            );
            // Add margin to separate the indicators
            int indicatorMargin = getResources().getDimensionPixelSize(R.dimen.indicator_margin);
            params.setMargins(indicatorMargin, 0, indicatorMargin, 0);
            indicatorItem.setLayoutParams(params);
            layoutIndicator.addView(indicatorItem);
            indicators.add(indicatorItem);
        }
    }

    private void setCurrentIndicator(int position) {
        for (int i = 0; i < indicators.size(); i++) {
            indicators.get(i).setImageResource(
                    i == position ? R.drawable.full_circle : R.drawable.circle
            );
        }
    }

    private void updateIndicators(int position) {
//        indicators.remove(position);
//        createIndicators();
//        setCurrentIndicator(viewPager.getCurrentItem() % indicators.size());
//        photosAdapter.updateIndicators(indicators);
        indicators.remove(position);

        // Ensure the position is within the valid range
        if (position >= 0 && position < indicators.size()) {
            setCurrentIndicator(position);
        } else if (!indicators.isEmpty()) {
            // If the position is out of range, set the indicator to the last one
            setCurrentIndicator(indicators.size() - 1);
        } else {
            // If there are no indicators left, clear the layoutIndicator
            layoutIndicator.removeAllViews();
        }

        photosAdapter.updateIndicators(indicators);
    }

    private void populatePreviousEdits() {
        photos = new ArrayList<>();
        MyDatabase myDatabase = MyDatabase.getDatabase(getActivity());
        PhotoDao photoDao = myDatabase.photoDao();
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<PhotoEntity> photoEntities = photoDao.getAllPhotos(userId);
                for (PhotoEntity photoEntity : photoEntities) {
                    photos.add(photoEntity);
                }
                if (isAdded()) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setAdapter();
                            createIndicators();
                        }
                    });
                }
            }
        }).start();
    }

    private void setAdapter() {
        if (viewPager != null) {
            photosAdapter = new PhotosAdapter(getActivity(), photos);
            viewPager.setAdapter(photosAdapter);
//            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
//                @Override
//                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//
//                }
//
//                @Override
//                public void onPageSelected(int position) {
//
//                }
//
//                @Override
//                public void onPageScrollStateChanged(int state) {
//
//                }
//            });
        } else Log.e("VIEW PAGER", "E NULL");
    }


    @Override
    public void onItemDeleted(int position) {
// Remove the indicator at the specified position
        layoutIndicator.removeViewAt(position);

        // Create a Handler to run code on the main (UI) thread
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                updateIndicators(position);

                // Pass the updated indicators list to the adapter
                photosAdapter.updateIndicators(indicators);
            }
        });
    }
}