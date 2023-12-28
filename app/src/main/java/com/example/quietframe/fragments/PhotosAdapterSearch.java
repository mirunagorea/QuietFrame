package com.example.quietframe.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quietframe.PhotoEntity;
import com.example.quietframe.R;

import java.util.List;

public class PhotosAdapterSearch extends RecyclerView.Adapter<PhotoViewHolder> {
    private List<PhotoEntity> photos;
    private final RecyclerViewInterface recyclerViewInterface;

    public void setFilteredList(List<PhotoEntity> filteredList) {
        this.photos = filteredList;
        notifyDataSetChanged();
    }

    public PhotosAdapterSearch(List<PhotoEntity> photos, RecyclerViewInterface recyclerViewInterface) {
        this.photos = photos;
        this.recyclerViewInterface = recyclerViewInterface;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.photo_item_search, parent, false);
        return new PhotoViewHolder(itemView, recyclerViewInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        PhotoEntity currentPhoto = photos.get(position);
        Bitmap bitmap = BitmapFactory.decodeByteArray(currentPhoto.getPhotoData(), 0, currentPhoto.getPhotoData().length);
        holder.getPhotoImageView().setImageBitmap(bitmap);
        holder.getPhotoTextView().setText(currentPhoto.getPhotoName());
        holder.getPhotoTextView().setText(holder.getPhotoTextView().getText() + " " + currentPhoto.getId());
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }
}
