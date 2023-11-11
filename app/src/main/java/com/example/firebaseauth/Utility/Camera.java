package com.example.firebaseauth.Utility;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

public class Camera {
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ProcessCameraProvider cameraProvider;
    private PreviewView previewView;
    private ImageCapture imageCapture;
    private CameraSelector cameraSelector;
    private int cameraFacing;
    private Context context;
    private RecyclerView recyclerView;
    private final List<Object> imageUris;
    private PhotoCaptureCallback callback;


    public Camera(Context context, PreviewView view, RecyclerView recyclerView, List imageUris, PhotoCaptureCallback callback) {
        this.context = context;
        this.previewView = view;
        this.recyclerView = recyclerView;
        this.imageUris = imageUris;
        this.callback = callback;

        cameraProviderFuture = ProcessCameraProvider.getInstance(context);
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                cameraFacing = CameraSelector.LENS_FACING_BACK;
                startCameraX(cameraProvider, cameraFacing);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, getExecutor());
    }

    public interface PhotoCaptureCallback {
        void onPhotoCaptured(Uri photoUri);
    }

    private Executor getExecutor() {
        return ContextCompat.getMainExecutor(context);
    }
    // setup cameraX builder
    private void startCameraX(ProcessCameraProvider cameraProvider, int cameraFacing) {

        cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(cameraFacing)
                .build();

        Preview preview = new Preview.Builder().build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());
        try {
            // Unbind use cases before rebinding
            cameraProvider.unbindAll();

            imageCapture = new ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .setFlashMode(ImageCapture.FLASH_MODE_OFF)
                    .build();
            // Bind use cases to camera
            // need to cast Context to AppCompatActivity, context does not implement Lifecycle
            cameraProvider.bindToLifecycle((AppCompatActivity) context, cameraSelector, preview, imageCapture);

        } catch (Exception e) {
            Log.d("Camera Bind", e.getMessage());
        }

    }

    // take photo when button click
    public void capturePhoto() {
        long timeStamp = System.currentTimeMillis();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, timeStamp);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");

        imageCapture.takePicture(
                new ImageCapture.OutputFileOptions.Builder(
                        context.getContentResolver(),
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        contentValues
                ).build(),
                getExecutor(),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Toast.makeText(context,"Saving...",Toast.LENGTH_SHORT).show();
                        Uri uri = outputFileResults.getSavedUri();
                        // add to image list
                        if (imageUris != null && recyclerView != null) {
                            imageUris.add(imageUris.size() - 1, uri);
                            recyclerView.getAdapter().notifyDataSetChanged();
                        }
                        if (callback != null) {
                            callback.onPhotoCaptured(uri);
                        }
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Toast.makeText(context,"Error: "+exception.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });

    }

    public void flipCamera() {
        if (cameraFacing == CameraSelector.LENS_FACING_FRONT) {
            cameraFacing = CameraSelector.LENS_FACING_BACK;
        } else {
            cameraFacing = CameraSelector.LENS_FACING_FRONT;
        }
        startCameraX(cameraProvider, cameraFacing);
    }

    public void turnOnFlashMode() {
        imageCapture.setFlashMode(ImageCapture.FLASH_MODE_ON);
    }

    public void turnOffFlashMode() {
        imageCapture.setFlashMode(ImageCapture.FLASH_MODE_OFF);
    }

}