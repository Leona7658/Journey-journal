
package com.example.firebaseauth.Trip;

import static com.google.android.gms.location.Priority.PRIORITY_BALANCED_POWER_ACCURACY;
import static java.lang.Math.round;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ViewFlipper;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebaseauth.DatabaseModel.TripDetail;
import com.example.firebaseauth.Utility.Camera;
import com.example.firebaseauth.DatabaseModel.TripDB;
import com.example.firebaseauth.Library.CustomLatLng;
import com.example.firebaseauth.DatabaseModel.DBFactory;
import com.example.firebaseauth.Utility.ImageAdapter;
import com.example.firebaseauth.Utility.ImageFactory;
import com.example.firebaseauth.Utility.MainActivity;
import com.example.firebaseauth.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Trip extends Fragment {
    private String galleryPermissionToRequest;
    private FusedLocationProviderClient mFusedLocationClient;
    private ActivityResultLauncher<String> galleryLauncher;
    private ImageAdapter imageAdapter;
    private ViewFlipper viewFlipper;
    private MaterialButton startButton, endButton, postButton;
    private ImageButton addButton;
    private PreviewView previewView;
    private String story = "";
    private AutoCompleteTextView dropdownText;
    private TextInputEditText tagStory;
    private EditText storyTitle;
    private TextView tagData;
    private final List<Object> imageUris = new ArrayList<>();
    private GoogleMap map;

    private ActivityResultLauncher<String[]> requestPermissionLauncher;
    private List<LatLng> routeLocationList = new ArrayList<>();
    private LatLng start, end, lastKnownLatLng;
    private float distance;
    private long speed;
    private long time = 0;
    private Camera cameraObj;

    private RecyclerView recyclerView;

    public static final int YELLOW = -256;
    private DBFactory dbFactory;
    private TripDB tripDB;
    private List<String> uploadTags = new ArrayList<>();
    private String[] items;


    @SuppressLint("NotifyDataSetChanged")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Choose the correct permission for gallery
        if (Build.VERSION.SDK_INT >= 33) {
            galleryPermissionToRequest = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            galleryPermissionToRequest = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        imageUris.add(ImageAdapter.SELECT_BUTTON_PLACEHOLDER);

        // Update permission results
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    if (result.containsKey(Manifest.permission.ACCESS_FINE_LOCATION) ||
                            result.containsKey(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                        boolean isGranted = (Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_FINE_LOCATION)) ||
                                Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_COARSE_LOCATION)));
                        if (Boolean.TRUE.equals(isGranted) && map != null) {
                            handleLocationPermission(true);
                        }
                    }

                    if (result.containsKey(Manifest.permission.CAMERA)) {
                        handleCameraPermission(Objects.requireNonNull(result.get(Manifest.permission.CAMERA)));
                    }

                    if (result.containsKey(galleryPermissionToRequest)) {
                        handleGalleryPermission(Objects.requireNonNull(result.get(galleryPermissionToRequest)));
                    }
                }
        );

        // Initialize gallery launcher
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        imageUris.add(imageUris.size() - 1, uri); // Add before the last item, which is the button
                        updatePostButtonState();
                        if (imageUris.size() > 9) {
                            imageUris.remove(ImageAdapter.SELECT_BUTTON_PLACEHOLDER); // Remove the button if 9 images have been selected
                        }
                        imageAdapter.notifyDataSetChanged();
                    }
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Initialise trip fragment
        View view = inflater.inflate(R.layout.fragment_trip, container, false);
        Animation inAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in);
        Animation outAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_out);
        Animation inAnim2 = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_2);
        Animation outAnim2 = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_out_2);

        // Initialise map
        SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.trip_map);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Async map
        assert supportMapFragment != null;
        supportMapFragment.getMapAsync(googleMap -> {
            map = googleMap;
            try {
                // Customise the styling of the base map using a JSON object defined
                // in a raw resource file.
                boolean success = map.setMapStyle(
                        MapStyleOptions.loadRawResourceStyle(
                                requireActivity(), R.raw.mapstyle));
                if (!success) {
                    Log.e("MapsActivity", "Style parsing failed.");
                }
            } catch (Resources.NotFoundException e) {
                Log.e("MapsActivity", "Can't find style. Error: ", e);
            }

            map.getUiSettings().setZoomGesturesEnabled(true);
            requestPermissionLauncher.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION});
        });

        // When click the button
        startButton = view.findViewById(R.id.trip_start_button);
        endButton = view.findViewById(R.id.trip_end_button);
        addButton = view.findViewById(R.id.trip_add_button);
        postButton = view.findViewById(R.id.trip_post_button);

        // camera buttons
        ImageButton bTakePicture = view.findViewById(R.id.trip_capture_button);
        previewView = view.findViewById(R.id.camera_preview);
        ImageButton cameraClose = view.findViewById(R.id.trip_camera_close);
        ImageButton cameraFlashOff = view.findViewById(R.id.camera_flash_off_button);
        ImageButton cameraFlashOn = view.findViewById(R.id.camera_flash_on_button);
        ImageButton cameraFlipper = view.findViewById(R.id.camera_flip_button);

        ImageButton closeButton = view.findViewById(R.id.trip_close_button);
        viewFlipper = view.findViewById(R.id.trip_view_flipper);
        // To show the map by default
        viewFlipper.setDisplayedChild(0);

        // To update text of speed
        TextView speedData = view.findViewById(R.id.trip_speed_data);
        Chronometer timeData = view.findViewById(R.id.trip_time_data);
        TextView distanceData = view.findViewById(R.id.trip_distance_data);
        tagData = view.findViewById(R.id.trip_tag_data);
        dropdownText = view.findViewById(R.id.trip_tag_selection);
        tagStory = view.findViewById(R.id.trip_tag_story);
        storyTitle = view.findViewById(R.id.trip_title);

        startButton.setOnClickListener(v -> {
            map.clear();
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                // Hide startButton and show endButton when startButton is clicked
                startButton.setVisibility(View.GONE);
                endButton.setVisibility(View.VISIBLE);
                addButton.setVisibility(View.VISIBLE);

                // Start listen for upcoming location change, clear all data
                routeLocationList.clear();
                uploadTags.clear();
                setUpLocationListener();
                distance = 0;
                speed = 0;
                speedData.setText("0");
                tagData.setText("0");
                distanceData.setText("0");

                // init database factory
                tripDB = new TripDB(getEmail());
                dbFactory = new DBFactory(tripDB, requireActivity(), getEmail());

                dbFactory.initTripDB();
                // prepare query for getting Tag count


                timeData.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
                    @Override
                    public void onChronometerTick(Chronometer cArg) {
                        long time = SystemClock.elapsedRealtime() - cArg.getBase();
                        int h = (int) (time / 3600000);
                        int m = (int) (time - h * 3600000) / 60000;
                        int s = (int) (time - h * 3600000 - m * 60000) / 1000;

                        cArg.setText(String.format("%02d:%02d:%02d", h, m, s));
                    }
                });
                timeData.setBase(SystemClock.elapsedRealtime());
                timeData.start();
            } else {
                handleLocationPermission(false);
            }
        });
        endButton.setOnClickListener(v -> {
            // Hide endButton and show startButton when endButton is clicked
            endButton.setVisibility(View.GONE);
            addButton.setVisibility(View.GONE);
            startButton.setVisibility(View.VISIBLE);

            // draw route
            addLocationToRoute(routeLocationList);

            //stop timer
            timeData.stop();
            time = SystemClock.elapsedRealtime() - timeData.getBase();
            // show speed
            speed = round(distance / (time / (3.6 * Math.pow(10, 6))));
            speedData.setText(Long.toString(speed));
            distanceData.setText( String.format("%.2f", distance));

            // Convert LatLng list to CustomLatLng list
            List<CustomLatLng> customLocationList = new ArrayList<>();
            for (LatLng latLng : routeLocationList) {
                customLocationList.add(CustomLatLng.fromLatLng(latLng));
            }

            // Update route info to db using customLocationList
            tripDB.setTripDB(distance, (int) speed, (int) time, customLocationList);
            dbFactory.setNewTripDB(tripDB);
            dbFactory.updateTripDB();


            // stop listening to location change
            mFusedLocationClient.removeLocationUpdates(locationCallback);

        });
        addButton.setOnClickListener(v -> {
            // To show the tag page
            getTagsFromDB();

            viewFlipper.setInAnimation(inAnim);
            viewFlipper.setOutAnimation(outAnim);
            viewFlipper.setDisplayedChild(1);
        });
        closeButton.setOnClickListener(v -> {
            boolean isEdited = isTagEdited();
            if (isEdited) {
                @SuppressLint("NotifyDataSetChanged") AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity())
                        .setMessage("Do you want to save this edit?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            // Store to database
                            viewFlipper.setInAnimation(inAnim2);
                            viewFlipper.setOutAnimation(outAnim2);
                            viewFlipper.setDisplayedChild(0); // To show the map page
                        })
                        .setNegativeButton("No", (dialog, which) -> {
                            viewFlipper.setInAnimation(inAnim2);
                            viewFlipper.setOutAnimation(outAnim2);
                            viewFlipper.setDisplayedChild(0); // To show the map page
                            // Clear post input

                            clearEdits();
                        });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                // Once the AlertDialog is created, retrieve the buttons and set their color
                Button positiveButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                Button negativeButton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                positiveButton.setTextColor(ContextCompat.getColor(requireActivity(), R.color.orange));
                negativeButton.setTextColor(ContextCompat.getColor(requireActivity(), R.color.grey));
            } else {
                viewFlipper.setInAnimation(inAnim2);
                viewFlipper.setOutAnimation(outAnim2);
                viewFlipper.setDisplayedChild(0); // To show the map page
                // Clear post input
                clearEdits();
            }
        });
        postButton.setOnClickListener(v -> {
            // display newest tag
            int tagColor = getColorForTag(dropdownText.getText().toString());

            viewFlipper.setInAnimation(inAnim2);
            viewFlipper.setOutAnimation(outAnim2);
            // prepare tag count
            uploadTags.add(dropdownText.getText().toString());

            tagData.setText(String.valueOf(uploadTags.size()));
            viewFlipper.setDisplayedChild(0);
            // submit post info to db factory
            addToDB();

            // clear post inputs
            clearEdits();

            if (map != null && lastKnownLatLng != null) {
                addTagIconToMap(lastKnownLatLng, tagColor);
            }
        });
        // Camera page buttons actions
        bTakePicture.setOnClickListener(v -> cameraObj.capturePhoto());
        cameraClose.setOnClickListener(v -> {
            viewFlipper.setDisplayedChild(1);
            ((MainActivity) requireActivity()).setBottomNavigationVisibility(View.VISIBLE);
            updatePostButtonState();
        });
        cameraFlipper.setOnClickListener(v -> cameraObj.flipCamera());
        cameraFlashOff.setOnClickListener(v -> {
            cameraFlashOff.setVisibility(View.GONE);
            cameraFlashOn.setVisibility(View.VISIBLE);
            cameraObj.turnOnFlashMode();
        });
        cameraFlashOn.setOnClickListener(v -> {
            cameraFlashOn.setVisibility(View.GONE);
            cameraFlashOff.setVisibility(View.VISIBLE);
            cameraObj.turnOffFlashMode();
        });


        // When focus on the story area
        tagStory.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                tagStory.setHint("");
            } else {
                tagStory.setHint("Write your story...");
            }
        });

        // Update the content in the story area in real time
        tagStory.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                story = s.toString().trim();
                updatePostButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Display from gallery
        imageAdapter = new ImageAdapter(requireActivity(), imageUris);
        recyclerView = view.findViewById(R.id.trip_tag_recycler);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        recyclerView.setAdapter(imageAdapter);

        imageAdapter.setOnOptionSelectedListener(which -> {
            // Camera option clicked
            if (which == 0) {
                // Request camera permissions if not granted
                requestPermissionLauncher.launch(new String[]{Manifest.permission.CAMERA});
                // Gallery option clicked
            } else if (which == 1) {
                // Request gallery permissions if not granted
                requestPermissionLauncher.launch(new String[]{galleryPermissionToRequest});
            }
        });

        // Delete selected images
        imageAdapter.setOnItemLongClickListener((itemView, position) -> {
            Object item = imageUris.get(position);
            if (!(item == ImageAdapter.SELECT_BUTTON_PLACEHOLDER)) {
                @SuppressLint("NotifyDataSetChanged") AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity())
                        .setTitle("Delete Image")
                        .setMessage("Are you sure you want to delete this image?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            imageUris.remove(position);
                            updatePostButtonState();
                            imageAdapter.notifyDataSetChanged();

                            // If you removed an image and the button isn't present, add it back
                            if (!imageUris.contains(ImageAdapter.SELECT_BUTTON_PLACEHOLDER) && imageUris.size() < 9) {
                                imageUris.add(ImageAdapter.SELECT_BUTTON_PLACEHOLDER);
                                imageAdapter.notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton("No", null);

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                // Once the AlertDialog is created, retrieve the buttons and set their color
                Button positiveButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                Button negativeButton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                positiveButton.setTextColor(ContextCompat.getColor(requireActivity(), R.color.grey));
                negativeButton.setTextColor(ContextCompat.getColor(requireActivity(), R.color.orange));
            }
            return true;
        });
        getTagsFromDB();
        // Dropdown button for selecting tags
        items = getResources().getStringArray(R.array.dropdown_items);
        TagList adapter = new TagList(getActivity(), items);
        dropdownText.setAdapter(adapter);

        return view;
    }

    // submit post info to db factory
    private void addToDB() {
        // text data to cloud db
        CustomLatLng lastLocation = CustomLatLng.fromLatLng(routeLocationList.get(routeLocationList.size() - 1));

        TripDetail detail = new TripDetail(
                getEmail(),
                storyTitle.getText().toString(),
                tagStory.getText().toString(),
                dropdownText.getText().toString(),
                CustomLatLng.fromLatLng(routeLocationList.get(routeLocationList.size() - 1)),
                dbFactory.getTripID()
        );
        dbFactory.setDetail(detail);
        dbFactory.addDetailDB();

        // update image uris to cloud db
        ImageFactory imageFactory = new ImageFactory(requireActivity(),
                imageUris.subList(0, imageUris.size() - 1), dbFactory);
        imageFactory.uploadImage();

    }

    // clear edit field
    private void clearEdits() {
        dropdownText.setText("");
        tagStory.setText("");
        storyTitle.setText("");
        imageUris.clear();
        imageUris.add(ImageAdapter.SELECT_BUTTON_PLACEHOLDER);
        imageAdapter.notifyDataSetChanged();
    }

    private int getColorForTag(String tag) {
        if (tag.equals(items[0])) {
            return R.color.tag_red;
        } else if (tag.equals(items[1])) {
            return R.color.tag_green;
        } else if (tag.equals(items[2])) {
            return R.color.tag_blue;
        } else if (tag.equals(items[3])) {
            return R.color.tag_yellow;
        } else if (tag.equals(items[4])) {
            return R.color.tag_pink;
        } else if (tag.equals(items[5])) {
            return R.color.tag_brown;
        } else {
            return R.color.grey;
        }
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        int width = drawable.getIntrinsicWidth();
        width = width > 0 ? width : 1;
        int height = drawable.getIntrinsicHeight();
        height = height > 0 ? height : 1;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    private void addTagIconToMap(LatLng latLng, int tagColorResId) {
        if (map == null || latLng == null) return;

        // Convert the resource ID to an actual color value
        int actualTagColor = ContextCompat.getColor(requireContext(), tagColorResId);

        Drawable drawable = ContextCompat.getDrawable(requireContext(), R.drawable.pin_icon);
        if (drawable != null) {
            drawable.setColorFilter(new PorterDuffColorFilter(actualTagColor, PorterDuff.Mode.SRC_IN));
        }
        Bitmap tagIconBitmap = drawableToBitmap(drawable);

        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.fromBitmap(tagIconBitmap));

        map.addMarker(markerOptions);

        if (tagIconBitmap != null && !tagIconBitmap.isRecycled()) {
            tagIconBitmap.recycle();
        }
    }

    private void handleLocationPermission(Boolean isGranted) {
        if (isGranted) {
            initializeMapWithLocation(map);
        } else {
            explainPermissionNeed("Location Permission Needed",
                    "This app requires location permission to show your current position on the map."
            );
        }
    }

    private void handleCameraPermission(Boolean isGranted) {
        if (isGranted) {
            // switch to camera layout page
            viewFlipper.setDisplayedChild(2);
            ((MainActivity) requireActivity()).setBottomNavigationVisibility(View.GONE);
            cameraObj = new Camera(requireActivity(), previewView, recyclerView, imageUris, null);
        } else {
            explainPermissionNeed("Camera Permission Needed",
                    "This app requires camera permission to take pictures."
            );
        }
    }

    private void handleGalleryPermission(Boolean isGranted) {
        if (isGranted) {
            galleryLauncher.launch("image/*");
        } else {
            explainPermissionNeed("Gallery Permission Needed",
                    "This app requires gallery permission to select images."
            );
        }
    }

    // Further request permissions
    private void explainPermissionNeed(String title, String message) {
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Go to Settings", (dialogInterface, which) -> {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", requireActivity().getPackageName(), null);
                    intent.setData(uri);
                    settingsActivityResultLauncher.launch(intent);
                })
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(d -> {
            Button positiveButton = ((AlertDialog) d).getButton(DialogInterface.BUTTON_POSITIVE);
            Button negativeButton = ((AlertDialog) d).getButton(DialogInterface.BUTTON_NEGATIVE);
            positiveButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.orange));
            negativeButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey));
        });

        dialog.show();
    }

    ActivityResultLauncher<Intent> settingsActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> showRestartDialog());

    private void showRestartDialog() {
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Restart App")
                .setMessage("Do you want to restart the app to validate the new permissions?")
                .setPositiveButton("Restart", (dialogInterface, which) -> {
                    // Restart the app logic
                    Intent intent = requireActivity().getPackageManager().getLaunchIntentForPackage(requireActivity().getPackageName());
                    if (intent != null) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        requireActivity().finish();
                    }
                })
                .setNegativeButton("Later", null)
                .create();

        dialog.show();
    }

    private void updatePostButtonState() {
        boolean isEdited = isTagEdited();
        if (isEdited) {
            postButton.setBackground(Objects.requireNonNull(ContextCompat.getDrawable(requireActivity(), R.drawable.orange_round_button)));
        } else {
            postButton.setBackground(Objects.requireNonNull(ContextCompat.getDrawable(requireActivity(), R.drawable.grey_round_button)));
        }
    }

    private boolean isTagEdited() {
        boolean hasStory = !story.trim().isEmpty();
        boolean hasImages = !(imageUris.size() == 1 && imageUris.get(0) == ImageAdapter.SELECT_BUTTON_PLACEHOLDER);
        return hasStory || hasImages;
    }

    private void initializeMapWithLocation(@NonNull GoogleMap googleMap) {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            try {
                googleMap.setMyLocationEnabled(true);
                googleMap.getUiSettings().setMyLocationButtonEnabled(false);

                // observe changes in user location
                LocationRequest locationRequest = new LocationRequest.Builder(PRIORITY_BALANCED_POWER_ACCURACY,
                        2500).build();

                mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    // callback when new long lat is detected, add new locations to location list
    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {

            if (locationResult == null) {
                return;
            }
            super.onLocationResult(locationResult);
            if (locationResult != null) {

                for (Location location : locationResult.getLocations()) {
                    LatLng newLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    lastKnownLatLng = newLatLng;
                    routeLocationList.add(newLatLng);
                    int length = routeLocationList.size();
                    if (length >=2) {

                        distance += distanceBetweenLocations(routeLocationList.get(length - 2),
                                routeLocationList.get(length - 1))/1000;
                    }
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(newLatLng, 15));

                }
            }

        }
    };

    /**
     * listen to long lat changes on map
     */
    private void setUpLocationListener() {
        // observe changes in user location
        LocationRequest locationRequest = new LocationRequest.Builder(PRIORITY_BALANCED_POWER_ACCURACY,
                2500).build();
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(requireActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    101);
            return;
        }
        mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }


    /**
     * draw routes on map
     */
    public void addLocationToRoute(List<LatLng> locations) {
        map.clear();
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions = polylineOptions.color(YELLOW);
        polylineOptions = polylineOptions.width(30);
        polylineOptions = polylineOptions.clickable(true);

        polylineOptions = polylineOptions.addAll(locations);

        Polyline polyLine = map.addPolyline(polylineOptions);
        // record route info
        this.start = locations.get(0);
        this.end = locations.get(locations.size()-1);
    }

    /**
     * find distance of a route
     */

    public double distanceBetweenLocations(LatLng start, LatLng end) {
        return SphericalUtil.computeDistanceBetween(start, end);
    }

    private void getTagsFromDB() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference tagRef = db.collection("Tags").document(getEmail());
        tagRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        List<String> result = (List<String>) document.getData().get("tagList");
                        //dropDownTags = new ArrayList<>(result);
                        items = result.toArray(new String[result.size()]);
                        TagList adapter = new TagList(getActivity(), items);
                        dropdownText.setAdapter(adapter);

                    }
                }
            }
        });


    }

    public static String getEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user.getEmail();
    }


    public List<LatLng> getRouteLocationList() {
        return routeLocationList;
    }

    public long getSpeed() {
        return speed;
    }

    public long getTime() {
        return time;
    }

    public float getDistance() {
        return distance;
    }
}


