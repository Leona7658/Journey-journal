package com.example.firebaseauth.Library;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.firebaseauth.R;
import com.example.firebaseauth.Trip.TripListFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Library#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Library extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public Library() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Library.
     */
    public static Library newInstance(String param1, String param2) {
        Library fragment = new Library();
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

    private class LibraryPagerAdapter extends FragmentStateAdapter {

        public LibraryPagerAdapter(Fragment fragment) {
            super(fragment);
        }

        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new GlobalMapFragment();
                case 1:
                    return new TripListFragment();
                default:
                    return null;
            }
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library, container, false);

        ViewPager2 viewPager2 = view.findViewById(R.id.viewPager2);
        // disable the functionalities of swiping between two tabs
        viewPager2.setUserInputEnabled(false);
        TabLayout tabLayout = view.findViewById(R.id.tabs);

        viewPager2.setAdapter(new LibraryPagerAdapter(this));

        // Link the TabLayout with the ViewPager2
        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager2,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("Map");
                            break;
                        case 1:
                            tab.setText("Trip");
                            break;
                    }
                }
        );
        tabLayoutMediator.attach();

        return view;
    }
}