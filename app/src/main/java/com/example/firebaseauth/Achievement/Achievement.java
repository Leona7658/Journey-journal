package com.example.firebaseauth.Achievement;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.firebaseauth.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class Achievement extends Fragment {

    CollectionAdapter collectionAdapter;
    ViewPager2 viewPager;


    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_achievement, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        collectionAdapter = new CollectionAdapter(this);
        viewPager = view.findViewById(R.id.pager);
        viewPager.setAdapter(collectionAdapter);

        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("Day");
                            break;
                        case 1:
                            tab.setText("Week");
                            break;
                        case 2:
                            tab.setText("Month");
                            break;
                        default:
                            tab.setText("Tab " + (position + 1));
                            break;
                    }
                }
        ).attach();
    }

}