package com.ideas.stickermaker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.ideas.stickermaker.constant.Constant;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImageManipulation {

    public static Bitmap newBitmap;

    public static Uri convertImageToWebP(Uri uri, String StickerBookId, String StickerId, Context context) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
            dirChecker(context.getFilesDir() + "/" + StickerBookId);
            String path = context.getFilesDir() + "/" + StickerBookId + "/" + StickerBookId + "-" + StickerId + ".webp";
            makeSmallestBitmapCompatible(path, bitmap);
            return Uri.fromFile(new File(path));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return uri;
    }

    public static void imageUrlToWebP() {
        try {
            new MyAsyncTask().execute(Constant.DEFAULT_IMAGE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class MyAsyncTask extends AsyncTask<String, Void, Bitmap> {

        protected Bitmap doInBackground(String... params) {
            try {
                URL url = new URL(params[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                return myBitmap;
            } catch (IOException e) {
                return null;
            }
        }

        protected void onPostExecute(Bitmap result) {
            newBitmap = result;
            Bitmap image = newBitmap;
            makeSmallestBitmapCompatible(getFilePath(), image);
        }
    }

    private static String getFilePath() {
        File newFolder = null;
        String fileName = "StickerList-" + System.currentTimeMillis() + ".webp";

        if (!(Build.VERSION.SDK_INT >= 19)) {
            newFolder = new File(Environment.getExternalStorageDirectory(), "/ImageEraser/CustomSticker/");
        } else if (Environment.getExternalStorageState().equals("mounted")) {
            newFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "/ImageEraser/CustomSticker/");
        }
        if (!newFolder.exists()) {
            newFolder.mkdirs();
        }
        File fileNamePath = new File(newFolder, fileName);
        return fileNamePath.toString();
    }
    
    public static Uri convertIconTrayToWebP(Uri uri, String StickerBookId, String StickerId, Context context) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
            dirChecker(context.getFilesDir() + "/" + StickerBookId);
            String path = context.getFilesDir() + "/" + StickerBookId + "/" + StickerBookId + "-" + StickerId + ".webp";

            Log.w("Conversion Data: ", "path: " + path);

            FileOutputStream out = new FileOutputStream(path);
            bitmap = Bitmap.createScaledBitmap(bitmap, 256, 256, true);
            bitmap.compress(Bitmap.CompressFormat.WEBP, 100, out); //100-best quality
            out.close();
            return Uri.fromFile(new File(path));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return uri;
    }

    private static void dirChecker(String dir) {
        File f = new File(dir);
        if (!f.isDirectory()) {
            f.mkdirs();
        }
        Log.e("StickerPath:-", f + "");
    }

    private static byte[] getByteArray(Bitmap bitmap, int quality) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.WEBP,
                quality,
                bos);

        return bos.toByteArray();
    }

    private static void makeSmallestBitmapCompatible(String path, Bitmap bitmap) {
        int quality = 100;
        FileOutputStream outs = null;
        try {
            outs = new FileOutputStream(path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        bitmap = Bitmap.createScaledBitmap(bitmap, 512, 512, true);

        int byteArrayLength = 100000;
        ByteArrayOutputStream bos = null;

        while ((byteArrayLength / 1000) >= 100) {
            bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.WEBP, quality, bos);
            byteArrayLength = bos.toByteArray().length;
            quality -= 10;
        }
        try {
            outs.write(bos.toByteArray());
            outs.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
