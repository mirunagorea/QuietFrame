package com.example.quietframe.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.quietframe.database.entity.DetectedObjectPhotoEntity;
import com.example.quietframe.database.entity.PhotoEntity;

import java.util.List;

@Dao
public interface DetectedObjectDao {
    @Insert
    long insertDetectedObject(DetectedObjectPhotoEntity detectedObjectPhotoEntity);

    @Query("SELECT * FROM detected_objects WHERE userId=:userId AND photoId=:photoId")
    List<DetectedObjectPhotoEntity> getAllDetectedObjectsByPhotoId(long userId, long photoId);

    @Query("SELECT * FROM photos INNER JOIN detected_objects ON photos.id == detected_objects.photoId WHERE detected_objects.objectId=:objectId")
    List<PhotoEntity> getPhotosByDetectedObjectId(long objectId);
}
