package com.example.quietframe;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "detected_objects", primaryKeys = {"photoId", "objectId"}, foreignKeys = {@ForeignKey(entity = PhotoEntity.class, parentColumns = "id", childColumns = "photoId", onDelete = 5, onUpdate = 5),
        @ForeignKey(entity = ObjectEntity.class, parentColumns = "id", childColumns = "objectId", onDelete = 5, onUpdate = 5)
})
public class DetectedObjectPhotoEntity {

    private long photoId;

    private long objectId;

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
}
