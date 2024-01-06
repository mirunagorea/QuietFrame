package com.example.quietframe.fragments.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quietframe.R;
import com.example.quietframe.fragments.adapter.RecyclerViewInterface;

public class PhotoViewHolder extends RecyclerView.ViewHolder {
    private final ImageView photoImageView;
    private final TextView photoTextView;
    private final ImageView imageViewDownload;

    public PhotoViewHolder(@NonNull View itemView, RecyclerViewInterface recyclerViewInterface) {
        super(itemView);
        photoImageView = itemView.findViewById(R.id.imageViewPhoto);
        photoTextView = itemView.findViewById(R.id.textViewPhoto);
        imageViewDownload = itemView.findViewById(R.id.imageViewDownload);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (recyclerViewInterface != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        recyclerViewInterface.onItemClick(position);
                    }
                }
            }
        });
        imageViewDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (recyclerViewInterface != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        recyclerViewInterface.onDownloadClick(position);
                    }
                }
            }
        });
    }

    public ImageView getPhotoImageView() {
        return photoImageView;
    }

    public TextView getPhotoTextView() {
        return photoTextView;
    }
}
