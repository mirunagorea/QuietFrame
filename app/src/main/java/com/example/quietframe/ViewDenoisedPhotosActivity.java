package com.example.quietframe;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.OutputStream;
import java.util.Objects;

public class ViewDenoisedPhotosActivity extends AppCompatActivity {
    private ImageView imgViewCNN;
    private ImageView imgViewTV;
    private ImageView imgViewNLM;
    private ImageView imgViewWavelet;

    private ImageView imgViewDownloadCNN;
    private ImageView imgViewDownloadTV;
    private ImageView imgViewDownloadNLM;
    private ImageView imgViewDownloadWavelet;
    private TextView textViewCNN;
    private TextView textViewTV;
    private TextView textViewNLM;
    private TextView textViewWavelet;
    private long photoId;
    private int selectedImageFormat = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_denoised_photos);

        imgViewCNN = findViewById(R.id.imgViewCNN);
        imgViewTV = findViewById(R.id.imgViewTV);
        imgViewNLM = findViewById(R.id.imgViewNLM);
        imgViewWavelet = findViewById(R.id.imgViewWavelet);

        imgViewDownloadCNN = findViewById(R.id.imageViewDownloadCNN);
        imgViewDownloadTV = findViewById(R.id.imageViewDownloadTV);
        imgViewDownloadNLM = findViewById(R.id.imageViewDownloadNLM);
        imgViewDownloadWavelet = findViewById(R.id.imageViewDownloadWavelet);

        textViewCNN = findViewById(R.id.textViewCNN);
        textViewTV = findViewById(R.id.textViewTV);
        textViewNLM = findViewById(R.id.textViewNLM);
        textViewWavelet = findViewById(R.id.textViewWavelet);

        changeTextColor(textViewCNN, "The result produced by the Convolutional Neural Network", "Convolutional Neural Network");
        changeTextColor(textViewTV, "The result produced by the Total Variation Algorithm", "Total Variation Algorithm");
        changeTextColor(textViewNLM, "The result produced by the Non-Local Means Algorithm", "Non-Local Means Algorithm");
        changeTextColor(textViewWavelet, "The result produced by the Wavelet Algorithm", "Wavelet Algorithm");

        if(getIntent().hasExtra("ID")){
            photoId = getIntent().getExtras().getLong("ID");
        }

        MyDatabase myDatabase = MyDatabase.getDatabase(this);
        PhotoDao photoDao = myDatabase.photoDao();
        new Thread(new Runnable() {
            @Override
            public void run() {
                PhotoEntity photoEntity = photoDao.getByPhotoId(photoId);
                Bitmap bmpCNN = BitmapFactory.decodeByteArray(photoEntity.getDenoisedPhotoDataCNN(), 0, photoEntity.getDenoisedPhotoDataCNN().length);
                Bitmap bmpTV = BitmapFactory.decodeByteArray(photoEntity.getDenoisedPhotoDataTV(), 0, photoEntity.getDenoisedPhotoDataTV().length);
                Bitmap bmpNLM = BitmapFactory.decodeByteArray(photoEntity.getDenoisedPhotoDataNLM(), 0, photoEntity.getDenoisedPhotoDataNLM().length);
                Bitmap bmpWavelet = BitmapFactory.decodeByteArray(photoEntity.getDenoisedPhotoDataWavelet(), 0, photoEntity.getDenoisedPhotoDataWavelet().length);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        imgViewCNN.setImageBitmap(bmpCNN);
                        imgViewTV.setImageBitmap(bmpTV);
                        imgViewNLM.setImageBitmap(bmpNLM);
                        imgViewWavelet.setImageBitmap(bmpWavelet);
                    }
                });
            }
        }).start();
        imgViewDownloadCNN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveImage(imgViewCNN, "CNN");
            }
        });
        imgViewDownloadTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveImage(imgViewTV, "TV");
            }
        });
        imgViewDownloadNLM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveImage(imgViewNLM, "NLM");
            }
        });
        imgViewDownloadWavelet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveImage(imgViewWavelet, "Wavelet");
            }
        });
    }

    private void saveImage(ImageView imgView, String algorithm) {
        SharedPreferences formatSharedPreferences = this.getSharedPreferences("FORMAT", Context.MODE_PRIVATE);
        selectedImageFormat = formatSharedPreferences.getInt("format", 0);
        Uri images;
        ContentResolver contentResolver = getContentResolver();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            images = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        } else {
            images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }

        ContentValues contentValues = new ContentValues();

        MyDatabase myDatabase = MyDatabase.getDatabase(this);
        PhotoDao photoDao = myDatabase.photoDao();
        new Thread(new Runnable() {
            @Override
            public void run() {
                PhotoEntity photoEntity = photoDao.getByPhotoId(photoId);
                if (selectedImageFormat == 0 || selectedImageFormat == -1) {
                    contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, photoEntity.getPhotoName() + "_" + algorithm + ".jpg");
                } else if (selectedImageFormat == 1) {
                    contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, photoEntity.getPhotoName() + "_" + algorithm + ".png");
                } else if (selectedImageFormat == 2) {
                    contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, photoEntity.getPhotoName() + "_" + algorithm + ".webp");
                }
                contentValues.put(MediaStore.Images.Media.MIME_TYPE, "images/*");
                Uri uri = contentResolver.insert(images, contentValues);
                try {
                    BitmapDrawable bitmapDrawable = (BitmapDrawable) imgView.getDrawable();
                    Bitmap bitmap = bitmapDrawable.getBitmap();

                    OutputStream outputStream = contentResolver.openOutputStream(Objects.requireNonNull(uri));
                    if (selectedImageFormat == 0 || selectedImageFormat == -1) {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    } else if (selectedImageFormat == 1) {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                    } else if (selectedImageFormat == 2) {
                        bitmap.compress(Bitmap.CompressFormat.WEBP, 100, outputStream);
                    }
                    Objects.requireNonNull(outputStream);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ViewDenoisedPhotosActivity.this, "Image saved successfully", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ViewDenoisedPhotosActivity.this, "Image not saved", Toast.LENGTH_SHORT).show();
                        }
                    });
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void changeTextColor(TextView textView, String fullText, String differentColorText){
        SpannableString spannableString = new SpannableString(fullText);
        int startIndex = fullText.indexOf(differentColorText);
        int endIndex = startIndex + differentColorText.length();
        spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.color_primary_light)), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(spannableString);
    }
}