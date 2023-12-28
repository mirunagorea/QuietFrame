package com.example.quietframe;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DetectedObjectDao {
    @Insert
    long insertDetectedObject(DetectedObjectPhotoEntity detectedObjectPhotoEntity);

    @Query("SELECT * FROM detected_objects")
    List<DetectedObjectPhotoEntity> getAllDetectedObjects();

    @Query("SELECT * FROM photos INNER JOIN detected_objects ON photos.id == detected_objects.photoId WHERE detected_objects.objectId=:objectId")
    List<PhotoEntity> getPhotosByDetectedObjectId(long objectId);
}
