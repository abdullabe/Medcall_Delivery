package com.yoodobuzz.medcalldelivery.utils;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class FileUtils
    {


        private static final int DEFAULT_MIN_WIDTH_QUALITY = 400;        // min pixels
        private static final int DEFAULT_MIN_HEIGHT_QUALITY = 400;        // min pixels

        private static int minWidthQuality = DEFAULT_MIN_WIDTH_QUALITY;
        private static int minHeightQuality = DEFAULT_MIN_HEIGHT_QUALITY;


        public static Bitmap decodeBitmap(Context context, Uri theUri) {
        Bitmap outputBitmap = null;
        AssetFileDescriptor fileDescriptor = null;

        try {
            fileDescriptor = context.getContentResolver().openAssetFileDescriptor(theUri, "r");

            // Get size of bitmap file
            BitmapFactory.Options boundsOptions = new BitmapFactory.Options();
            boundsOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFileDescriptor(fileDescriptor.getFileDescriptor(), null, boundsOptions);

            // Get desired sample size. Note that these must be powers-of-two.
            int[] sampleSizes = new int[]{8, 4, 2, 1};
            int selectedSampleSize = 1; // 1 by default (original image)

            for (int sampleSize : sampleSizes) {
                selectedSampleSize = sampleSize;
                int targetWidth = boundsOptions.outWidth / sampleSize;
                int targetHeight = boundsOptions.outHeight / sampleSize;
                if (targetWidth >= minWidthQuality && targetHeight >= minHeightQuality) {
                    break;
                }
            }

            // Decode bitmap at desired size
            BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
            decodeOptions.inSampleSize = selectedSampleSize;
            outputBitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor.getFileDescriptor(), null, decodeOptions);
            if (outputBitmap != null) {
             /*   Log.i(TAG, "Loaded image with sample size " + decodeOptions.inSampleSize + "\t\t"
                        + "Bitmap width: " + outputBitmap.getWidth()
                        + "\theight: " + outputBitmap.getHeight());*/
            }
            fileDescriptor.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return outputBitmap;
    }

        public static byte[] getFileDataFromDrawable(Bitmap bitmap) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 80, byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        }

        public static boolean isExcelFile(String path)
        {
            String[] types = {"xls","xlsx"};
            return FilePickerUtils.contains(types, path);
        }

        public static boolean isDocFile(String path)
        {
            String[] types = {"doc","docx", "dot","dotx"};
            return FilePickerUtils.contains(types, path);
        }

        public static boolean isPPTFile(String path)
        {
            String[] types = {"ppt","pptx"};
            return FilePickerUtils.contains(types, path);
        }

        public static boolean isPDFFile(String path)
        {
            String[] types = {"pdf"};
            return FilePickerUtils.contains(types, path);
        }

        public static boolean isTxtFile(String path)
        {
            String[] types = {"txt"};
            return FilePickerUtils.contains(types, path);
        }

    }


