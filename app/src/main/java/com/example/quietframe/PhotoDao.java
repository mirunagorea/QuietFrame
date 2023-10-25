package com.example.quietframe;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface PhotoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertPhoto(PhotoEntity photoEntity);

    @Query("SELECT * FROM photos WHERE userId=:userId")
    List<PhotoEntity> getAllPhotos(long userId);

    @Query("SELECT * FROM photos WHERE id=:photoId")
    PhotoEntity getByPhotoId(long photoId);

    @Delete
    void deletePhoto(PhotoEntity photoEntity);

    @Update
    void update(PhotoEntity photoEntity);
}
