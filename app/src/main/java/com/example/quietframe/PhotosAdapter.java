package com.example.quietframe;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PhotosAdapter extends RecyclerView.Adapter<PhotosAdapter.PhotoViewHolder> {
    private List<PhotoEntity> photoItems;
    private Context context;

    private OnItemDeleteListener onItemDeleteListener;
    private List<ImageView> indicators;

    public PhotosAdapter(Context context, List<PhotoEntity> dataItems) {
        this.context = context;
        this.photoItems = dataItems;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.photo_item, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        PhotoEntity currentPhotoItem = photoItems.get(holder.getAdapterPosition());

        if (currentPhotoItem.getDenoisedPhotoData() != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(currentPhotoItem.getDenoisedPhotoData(), 0, currentPhotoItem.getDenoisedPhotoData().length);
            holder.getImageViewPhoto().setImageBitmap(bitmap);
        }

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                deleteItem(holder.getAdapterPosition());
                return true;
            }
        });
    }

    private void deleteItem(int position) {
        PhotoEntity photoEntity = photoItems.get(position);

        photoItems.remove(position);
        notifyItemRemoved(position);
        deleteFromDatabase(photoEntity);

        // Call the onItemDeleted method of the OnItemDeleteListener
        if (onItemDeleteListener != null) {
            onItemDeleteListener.onItemDeleted(position);
        }
    }

    private void deleteFromDatabase(PhotoEntity photoEntity) {
        MyDatabase myDatabase = MyDatabase.getDatabase(context);
        PhotoDao photoDao = myDatabase.photoDao();

        new Thread(new Runnable() {
            @Override
            public void run() {
                photoDao.deletePhoto(photoEntity);
            }
        }).start();
    }

    public void updateIndicators(List<ImageView> indicators) {
        this.indicators = indicators;
        notifyDataSetChanged();

    }

    @Override
    public int getItemCount() {
        return photoItems.size();
    }

    public class PhotoViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageViewPhoto;

        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewPhoto = itemView.findViewById(R.id.imageView);
        }

        public ImageView getImageViewPhoto() {
            return imageViewPhoto;
        }
    }

    public interface OnItemDeleteListener {
        void onItemDeleted(int position);
    }
} 