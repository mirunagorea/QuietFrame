package com.example.quietframe.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.quietframe.database.MyDatabase;
import com.example.quietframe.database.dao.PhotoDao;
import com.example.quietframe.database.entity.PhotoEntity;
import com.example.quietframe.R;

public class SetPhotoNameActivity extends AppCompatActivity {
    private EditText photoNameEditText;
    private Button submitButton;
    private long photoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_photo_name);
        photoNameEditText = findViewById(R.id.setNameEditText);
        submitButton = findViewById(R.id.submitButton);
        if (getIntent().hasExtra("ID")) {
            photoId = getIntent().getExtras().getLong("ID");
        }
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyDatabase myDatabase = MyDatabase.getDatabase(SetPhotoNameActivity.this);
                PhotoDao photoDao = myDatabase.photoDao();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        PhotoEntity photoEntity = photoDao.getByPhotoId(photoId);
                        photoEntity.setPhotoName(String.valueOf(photoNameEditText.getText()));
                        photoDao.update(photoEntity);
                    }
                }).start();
                Intent intent = new Intent(SetPhotoNameActivity.this, DenoiseActivity.class);
                intent.putExtra("ID", photoId);
                startActivity(intent);
            }
        });
    }
}