package com.example.quietframe.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.quietframe.database.dao.DetectedObjectDao;
import com.example.quietframe.database.entity.DetectedObjectPhotoEntity;
import com.example.quietframe.database.dao.ObjectDao;
import com.example.quietframe.database.entity.ObjectEntity;
import com.example.quietframe.database.dao.PhotoDao;
import com.example.quietframe.database.entity.PhotoEntity;
import com.example.quietframe.database.dao.UserDao;
import com.example.quietframe.database.entity.UserEntity;

@Database(entities = {UserEntity.class, PhotoEntity.class, ObjectEntity.class, DetectedObjectPhotoEntity.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class  MyDatabase extends RoomDatabase {
    private static final String dbName = "myDatabase";
    private static MyDatabase myDatabase;

    public static synchronized MyDatabase getDatabase(Context context) {
        if (myDatabase == null)
            myDatabase = Room.databaseBuilder(context, MyDatabase.class, dbName).fallbackToDestructiveMigration().build();
        return myDatabase;
    }

    public abstract UserDao userDao();
    public abstract PhotoDao photoDao();
    public abstract ObjectDao objectDao();
    public abstract DetectedObjectDao detectedObjectDao();
}
