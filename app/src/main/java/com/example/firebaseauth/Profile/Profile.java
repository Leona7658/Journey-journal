package com.example.firebaseauth.Profile;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;
import com.example.firebaseauth.Utility.Camera;
import com.example.firebaseauth.DatabaseModel.AvatarDB;
import com.example.firebaseauth.DatabaseModel.DBFactory;
import com.example.firebaseauth.DatabaseModel.TagDB;
import com.example.firebaseauth.Utility.ImageFactory;
import com.example.firebaseauth.Utility.MainActivity;
import com.example.firebaseauth.R;
import com.example.firebaseauth.login.LoginActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class Profile extends Fragment {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ViewFlipper viewFlipper;
    private ActivityResultLauncher<String[]> requestPermissionLauncher;
    private String galleryPermissionToRequest;
    private ActivityResultLauncher<String> galleryLauncher;
    private CircleImageView avatarButton;
    private Camera cameraObj;
    private PreviewView previewView;
    private FirebaseAuth mAuth;
    private EditText userNameEdit;
    private FragmentActivity context;
    private TextView userNameTextView;
    private String userNameStr;
//    private ProgressBar progressBar;
    private FirebaseStorage storage = FirebaseStorage.getInstance();

    private String downloadURL;

    private List<String> tagList = new ArrayList<>();



    String oldName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();

        // Choose the correct permission for gallery
        if (Build.VERSION.SDK_INT >= 33) {
            galleryPermissionToRequest = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            galleryPermissionToRequest = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        // Activity result launcher for permissions
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    if (result.containsKey(Manifest.permission.CAMERA)) {
                        handleCameraPermission(Objects.requireNonNull(result.get(Manifest.permission.CAMERA)));
                    }
                    if (result.containsKey(galleryPermissionToRequest)) {
                        handleGalleryPermission(Objects.requireNonNull(result.get(galleryPermissionToRequest)));
                    }
                }
        );

        // Activity result launcher for gallery
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        // Handle the URI, e.g., load it into your ImageView
                        Intent cropIntent = CropImage.activity(uri)
                                .setGuidelines(CropImageView.Guidelines.ON)
                                .setCropShape(CropImageView.CropShape.OVAL)
                                .setFixAspectRatio(true)
                                .setAspectRatio(1, 1)
                                .getIntent(requireContext());
                        cropActivityResultLauncher.launch(cropIntent);
                    }
                }
        );

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for profile fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        mAuth = FirebaseAuth.getInstance();
        Animation inAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in);
        Animation outAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_out);
        Animation inAnim2 = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_2);
        Animation outAnim2 = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_out_2);

        // When click the button
        avatarButton = view.findViewById(R.id.profile_avatar);
        AutoCompleteTextView usernameButton = view.findViewById(R.id.profile_settings_username_button);
        AutoCompleteTextView tagsButton = view.findViewById(R.id.profile_settings_tags_button);
        AutoCompleteTextView lightModeButton = view.findViewById(R.id.profile_settings_light_mode_button);
        AutoCompleteTextView passwordButton = view.findViewById(R.id.profile_security_password_button);
        AutoCompleteTextView logoutButton = view.findViewById(R.id.profile_logout_button);

        ImageButton usernameCloseButton = view.findViewById(R.id.profile_username_left_arrow);
        ImageButton tagsCloseButton = view.findViewById(R.id.profile_tags_left_arrow);
        ImageButton lightModeCloseButton = view.findViewById(R.id.profile_light_mode_left_arrow);
        ImageButton passwordCloseButton = view.findViewById(R.id.profile_change_password_left_arrow);
        userNameTextView = view.findViewById(R.id.profile_name);

        viewFlipper = view.findViewById(R.id.profile_view_flipper);
        // To show the main profile page by default
        viewFlipper.setDisplayedChild(0);
        logoutButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(getActivity(), "Log out successful", Toast.LENGTH_SHORT).show();

            }

        });
        avatarButton.setOnClickListener(v -> {
            final Dialog dialog = new Dialog(requireContext(), R.style.CustomAlertDialogStyle);
            dialog.setContentView(R.layout.image_source_dialog);

            Button cameraButton = dialog.findViewById(R.id.image_source_camera_button);
            Button galleryButton = dialog.findViewById(R.id.image_source_gallery_button);
            Button cancelButton = dialog.findViewById(R.id.image_source_cancel_button);

            cameraButton.setOnClickListener(vw -> {
                requestPermissionLauncher.launch(new String[]{Manifest.permission.CAMERA});
            });

            galleryButton.setOnClickListener(vw -> {
                requestPermissionLauncher.launch(new String[]{galleryPermissionToRequest});
                dialog.dismiss();
            });

            cancelButton.setOnClickListener(vw -> dialog.dismiss());

            Window window = dialog.getWindow();
            if (window != null) {
                WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                layoutParams.copyFrom(window.getAttributes());
                layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
                layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
                layoutParams.gravity = Gravity.BOTTOM;
                window.setAttributes(layoutParams);
                window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            }

            // Apply the bottom sheet animation for dialog
            dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
            dialog.show();

        });

        // camera buttons
        ImageButton bTakePicture = view.findViewById(R.id.profile_capture_button);
        previewView = view.findViewById(R.id.profile_camera_preview);
        ImageButton cameraClose = view.findViewById(R.id.profile_camera_close);
        ImageButton cameraFlashOff = view.findViewById(R.id.profile_camera_flash_off_button);
        ImageButton cameraFlashOn = view.findViewById(R.id.profile_camera_flash_on_button);
        ImageButton cameraFlipper = view.findViewById(R.id.profile_camera_flip_button);

        bTakePicture.setOnClickListener(v -> cameraObj.capturePhoto());
        cameraClose.setOnClickListener(v -> {
            viewFlipper.setDisplayedChild(0);
            ((MainActivity) requireActivity()).setBottomNavigationVisibility(View.VISIBLE);
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

        // username
        initFireBase();
        usernameButton.setOnClickListener(v -> {
            viewFlipper.setInAnimation(inAnim);
            viewFlipper.setOutAnimation(outAnim);
            viewFlipper.setDisplayedChild(1);
        });
        Button userNameOk = view.findViewById(R.id.profile_username_ok_arrow);
        userNameEdit = view.findViewById(R.id.profile_username_text);
        FirebaseUser user = mAuth.getCurrentUser();
        userNameOk.setOnClickListener(view1 -> {

            UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                    .setDisplayName(userNameEdit.getText().toString())
                    .build();

            assert user != null;
            user.updateProfile(profileUpdate)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(getActivity(), "userName updated success！", Toast.LENGTH_SHORT).show();
                            viewFlipper.setDisplayedChild(0);
                            userNameTextView.setText(userNameEdit.getText().toString());
                        } else {
                            Toast.makeText(getActivity(), "userName updated fail!" +
                                    Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
        usernameCloseButton.setOnClickListener(v -> {
            viewFlipper.setInAnimation(inAnim2);
            viewFlipper.setOutAnimation(outAnim2);
            viewFlipper.setDisplayedChild(0);
            userNameEdit.setText(userNameStr);
        });
        userNameEdit.setText(userNameStr);

        // tag
        Button userTagsOk = view.findViewById(R.id.profile_tags_ok_arrow);
        EditText tags_tag1 = view.findViewById(R.id.profile_tags_tag1);
        EditText tags_tag2 = view.findViewById(R.id.profile_tags_tag2);
        EditText tags_tag3 = view.findViewById(R.id.profile_tags_tag3);
        EditText tags_tag4 = view.findViewById(R.id.profile_tags_tag4);
        EditText tags_tag5 = view.findViewById(R.id.profile_tags_tag5);
        EditText tags_tag6 = view.findViewById(R.id.profile_tags_tag6);

        // get current tags from DB
        getTagsFromDB();

        tagsButton.setOnClickListener(v -> {
            viewFlipper.setInAnimation(inAnim);
            viewFlipper.setOutAnimation(outAnim);
            // init tag if db has record
            if (tagList.size()>1) {
                tags_tag1.setText(tagList.get(0));
                tags_tag2.setText(tagList.get(1));
                tags_tag3.setText(tagList.get(2));
                tags_tag4.setText(tagList.get(3));
                tags_tag5.setText(tagList.get(4));
                tags_tag6.setText(tagList.get(5));
            }
            viewFlipper.setDisplayedChild(2);
        });

        userTagsOk.setOnClickListener(v -> {
            // add new tags to database

            tagList = Arrays.asList(new String[]{
                    tags_tag1.getText().toString(),
                    tags_tag2.getText().toString(),
                    tags_tag3.getText().toString(),
                    tags_tag4.getText().toString(),
                    tags_tag5.getText().toString(),
                    tags_tag6.getText().toString()
            });
            TagDB tagDB = new TagDB(getEmail(), tagList);
            DBFactory dbFactory = new DBFactory(tagDB, getEmail());
            dbFactory.addTags();

            Toast.makeText(getActivity(), "tag update success!", Toast.LENGTH_SHORT).show();
            viewFlipper.setDisplayedChild(0);
        });
        tagsCloseButton.setOnClickListener(v -> {
            viewFlipper.setInAnimation(inAnim2);
            viewFlipper.setOutAnimation(outAnim2);
            viewFlipper.setDisplayedChild(0);

        });

        // light mode
        lightModeButton.setOnClickListener(v -> {
            viewFlipper.setInAnimation(inAnim);
            viewFlipper.setOutAnimation(outAnim);
            viewFlipper.setDisplayedChild(3);
        });
        Button lightModeOk = view.findViewById(R.id.profile_light_mode_ok_arrow);
        lightModeOk.setVisibility(View.GONE);
        SeekBar brightnessSeekBar = view.findViewById(R.id.brightnessSeekBar);
        setAppBrightness(127);
        brightnessSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setAppBrightness(progress);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Do nothing here
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Do nothing here
            }
        });
        lightModeCloseButton.setOnClickListener(v -> {
            viewFlipper.setInAnimation(inAnim2);
            viewFlipper.setOutAnimation(outAnim2);
            viewFlipper.setDisplayedChild(0);
        });

        // password
        passwordButton.setOnClickListener(v -> {
            viewFlipper.setInAnimation(inAnim);
            viewFlipper.setOutAnimation(outAnim);
            viewFlipper.setDisplayedChild(4);
        });
        Button userPasswordOk = view.findViewById(R.id.profile_password_ok_arrow);
        EditText passwordOld = view.findViewById(R.id.current_password_text);
        EditText passwordNew = view.findViewById(R.id.new_password_text);
        EditText passwordNewConfirm = view.findViewById(R.id.confirm_new_password_text);
        userPasswordOk.setOnClickListener(view12 -> {
            String oldPassword = passwordOld.getText().toString();
            String newPassword = passwordNew.getText().toString();
            String confirmPassword = passwordNewConfirm.getText().toString();

            if (user != null && newPassword.equals(confirmPassword)) {
                // Re-authenticate user
//                    progressBar.setVisibility(View.VISIBLE);
                AuthCredential credential = EmailAuthProvider.getCredential(Objects.requireNonNull(user.getEmail()), oldPassword);
                user.reauthenticate(credential).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Old password is correct, update to new password
                        user.updatePassword(newPassword).addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                Toast.makeText(getActivity(), "Password updated!", Toast.LENGTH_SHORT).show();
                                viewFlipper.setDisplayedChild(0);
                                passwordOld.setText("");
                                passwordNew.setText("");
                                passwordNewConfirm.setText("");
                            } else {
                                Toast.makeText(getActivity(), "Error updating password: " + Objects.requireNonNull(task1.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(getActivity(), "Old password is incorrect", Toast.LENGTH_SHORT).show();
                    }
//                            progressBar.setVisibility(View.GONE);
                });
            } else {
                Toast.makeText(getActivity(), "Passwords do not match or user is not logged in", Toast.LENGTH_SHORT).show();
            }
        });
        passwordCloseButton.setOnClickListener(v -> {
            viewFlipper.setInAnimation(inAnim2);
            viewFlipper.setOutAnimation(outAnim2);
            viewFlipper.setDisplayedChild(0);
        });

        getExistAvatar();
        return view;
    }

    private void initFireBase() {
        if (mAuth != null) {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null) {
                userNameStr = user.getDisplayName();
                userNameTextView.setText(userNameStr);
            } else {
                Toast.makeText(getActivity(), "User not logged in ", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getActivity(), "User not logged in ", Toast.LENGTH_SHORT).show();
        }
    }

    private void setAppBrightness(int brightness) {
        Window window = context.getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.screenBrightness = brightness / 255.0f;
        window.setAttributes(layoutParams);
    }

    private void handleCameraPermission(Boolean isGranted) {
        if (isGranted) {
            viewFlipper.setDisplayedChild(5);
            ((MainActivity) requireActivity()).setBottomNavigationVisibility(View.GONE);
            cameraObj = new Camera(requireActivity(), previewView, null, null, photoUri -> {
                Intent cropIntent = CropImage.activity(photoUri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setCropShape(CropImageView.CropShape.OVAL)
                        .setFixAspectRatio(true)
                        .setAspectRatio(1, 1)
                        .getIntent(requireContext());
                cropActivityResultLauncher.launch(cropIntent);
            });

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

    private final ActivityResultLauncher<Intent> cropActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    CropImage.ActivityResult cropResult = CropImage.getActivityResult(result.getData());
                    Uri uri = cropResult.getUri();
                    avatarButton.setImageURI(uri);
                    viewFlipper.setDisplayedChild(0);

                    // add avtar to database
                    AvatarDB avatarDB = new AvatarDB(getEmail());
                    DBFactory dbFactory = new DBFactory(avatarDB, getEmail());
                    ImageFactory imageFactory = new ImageFactory(avatarDB, dbFactory);
                    imageFactory.uploadAvatar(uri);

                    ((MainActivity) requireActivity()).setBottomNavigationVisibility(View.VISIBLE);
                }
            }
    );

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

    private void getExistAvatar() {
        DocumentReference avatarRef = db.collection("Avatar").document(getEmail());
        avatarRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        String result = (String) document.getData().get("avatar");
                        downloadURL = result;
                        convertToURI();

                    }
                }
            }
        });

    }
    private void getTagsFromDB() {
        DocumentReference tagRef = db.collection("Tags").document(getEmail());
        tagRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        List<String> result = (List<String>) document.getData().get("tagList");
                        tagList = new ArrayList<>(result);

                    }
                }
            }
        });


    }

    private void convertToURI() {
        StorageReference imageRef = storage.getReferenceFromUrl(downloadURL);
        //avatarButton.setImageURI(image);
        Uri image = Uri.parse(downloadURL);
        Glide.with(requireActivity())
                .load(image)
                .into(avatarButton);

    }
    public String getEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user.getEmail();
    }


}