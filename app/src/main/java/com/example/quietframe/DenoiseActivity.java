package com.example.quietframe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;

import org.opencv.android.OpenCVLoader;
import org.pytorch.IValue;
import org.pytorch.LiteModuleLoader;
import org.pytorch.Module;
import org.pytorch.Tensor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

public class DenoiseActivity extends AppCompatActivity {
    private SeekBar seekBar;
    private byte[] denoisedPhotoDataCNN;
    private byte[] denoisedPhotoDataNLM;
    private byte[] denoisedPhotoDataTV;
    private byte[] denoisedPhotoDataWavelet;
    private ImageView imageViewCNN;
    private ImageView imageViewNLM;
    private ImageView imageViewTV;
    private ImageView imageViewWavelet;
    private ImageView imageViewBefore;
    private ImageView imageViewAfter;
    private Button downloadButton;
    private CardView otherEditsCardView;
    private FrameLayout frameLayout;
    private ProgressBar loadingProgressBar;
    private TextView textViewButton;
    private View blueView;
    private Module module;
    private ConstraintLayout toggleBtnLayout;
    private boolean isCardViewOpen = false;

    private static final int REQUEST_CODE = 100;

    private static String TAG = "DenoiseActivity";

    private int selectedImageFormat = 0;

    private String imageString = "";
    private Bitmap outputBitmap;
    private long photoId;

    static {
        if (OpenCVLoader.initDebug()) {
            Log.e(TAG, "opencv installed successfully");
        } else {
            Log.e(TAG, "opencv isn't installed");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_denoise);
        imageViewCNN = findViewById(R.id.imgViewCNN);
        imageViewNLM = findViewById(R.id.imgViewNLM);
        imageViewTV = findViewById(R.id.imgViewTV);
        imageViewWavelet = findViewById(R.id.imgViewWavelet);
        imageViewBefore = findViewById(R.id.imageViewBefore);
        imageViewAfter = findViewById(R.id.imageViewAfter);
        downloadButton = findViewById(R.id.buttonSave);
        otherEditsCardView = findViewById(R.id.cardViewOtherEdits);
        textViewButton = findViewById(R.id.textViewButton);
        blueView = findViewById(R.id.blueView);
        toggleBtnLayout = findViewById(R.id.toggleBtn);
        frameLayout = findViewById(R.id.frameLayout);
        loadingProgressBar = findViewById(R.id.loadingCircle);
        seekBar = findViewById(R.id.seekBar);
        downloadButton.setVisibility(View.INVISIBLE);
        imageViewAfter.setVisibility(View.INVISIBLE);
        frameLayout.setVisibility(View.INVISIBLE);
        otherEditsCardView.setVisibility(View.INVISIBLE);
        seekBar.setVisibility(View.INVISIBLE);

        // Load the model
        try {
            module = LiteModuleLoader.load(assetFilePath("model.pt"));
        } catch (IOException e) {
            Log.e(TAG, "Unable to load model", e);
        }

        photoId = getIntent().getExtras().getLong("ID");
        if (photoId == 0) Log.e("PHOTOID", "Nu s-a pus id extra");
        else Log.e("PHOTOID", String.valueOf(photoId));
        MyDatabase myDatabase = MyDatabase.getDatabase(this);
        PhotoDao photoDao = myDatabase.photoDao();
        new Thread(new Runnable() {
            @Override
            public void run() {
                PhotoEntity photoEntity = photoDao.getByPhotoId(photoId);
                Log.e("POZA ADAUGATA", String.valueOf(photoEntity.getPhotoData()));
                byte[] imageData = photoEntity.getPhotoData();
//                for (int i = 0; i < 20; i++) {
//                    Log.d("imageDataArrayValue", "Index" + i + ": " + String.valueOf(imageData[i]));
//                }
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                Log.e("!!!!imageData.length", String.valueOf(imageData.length));
                Log.e("!!!!width", String.valueOf(width));
                Log.e("!!!!height", String.valueOf(height));
                Tensor imageTensor = byteArrayToTensor(imageData);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Tensor outputTensor = module.forward(IValue.from(imageTensor)).toTensor();
                        float[] outputArr = outputTensor.getDataAsFloatArray();
                        float[] processedArray = postProcessOutput(outputArr, height, width);
                        outputBitmap = createBitmapFromProcessedArray(processedArray, height, width);
                        denoisedPhotoDataCNN = bitmapToByteArray(outputBitmap);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                imageViewCNN.setImageBitmap(outputBitmap);
                            }
                        });
                    }
                }).start();

                denoisedPhotoDataNLM = Denoising.nonLocalMeansDenoising(photoEntity.getPhotoData());
                Bitmap bmpNLM = BitmapFactory.decodeByteArray(denoisedPhotoDataNLM, 0, denoisedPhotoDataNLM.length);

                imageString = getStringImage(photoEntity.getPhotoData());
                final Python py = Python.getInstance();
                PyObject pyo = py.getModule("total_variation");
                PyObject obj = pyo.callAttr("total_variation", imageString);
                String str = obj.toString();
                byte dataTV[] = Base64.decode(str, Base64.DEFAULT);
                Bitmap bmpTV = BitmapFactory.decodeByteArray(dataTV, 0, dataTV.length);
//                denoisedPhotoDataTV = Denoising.medianFilterDenoising(photoEntity.getPhotoData(), 10);
                denoisedPhotoDataTV = bitmapToByteArray(bmpTV);

                PyObject objWavelet = pyo.callAttr("wavelet_denoising", imageString);
                String strWavelet = objWavelet.toString();
                byte dataWavelet[]= Base64.decode(strWavelet, Base64.DEFAULT);
                Bitmap bmpWavelet = BitmapFactory.decodeByteArray(dataWavelet, 0, dataWavelet.length);
                denoisedPhotoDataWavelet = bitmapToByteArray(bmpWavelet);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        imageViewBefore.setImageBitmap(BitmapFactory.decodeByteArray(photoEntity.getPhotoData(), 0, photoEntity.getPhotoData().length));
                        imageViewAfter.setImageBitmap(outputBitmap);
                        imageViewCNN.setImageBitmap(outputBitmap);
                        downloadButton.setVisibility(View.VISIBLE);
                        imageViewAfter.setVisibility(View.VISIBLE);
                        frameLayout.setVisibility(View.VISIBLE);
                        otherEditsCardView.setVisibility(View.VISIBLE);
                        seekBar.setVisibility(View.VISIBLE);
                        loadingProgressBar.setVisibility(View.GONE);
//                        Bitmap bitmap3 = BitmapFactory.decodeByteArray(denoisedPhotoData3, 0, denoisedPhotoData3.length);
                        imageViewTV.setImageBitmap(bmpTV);
                        imageViewWavelet.setImageBitmap(bmpWavelet);
                        imageViewNLM.setImageBitmap((bmpNLM));
                    }
                });
                photoEntity.setId(photoId);
                photoEntity.setUserId(photoEntity.getUserId());
                photoEntity.setPhotoData(photoEntity.getPhotoData());
                photoEntity.setDenoisedPhotoDataNLM(denoisedPhotoDataNLM);
                photoEntity.setDenoisedPhotoDataCNN(denoisedPhotoDataCNN);
                photoEntity.setDenoisedPhotoDataTV(denoisedPhotoDataTV);
                photoEntity.setDenoisedPhotoDataWavelet(denoisedPhotoDataWavelet);
                photoDao.update(photoEntity);
            }
        }).start();

        float density = getResources().getDisplayMetrics().density;
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                FrameLayout target = (FrameLayout) findViewById(R.id.frameLayout);
                progress = progresValue;
                ViewGroup.LayoutParams lp = target.getLayoutParams();
                lp.width = (int) (progress * density + 0.5f);
                if (lp.width >= 2)
                    target.setLayoutParams(lp);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (Environment.isExternalStorageManager()) {
                        // Permission granted, proceed with saving image
                        saveImage();
                    } else {
                        // Request the MANAGE_EXTERNAL_STORAGE permission
                        Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                        Toast.makeText(DenoiseActivity.this, "Please grant permission to save the image", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // For devices below Android 10, request WRITE_EXTERNAL_STORAGE permission
                    if (ContextCompat.checkSelfPermission(DenoiseActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                        saveImage();
                    else
                        askPermission();
                }
            }
        });

        toggleBtnLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleCardViewVisibility();
            }
        });

        imageViewCNN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageViewAfter.setImageDrawable(imageViewCNN.getDrawable());
            }
        });

        imageViewNLM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageViewAfter.setImageDrawable(imageViewNLM.getDrawable());
            }
        });

        imageViewTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageViewAfter.setImageDrawable(imageViewTV.getDrawable());
            }
        });

        imageViewWavelet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageViewAfter.setImageDrawable(imageViewWavelet.getDrawable());
            }
        });
    }

    private byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    private String getStringImage(byte[] photoData) {
        String encodedImage = android.util.Base64.encodeToString(photoData, Base64.DEFAULT);
        return encodedImage;
    }

    private void toggleCardViewVisibility() {
        isCardViewOpen = !isCardViewOpen;
        int targetHeight = isCardViewOpen ? getResources().getDimensionPixelSize(R.dimen.card_view_opened_height) : getResources().getDimensionPixelSize(R.dimen.card_view_closed_height);
        ValueAnimator valueAnimator = ValueAnimator.ofInt(otherEditsCardView.getHeight(), targetHeight);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(@NonNull ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = otherEditsCardView.getLayoutParams();
                layoutParams.height = value;
                otherEditsCardView.setLayoutParams(layoutParams);
            }
        });

        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (isCardViewOpen) {
                    otherEditsCardView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!isCardViewOpen) {
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        valueAnimator.setDuration(300);
        valueAnimator.start();
    }

    private Tensor byteArrayToTensor(byte[] imageData) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
        Tensor inputTensor = normalizeImage(bitmap);
        long[] tensorShape = inputTensor.shape();
//        for (long dimension : tensorShape) {
//            Log.d("TensorShape", Long.toString(dimension));
//        }
        return inputTensor;
    }

    private Tensor normalizeImage(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        //imageBytes RGB
        int channels = 3;
        float[] floatArray = new float[channels * height * width];
        int floatIndex = 0;
        for (int i = 0; i < channels; i++) {
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int pixel = bitmap.getPixel(x, y);
                    if (i == 0) {
                        //blue
                        floatArray[floatIndex++] = (Color.blue(pixel) & 0xFF);
                    } else if (i == 1) {
                        //green
                        floatArray[floatIndex++] = (Color.green(pixel) & 0xFF);
                    } else {
                        floatArray[floatIndex++] = (Color.red(pixel) & 0xFF);
                    }
                }
            }
        }
        long[] shape = {1, 3, height, width};
        return Tensor.fromBlob(floatArray, shape);
    }

    private float[] postProcessOutput(float[] outputArr, int height, int width) {
        for (int i = 0; i < outputArr.length; i++) {
            outputArr[i] = Math.min(255, Math.max(0, outputArr[i]));
        }
//        for (int i = 0; i < 100; i++) {
//            Log.d("OutputArrayValue", "Index" + i + ": " + outputArr[i]);
//        }
        return outputArr;
    }

    private Bitmap createBitmapFromProcessedArray(float[] processedArray, int height, int width) {
        Bitmap outputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        int loc = 0;
        int channels = 3;
        for (int c = 0; c < channels; c++) {
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int pixelValue = (int) processedArray[loc++];
                    int currentColor = outputBitmap.getPixel(x, y);
                    if (c == 0) {
                        //blue
                        currentColor = Color.rgb(Color.red(currentColor), Color.green(currentColor), pixelValue);
                    } else if (c == 1) {
                        //green
                        currentColor = Color.rgb(Color.red(currentColor), pixelValue, Color.blue(currentColor));
                    } else {
                        //red
                        currentColor = Color.rgb(pixelValue, Color.green(currentColor), Color.blue(currentColor));
                    }
                    outputBitmap.setPixel(x, y, currentColor);
                }
            }
        }
        return outputBitmap;
    }

    private String assetFilePath(String s) throws IOException {
        File file = new File(this.getFilesDir(), s);
        if (file.exists() && file.length() > 0) {
            return file.getAbsolutePath();
        }

        try (InputStream is = this.getAssets().open(s)) {
            try (OutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            }
            return file.getAbsolutePath();
        }
    }

    private void askPermission() {
        ActivityCompat.requestPermissions(DenoiseActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveImage();
            } else {
                Toast.makeText(DenoiseActivity.this, "Please provide required permission", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveImage() {
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
        PhotoEntity photoEntity = photoDao.getByPhotoId(photoId);

        if (selectedImageFormat == 0 || selectedImageFormat == -1) {
            contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, photoEntity.getPhotoName() + ".jpg");
        } else if (selectedImageFormat == 1) {
            contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, photoEntity.getPhotoName() + ".png");
        } else if (selectedImageFormat == 2) {
            contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, photoEntity.getPhotoName() + ".webp");
        }
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "images/*");
        Uri uri = contentResolver.insert(images, contentValues);
        try {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) imageViewAfter.getDrawable();
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
            Toast.makeText(DenoiseActivity.this, "Image saved successfully", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(DenoiseActivity.this, "Image not saved", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}