package com.example.quietframe;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {UserEntity.class, PhotoEntity.class}, version = 1)
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
}
