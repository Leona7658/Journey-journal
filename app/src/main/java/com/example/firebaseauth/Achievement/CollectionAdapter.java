package com.example.firebaseauth.Achievement;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class CollectionAdapter extends FragmentStateAdapter {
    public CollectionAdapter(Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new DayFragment();
            case 1:
                return new WeekFragment();
            case 2:
                return new MonthFragment();
            default:
                throw new IllegalArgumentException("Unknown position: " + position);
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }

}

