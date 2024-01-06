package com.example.quietframe.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.quietframe.database.entity.ObjectEntity;

import java.util.List;

@Dao
public interface ObjectDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertObject(ObjectEntity objectEntity);

    @Query("SELECT * FROM objects")
    List<ObjectEntity> getAllObjects();

    @Query("SELECT label FROM objects WHERE id=:id")
    String getObjectLabelById(long id);
}
