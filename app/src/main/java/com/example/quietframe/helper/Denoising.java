package com.example.quietframe.helper;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.photo.Photo;
import org.opencv.imgproc.Imgproc;

public class Denoising {

    public static class DenoisedResult{
        public byte[] denoisedPhotoData;
        public double psnrValue;
        public double ssimValue;

        public DenoisedResult(byte[] denoisedPhotoData, double psnrValue, double ssimValue) {
            this.denoisedPhotoData = denoisedPhotoData;
            this.psnrValue = psnrValue;
            this.ssimValue = ssimValue;
        }
    }

    public static DenoisedResult nonLocalMeansDenoising(byte[] photoData){
        MatOfByte matOfByte = new MatOfByte(photoData);
        Mat inputImage = Imgcodecs.imdecode(matOfByte, Imgcodecs.IMREAD_COLOR);

        Mat denoisedImage = new Mat();
        Photo.fastNlMeansDenoisingColored(inputImage, denoisedImage, 10, 10);

        MatOfByte matOfByteDenoised = new MatOfByte();
        Imgcodecs.imencode(".jpg", denoisedImage, matOfByteDenoised);
        byte[] denoisedPhotoData = matOfByteDenoised.toArray();

        double psnrValue = calculatePSNR(inputImage, denoisedImage);
        double ssimValue = calculateSSIM(inputImage, denoisedImage);
        return new DenoisedResult(denoisedPhotoData, psnrValue, ssimValue);
    }

    public static double calculateSSIM(Mat inputImage, Mat denoisedImage) {
        Mat inputImgGray = new Mat();
        Mat denoisedImgGray = new Mat();

        Imgproc.cvtColor(inputImage, inputImgGray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.cvtColor(denoisedImage, denoisedImgGray, Imgproc.COLOR_BGR2GRAY);

        Mat ssimMap = new Mat();
        Imgproc.matchTemplate(inputImgGray, denoisedImgGray, ssimMap, Imgproc.TM_CCOEFF_NORMED);

        Core.MinMaxLocResult result = Core.minMaxLoc(ssimMap);

        return result.maxVal; // The higher the value, the more similar the images
    }

    public static double calculatePSNR(Mat inputImage, Mat denoisedImage) {
        // Convert Mat to array
        byte[] originalBytes = new byte[(int) (inputImage.total() * inputImage.channels())];
        inputImage.get(0, 0, originalBytes);

        byte[] denoisedBytes = new byte[(int) (denoisedImage.total() * denoisedImage.channels())];
        denoisedImage.get(0, 0, denoisedBytes);

        // Calculate PSNR
        double mse = 0;
        for (int i = 0; i < originalBytes.length; i++) {
            mse += Math.pow((originalBytes[i] & 0xFF) - (denoisedBytes[i] & 0xFF), 2);
        }
        mse /= originalBytes.length;

        double maxPixelValue = 255.0;
        double psnr = 10 * Math.log10((maxPixelValue * maxPixelValue) / mse);

        return psnr;
    }

    public static byte[] medianFilterDenoising(byte[] photoData, int kernelSize) {
        MatOfByte matOfByte = new MatOfByte(photoData);
        Mat inputImage = Imgcodecs.imdecode(matOfByte, Imgcodecs.IMREAD_COLOR);

        Mat denoisedImage = new Mat();
        Imgproc.medianBlur(inputImage, denoisedImage, 3);

        MatOfByte matOfByteDenoised = new MatOfByte();
        Imgcodecs.imencode(".jpg", denoisedImage, matOfByteDenoised);
        byte[] denoisedPhotoData = matOfByteDenoised.toArray();
        return denoisedPhotoData;
    }
}
