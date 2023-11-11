package com.example.firebaseauth.Utility;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.firebaseauth.DatabaseModel.AvatarDB;
import com.example.firebaseauth.DatabaseModel.DBFactory;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.List;

public class ImageFactory {
    private Context context;
    private List<Object> imageUris;
    private DBFactory dbFactory;
    private AvatarDB avatarDB;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef = storage.getReference();


    public ImageFactory(Context context, List<Object> imageUris, DBFactory dbFactory) {
        this.context = context;
        this.imageUris = imageUris;
        this.dbFactory = dbFactory;

    }
    public ImageFactory(AvatarDB avatarDB, DBFactory dbFactory) {
        this.avatarDB = avatarDB;
        this.dbFactory = dbFactory;

    }
    // get file extension (content type)
    private String getExtension(Uri uri) {
        ContentResolver cR = context.getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String type = mime.getExtensionFromMimeType(cR.getType(uri));
        return type;
    }
    private String getAvatarType(Uri uri) {
        String type = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
       return type;
    }

    // upload each image to cloud storage
    public void uploadImage() {
        if (!imageUris.isEmpty()) {

            for (Object image : imageUris) {
                //storageUris.add(addImageToDB(image));
                Uri file = (Uri) image;

                StorageReference imageRef = storageRef.child("images")
                        .child(System.currentTimeMillis() + "." + getExtension(file));

                UploadTask uploadTask = imageRef.putFile(file);
                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }

                        // Continue with the task to get the download URL
                        return imageRef.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            // add download uri to db
                            Uri downloadUri = task.getResult();
                            dbFactory.updateTripDetail(downloadUri);
                            Toast.makeText(context, "Upload image successful", Toast.LENGTH_SHORT).show();


                        } else {
                            Toast.makeText(context, "Upload failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }

        }
    }

    public void uploadAvatar(Uri image) {
        //storageUris.add(addImageToDB(image));
        Uri file = (Uri) image;

        StorageReference imageRef = storageRef.child("images")
                .child(System.currentTimeMillis() + "." + getAvatarType(file));

        UploadTask uploadTask = imageRef.putFile(file);
        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return imageRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    // add download uri to db
                    Uri downloadUri = task.getResult();
                    avatarDB.setAvatar(downloadUri);
                    dbFactory.setAvatarDB(avatarDB);
                    dbFactory.addAvatar();
                }
            }
        });

    }

}
