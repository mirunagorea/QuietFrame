package com.example.quietframe;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "photos")
public class PhotoEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "userId")
    private long userId;

    @ColumnInfo(name = "photoData")
    private byte[] photoData;

    @ColumnInfo(name = "denoisedPhotoData")
    private byte[] denoisedPhotoData;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public byte[] getPhotoData() {
        return photoData;
    }

    public void setPhotoData(byte[] photoData) {
        this.photoData = photoData;
    }

    public byte[] getDenoisedPhotoData() {
        return denoisedPhotoData;
    }

    public void setDenoisedPhotoData(byte[] denoisedPhotoData) {
        this.denoisedPhotoData = denoisedPhotoData;
    }
}
