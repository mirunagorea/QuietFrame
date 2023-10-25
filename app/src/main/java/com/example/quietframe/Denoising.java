package com.example.quietframe;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.photo.Photo;

public class Denoising {

    public static byte[] nonLocalMeansDenoising(byte[] photoData){
        MatOfByte matOfByte = new MatOfByte(photoData);
        Mat inputImage = Imgcodecs.imdecode(matOfByte, Imgcodecs.IMREAD_COLOR);

        Mat denoisedImage = new Mat();
        Photo.fastNlMeansDenoisingColored(inputImage, denoisedImage, 10, 10);

        MatOfByte matOfByteDenoised = new MatOfByte();
        Imgcodecs.imencode(".jpg", denoisedImage, matOfByteDenoised);
        byte[] denoisedPhotoData = matOfByteDenoised.toArray();
        return denoisedPhotoData;
    }
}
