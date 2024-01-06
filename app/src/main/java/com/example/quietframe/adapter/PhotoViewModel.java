package com.example.quietframe.adapter;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.quietframe.database.entity.PhotoEntity;

import java.util.List;

public class PhotoViewModel extends ViewModel {
    private MutableLiveData<List<PhotoEntity>> photosLiveData = new MutableLiveData<>();
    public LiveData<List<PhotoEntity>> getPhotosLiveData(){
        return photosLiveData;
    }
    public void setPhotos(List<PhotoEntity> photos){
        photosLiveData.setValue(photos);
    }
}
