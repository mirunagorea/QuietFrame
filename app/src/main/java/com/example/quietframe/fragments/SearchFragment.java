package com.example.quietframe.fragments;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.quietframe.DenoiseActivity;
import com.example.quietframe.DetectedObjectDao;
import com.example.quietframe.DetectedObjectPhotoEntity;
import com.example.quietframe.MyDatabase;
import com.example.quietframe.ObjectDao;
import com.example.quietframe.ObjectEntity;
import com.example.quietframe.PhotoDao;
import com.example.quietframe.PhotoEntity;
import com.example.quietframe.R;
import com.example.quietframe.ViewDenoisedPhotosActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.objects.DetectedObject;
import com.google.mlkit.vision.objects.ObjectDetection;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions;
import com.google.mlkit.vision.objects.defaults.PredefinedCategory;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SearchFragment extends Fragment implements RecyclerViewInterface {

    private List<PhotoEntity> usersPhotos;
    private RecyclerView recyclerViewPhotos;
    private PhotosAdapterSearch photosAdapterSearch;
    private long userId;
    private SearchView searchView;
    private int selectedImageFormat = 0;
    //    private ObjectDetector objectDetector;
    private TextView objectsTextView;
    private TextView detectedObjectsTextView;
    private List<ObjectEntity> objects = new ArrayList<>();
    private List<DetectedObjectPhotoEntity> detectedObjects = new ArrayList<>();


    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (getActivity().getIntent().hasExtra("ID")) {
            userId = getActivity().getIntent().getExtras().getLong("ID");
        }
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        recyclerViewPhotos = view.findViewById(R.id.recyclerViewPhotos);
        searchView = view.findViewById(R.id.searchView);
        objectsTextView = view.findViewById(R.id.textViewObjects);
        detectedObjectsTextView = view.findViewById(R.id.textViewDetectedObjects);
//        searchView.clearFocus();

//        // Multiple object detection in static images
//        ObjectDetectorOptions options =
//                new ObjectDetectorOptions.Builder()
//                        .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
//                        .enableMultipleObjects()
//                        .enableClassification()
//                        .build();
//        objectDetector = ObjectDetection.getClient(options);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterList(newText);
                return false;
            }
        });
        populatePhotos();
        setupRecyclerView();

//        for (PhotoEntity photoEntity : usersPhotos) {
//            byte[] photoData = photoEntity.getPhotoData();
//            Bitmap bitmap = BitmapFactory.decodeByteArray(photoData, 0, photoData.length);
//            InputImage image = InputImage.fromBitmap(bitmap, 0);
//            objectDetector.process(image)
//                    .addOnSuccessListener(
//                            new OnSuccessListener<List<DetectedObject>>() {
//                                @Override
//                                public void onSuccess(List<DetectedObject> detectedObjects) {
//                                    for (DetectedObject detectedObject : detectedObjects) {
//                                        Rect boundingBox = detectedObject.getBoundingBox();
//                                        Integer trackingId = detectedObject.getTrackingId();
//                                        for (DetectedObject.Label label : detectedObject.getLabels()) {
//                                            String text = label.getText();
//                                            Log.e("Classification", String.valueOf(photoEntity.getId()) + " " + text);
//                                            float confidence = label.getConfidence();
//                                        }
//                                    }
//                                }
//                            })
//                    .addOnFailureListener(
//                            new OnFailureListener() {
//                                @Override
//                                public void onFailure(@NonNull Exception e) {
//                                    // Task failed with an exception
//                                    // ...
//                                }
//                            });
//        }
        MyDatabase myDatabase = MyDatabase.getDatabase(getContext());
        ObjectDao objectDao = myDatabase.objectDao();
        new Thread(new Runnable() {
            @Override
            public void run() {
                objects = objectDao.getAllObjects();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (ObjectEntity object : objects) {
                            objectsTextView.setText(objectsTextView.getText() + System.getProperty("line.separator") + object.getId() + " " + object.getLabel());
                        }
                    }
                });
            }
        }).start();
        DetectedObjectDao detectedObjectDao = myDatabase.detectedObjectDao();
        new Thread(new Runnable() {
            @Override
            public void run() {
                detectedObjects = detectedObjectDao.getAllDetectedObjects();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (DetectedObjectPhotoEntity detectedObject : detectedObjects) {
                            detectedObjectsTextView.setText(detectedObjectsTextView.getText() + System.getProperty("line.separator") + detectedObject.getObjectId() + detectedObject.getPhotoId());
                        }
                    }
                });
            }
        }).start();
        return view;
    }

    private void filterList(String newText) {
        List<PhotoEntity> filteredList = new ArrayList<>();
        for (PhotoEntity photoEntity : usersPhotos) {
            if (photoEntity.getPhotoName() != null) {
                if (photoEntity.getPhotoName().toLowerCase().contains(newText.toLowerCase())) {
                    filteredList.add(photoEntity);
                }
            }
        }

        if (filteredList.isEmpty()) {
            Toast.makeText(getContext(), "No data found", Toast.LENGTH_LONG).show();
        } else {
            photosAdapterSearch.setFilteredList(filteredList);
        }
    }

    private void setupRecyclerView() {
        recyclerViewPhotos.setLayoutManager(new LinearLayoutManager(getContext()));
        photosAdapterSearch = new PhotosAdapterSearch(usersPhotos, this);
        recyclerViewPhotos.setAdapter(photosAdapterSearch);
    }

    private void populatePhotos() {
        usersPhotos = new ArrayList<>();
        MyDatabase myDatabase = MyDatabase.getDatabase(getActivity());
        PhotoDao photoDao = myDatabase.photoDao();
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<PhotoEntity> photoEntities = photoDao.getAllPhotos(userId);
                for (PhotoEntity photoEntity : photoEntities) {
                    usersPhotos.add(photoEntity);
                }
            }
        }).start();
    }

    @Override
    public void onItemClick(int position) {
        Intent intent = new Intent(getActivity(), ViewDenoisedPhotosActivity.class);
        long photoId = usersPhotos.get(position).getId();
        intent.putExtra("ID", photoId);
        startActivity(intent);
    }

    @Override
    public void onDownloadClick(int position) {
        ContentResolver contentResolver = getContext().getContentResolver();
        Uri images;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            images = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        } else {
            images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }

        PhotoEntity currentPhoto = usersPhotos.get(position);
        String name = currentPhoto.getPhotoName();

        saveImageToGallery(contentResolver, images, currentPhoto.getDenoisedPhotoDataCNN(), "CNN_" + name);
        saveImageToGallery(contentResolver, images, currentPhoto.getDenoisedPhotoDataTV(), "TV_" + name);
        saveImageToGallery(contentResolver, images, currentPhoto.getDenoisedPhotoDataNLM(), "NLM_" + name);
        saveImageToGallery(contentResolver, images, currentPhoto.getDenoisedPhotoDataWavelet(), "Wavelet_" + name);

        Toast.makeText(getActivity(), "Images saved successfully", Toast.LENGTH_SHORT).show();
    }

    private void saveImageToGallery(ContentResolver contentResolver, Uri baseUri, byte[] imageData, String displayName) {
        SharedPreferences formatSharedPreferences = getContext().getSharedPreferences("FORMAT", Context.MODE_PRIVATE);
        selectedImageFormat = formatSharedPreferences.getInt("format", 0);
        ContentValues contentValues = new ContentValues();
        if (selectedImageFormat == 0 || selectedImageFormat == -1) {
            contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, displayName + ".jpg");
        } else if (selectedImageFormat == 1) {
            contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, displayName + ".png");
        } else if (selectedImageFormat == 2) {
            contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, displayName + ".webp");
        }
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "images/*");
        Uri uri = contentResolver.insert(baseUri, contentValues);

        try (OutputStream outputStream = contentResolver.openOutputStream(Objects.requireNonNull(uri))) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
            if (bitmap != null) {
                if (selectedImageFormat == 0 || selectedImageFormat == -1) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);

                } else if (selectedImageFormat == 1) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                } else if (selectedImageFormat == 2) {
                    bitmap.compress(Bitmap.CompressFormat.WEBP, 100, outputStream);
                }
                Objects.requireNonNull(outputStream).close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}