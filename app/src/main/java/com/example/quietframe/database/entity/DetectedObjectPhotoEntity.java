package com.example.quietframe.database.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;

@Entity(tableName = "detected_objects", primaryKeys = {"photoId", "objectId"}, foreignKeys = {@ForeignKey(entity = PhotoEntity.class, parentColumns = "id", childColumns = "photoId", onDelete = 5, onUpdate = 5),
        @ForeignKey(entity = ObjectEntity.class, parentColumns = "id", childColumns = "objectId", onDelete = 5, onUpdate = 5)
})
public class DetectedObjectPhotoEntity {

    private long photoId;

    private long objectId;

    @ColumnInfo(name = "userId")
    private long userId;

    public long getPhotoId() {
        return photoId;
    }

    public void setPhotoId(long photoId) {
        this.photoId = photoId;
    }

    public long getObjectId() {
        return objectId;
    }

    public void setObjectId(long objectId) {
        this.objectId = objectId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }
}
