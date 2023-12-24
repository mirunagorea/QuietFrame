package com.example.quietframe;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "photos")
public class PhotoEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "photoName")
    private String photoName;

    @ColumnInfo(name = "userId")
    private long userId;

    @ColumnInfo(name = "photoData")
    private byte[] photoData;

    @ColumnInfo(name = "denoisedPhotoDataCNN")
    private byte[] denoisedPhotoDataCNN;

    @ColumnInfo(name = "denoisedPhotoDataNLM")
    private byte[] denoisedPhotoDataNLM;

    @ColumnInfo(name = "denoisedPhotoDataTV")
    private byte[] denoisedPhotoDataTV;

    @ColumnInfo(name = "denoisedPhotoDataWavelet")
    private byte[] denoisedPhotoDataWavelet;

    public String getPhotoName() {
        return photoName;
    }

    public void setPhotoName(String photoName) {
        this.photoName = photoName;
    }

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

    public byte[] getDenoisedPhotoDataNLM() {
        return denoisedPhotoDataNLM;
    }

    public void setDenoisedPhotoDataNLM(byte[] denoisedPhotoData) {
        this.denoisedPhotoDataNLM = denoisedPhotoData;
    }

    public byte[] getDenoisedPhotoDataCNN() {
        return denoisedPhotoDataCNN;
    }

    public void setDenoisedPhotoDataCNN(byte[] denoisedPhotoDataCNN) {
        this.denoisedPhotoDataCNN = denoisedPhotoDataCNN;
    }

    public byte[] getDenoisedPhotoDataTV() {
        return denoisedPhotoDataTV;
    }

    public void setDenoisedPhotoDataTV(byte[] denoisedPhotoDataTV) {
        this.denoisedPhotoDataTV = denoisedPhotoDataTV;
    }

    public byte[] getDenoisedPhotoDataWavelet() {
        return denoisedPhotoDataWavelet;
    }

    public void setDenoisedPhotoDataWavelet(byte[] denoisedPhotoDataWavelet) {
        this.denoisedPhotoDataWavelet = denoisedPhotoDataWavelet;
    }
}
