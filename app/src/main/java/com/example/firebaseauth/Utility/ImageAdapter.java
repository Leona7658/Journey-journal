package com.example.firebaseauth.Utility;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.firebaseauth.R;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final Context context;
    private final List<Object> imageUris;
    private final int imageSize;
    public static final Object SELECT_BUTTON_PLACEHOLDER = new Object();
    private OnItemClickListener listener;
    private OnItemLongClickListener longClickListener;

    public ImageAdapter(Context context, List<Object> imageUris) {
        this.context = context;
        this.imageUris = imageUris;

        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int screenWidth = metrics.widthPixels;
        imageSize = (screenWidth-120) / 3;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 1) {
            ImageView imageView = new ImageView(context);
            imageView.setLayoutParams(new RecyclerView.LayoutParams(imageSize, imageSize));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
            return new ImageViewHolder(imageView);
        } else {
            Button button = new Button(context);
            button.setText("");
            button.setGravity(Gravity.CENTER);

            Drawable drawable = ContextCompat.getDrawable(context, R.drawable.plus_icon);
            if (drawable != null) {
                drawable = DrawableCompat.wrap(drawable).mutate();
                DrawableCompat.setTint(drawable, ContextCompat.getColor(context, R.color.light_grey));
                drawable.setBounds(0, 0, imageSize/2, imageSize/2);
                button.setCompoundDrawables(drawable, null, null, null);
                button.setPadding(imageSize/4, 0, 0, 0);
            }

            button.setBackgroundColor(ContextCompat.getColor(context, R.color.lighter_grey));
            button.setLayoutParams(new RecyclerView.LayoutParams(imageSize, imageSize));
            return new ButtonViewHolder(button);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object item = imageUris.get(position);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(v, position);
            }
        });

        if (holder instanceof ImageViewHolder && item instanceof Uri) {
            Glide.with(context).load((Uri) item).into(((ImageViewHolder) holder).imageView);
        } else if (holder instanceof ButtonViewHolder && item == SELECT_BUTTON_PLACEHOLDER) {
            ((ButtonViewHolder) holder).button.setOnClickListener(view -> showCameraOrGalleryOptions());
        }

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                return longClickListener.onItemLongClick(v, position);
            }
            return false;
        });
    }

    private void showCameraOrGalleryOptions() {
        final Dialog dialog = new Dialog(context, R.style.CustomAlertDialogStyle);
        dialog.setContentView(R.layout.image_source_dialog);

        Button cameraButton = dialog.findViewById(R.id.image_source_camera_button);
        Button galleryButton = dialog.findViewById(R.id.image_source_gallery_button);
        Button cancelButton = dialog.findViewById(R.id.image_source_cancel_button);

        cameraButton.setOnClickListener(v -> {
            if (optionSelectedListener != null) {
                optionSelectedListener.onOptionSelected(0);
            }
            dialog.dismiss();
        });

        galleryButton.setOnClickListener(v -> {
            if (optionSelectedListener != null) {
                optionSelectedListener.onOptionSelected(1);
            }
            dialog.dismiss();
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());

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
    }
    public interface OnOptionSelectedListener {
        void onOptionSelected(int which);
    }
    private OnOptionSelectedListener optionSelectedListener;
    public void setOnOptionSelectedListener(OnOptionSelectedListener listener) {
        this.optionSelectedListener = listener;
    }
    @Override
    public int getItemViewType(int position) {
        if (imageUris.get(position) instanceof Uri) {
            return 1;
        } else {
            return 2;
        }
    }

    @Override
    public int getItemCount() {
        return imageUris.size();
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public interface OnItemLongClickListener {
        boolean onItemLongClick(View view, int position);
    }

    public void setOnItemLongClickListener(OnItemLongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ImageViewHolder(@NonNull ImageView itemView) {
            super(itemView);
            imageView = itemView;
        }
    }

    public static class ButtonViewHolder extends RecyclerView.ViewHolder {
        Button button;

        public ButtonViewHolder(@NonNull Button itemView) {
            super(itemView);
            button = itemView;
        }
    }

}