package com.example.firebaseauth.Utility;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsetsController;
import android.view.WindowInsets;

import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.example.firebaseauth.Achievement.Achievement;
import com.example.firebaseauth.Library.Library;
import com.example.firebaseauth.Profile.Profile;
import com.example.firebaseauth.R;
import com.example.firebaseauth.Trip.Trip;
import com.example.firebaseauth.Trip.TripDetailFragment;
import com.example.firebaseauth.databinding.ActivityMainBinding;
import com.google.firebase.FirebaseApp;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    Fragment libraryFragment, tripFragment, achievementFragment, profileFragment, tripDetailFragment, currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        FirebaseApp.initializeApp(this);


        // Hide status and navigation bar unless wiping
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            final WindowInsetsController insetsController = getWindow().getInsetsController();
            if (insetsController != null) {
                insetsController.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                insetsController.setSystemBarsAppearance(WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS);
                insetsController.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // For devices between Android 6.0 (API level 23) to Android 10 (API level 29)
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // For devices between Android 4.4 (API level 19) to Android 5.1 (API level 22)
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        // Initialize fragments
        libraryFragment = new Library();
        tripFragment = new Trip();
        achievementFragment = new Achievement();
        profileFragment = new Profile();
        tripDetailFragment = new TripDetailFragment();

        // Start with the library fragment by default
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.frame_layout, libraryFragment);
        fragmentTransaction.add(R.id.frame_layout, tripFragment).hide(tripFragment);
        fragmentTransaction.add(R.id.frame_layout, achievementFragment).hide(achievementFragment);
        fragmentTransaction.add(R.id.frame_layout, profileFragment).hide(profileFragment);
        fragmentTransaction.commit();

        currentFragment = libraryFragment;

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.library) {
                switchFragment(libraryFragment);
            } else if (itemId == R.id.trip) {
                switchFragment(tripFragment);
            } else if (itemId == R.id.achievement) {
                switchFragment(achievementFragment);
            } else if (itemId == R.id.profile) {
                switchFragment(profileFragment);
            }
            return true;
        });
    }

    private void switchFragment(Fragment newFragment) {

        if (newFragment != currentFragment) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.hide(currentFragment);
            fragmentTransaction.show(newFragment);
            fragmentTransaction.commit();
            currentFragment = newFragment;
        }
        if (newFragment == tripDetailFragment) {
            binding.bottomNavigationView.setVisibility(View.GONE);
        } else {
            binding.bottomNavigationView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)ev.getRawX(), (int)ev.getRawY())) {
                    v.clearFocus();
                    hideKeyboard(this);
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(activity.getWindow().getDecorView().getWindowToken(), 0);
        }
    }

    public void setBottomNavigationVisibility(int visibility) {
        binding.bottomNavigationView.setVisibility(visibility);
    }

}