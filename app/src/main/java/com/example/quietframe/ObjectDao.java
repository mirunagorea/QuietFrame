package com.example.quietframe;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ObjectDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertObject(ObjectEntity objectEntity);

    @Query("SELECT * FROM objects")
    List<ObjectEntity> getAllObjects();
}
