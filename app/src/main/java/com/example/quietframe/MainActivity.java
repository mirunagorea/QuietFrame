package com.example.quietframe;
import android.content.Context;
import android.content.Intent;
import android.database.CursorWindow;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.chaquo.python.android.AndroidPlatform;
import com.example.quietframe.fragments.HomeFragment;
import com.example.quietframe.fragments.ProfileFragment;
import com.example.quietframe.fragments.SearchFragment;
import com.example.quietframe.fragments.SettingsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.opencv.android.OpenCVLoader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import com.chaquo.python.Python;

public class MainActivity extends AppCompatActivity {
    private FloatingActionButton fab;
    private BottomNavigationView bottomNavigationView;

    private ActivityResultLauncher<String> galleryLauncher;
    private byte[] photoData;

    private long userId;
    private static String TAG = "MainActivity";
    static{
        if(OpenCVLoader.initDebug()){
            Log.e(TAG, "opencv installed successfully");
        }
        else{
            Log.e(TAG, "opencv isn't installed");
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = this;
        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(context));
        }
        try {
            Field field = CursorWindow.class.getDeclaredField("sCursorWindowSize");
            field.setAccessible(true);
            field.set(null, 100 * 1024 * 1024); //the 100MB is the new size
        } catch (Exception e) {
            e.printStackTrace();
        }
        setContentView(R.layout.activity_main);
        if (getIntent().hasExtra("ID")) {
            userId = getIntent().getExtras().getLong("ID");
        }

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        fab = findViewById(R.id.fab);
//        Toolbar toolbar = findViewById(R.id.toolbar);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new HomeFragment()).commit();
        }

//        replaceFragment(new HomeFragment());

        bottomNavigationView.setBackground(null);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.home) replaceFragment(new HomeFragment());
            else if (item.getItemId() == R.id.search) replaceFragment(new SearchFragment());
            else if (item.getItemId() == R.id.profile) replaceFragment(new ProfileFragment());
            else if (item.getItemId() == R.id.settings) replaceFragment(new SettingsFragment());
            return true;
        });

        galleryLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri result) {
                if (result != null) {
                    photoData = getPhotoFromUri(result);
                    MyDatabase myDatabase = MyDatabase.getDatabase(MainActivity.this);
                    PhotoDao photoDao = myDatabase.photoDao();
                    PhotoEntity photoEntity = new PhotoEntity();
                    photoEntity.setUserId(userId);
                    photoEntity.setPhotoData(photoData);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            long photoId = photoDao.insertPhoto(photoEntity);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.e("MAIN PHOTO ID", String.valueOf(photoId));
                                    Toast.makeText(MainActivity.this, "Photo added successfully", Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(MainActivity.this, SetPhotoName.class);
                                    intent.putExtra("ID", photoId);
                                    startActivity(intent);
                                }
                            });
                        }
                    }).start();
                }
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                galleryLauncher.launch("image/*");
            }
        });
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if(fragment instanceof SettingsFragment){
            fragmentTransaction.replace(R.id.frame_layout, fragment);
        }
        else{
            fragmentTransaction.replace(R.id.frame_layout, fragment);
            fragmentTransaction.addToBackStack(null);
        }
        fragmentTransaction.commit();
    }

    private byte[] getPhotoFromUri(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            byte[] photoData = getBytesFromInputStream(inputStream);
            return photoData;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    private byte[] getBytesFromInputStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }
}