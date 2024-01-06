package com.example.quietframe.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.quietframe.activities.ForgotPasswordActivity;
import com.example.quietframe.database.MyDatabase;
import com.example.quietframe.R;
import com.example.quietframe.database.dao.UserDao;
import com.example.quietframe.database.entity.UserEntity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ProfileFragment extends Fragment {

    private EditText usernameEditText;
    private EditText emailEditText;
    private ImageView editImageView;
    private ImageView profilePictureImageView;
    private TextView addProfilePicTextView;
    private Button changePasswordButton;
    private Button saveChangesButton;
    private Button logoutButton;
    private ActivityResultLauncher<String> galleryLauncher;
    private Long userId;
    private byte[] profilePictureData;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        usernameEditText = view.findViewById(R.id.usernameEditText);
        emailEditText = view.findViewById(R.id.emailEditText);
        editImageView = view.findViewById(R.id.editImageView);
        profilePictureImageView = view.findViewById(R.id.profilePictureImageView);
        addProfilePicTextView = view.findViewById(R.id.addProfilePicTextView);
        changePasswordButton = view.findViewById(R.id.changePasswordButton);
        saveChangesButton = view.findViewById(R.id.saveChangesButton);
        logoutButton = view.findViewById(R.id.logoutButton);
        if (getActivity().getIntent().hasExtra("ID")) {
            userId = getActivity().getIntent().getExtras().getLong("ID");
            MyDatabase myDatabase = MyDatabase.getDatabase(getActivity());
            UserDao userDao = myDatabase.userDao();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    UserEntity userEntity = userDao.findUserById(userId);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            usernameEditText.setText(userEntity.getUsername());
                            emailEditText.setText(userEntity.getEmail());
                        }
                    });
                }
            }).start();
        }
        galleryLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri result) {
                if (result != null) {
                    addProfilePicTextView.setText("");
                    profilePictureData = getPhotoFromUri(result);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(profilePictureData, 0, profilePictureData.length);
                    profilePictureImageView.setBackgroundResource(R.drawable.circle_background);
                    BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), bitmap);
                    profilePictureImageView.setImageDrawable(bitmapDrawable);
                }
            }
        });
        editImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                galleryLauncher.launch("image/*");
            }
        });
        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });
        saveChangesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString();
                if (email.isEmpty()) {
                    emailEditText.setError("Please add your email address");
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    emailEditText.setError("Please add a valid email address");
                } else {
                    MyDatabase myDatabase = MyDatabase.getDatabase(getActivity());
                    UserDao userDao = myDatabase.userDao();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            UserEntity userEntity = userDao.findUserById(userId);
                            userEntity.setUsername(usernameEditText.getText().toString());
                            userEntity.setEmail(emailEditText.getText().toString());
                            userDao.update(userEntity);
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getActivity(), "Changes saved successfully", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }).start();
                }
            }
        });
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder;
                if (isDarkThemeEnabled()) {
                    builder = new AlertDialog.Builder(getActivity());

                } else {
                    builder = new AlertDialog.Builder(getActivity());
                }
                builder.setTitle("Logout");
                builder.setMessage("Are you sure you want to logout?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getActivity().finish();
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();
            }
        });
        return view;
    }

    private byte[] getPhotoFromUri(Uri imageUri) {
        try {
            InputStream inputStream = getActivity().getContentResolver().openInputStream(imageUri);
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

    private boolean isDarkThemeEnabled() {
        int nightMode = AppCompatDelegate.getDefaultNightMode();
        switch (nightMode) {
            case AppCompatDelegate.MODE_NIGHT_NO:
                return false;
            case AppCompatDelegate.MODE_NIGHT_YES:
                return true;
            case AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM:
                int currentNightMode = getResources().getConfiguration().uiMode
                        & Configuration.UI_MODE_NIGHT_MASK;
                return currentNightMode == Configuration.UI_MODE_NIGHT_YES;
            default:
                return false;
        }
    }
}