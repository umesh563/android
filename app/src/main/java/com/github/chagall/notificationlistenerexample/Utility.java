package com.github.chagall.notificationlistenerexample;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.provider.Settings;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;

import static android.os.Environment.DIRECTORY_PICTURES;

/**
 * Created by shadman on 24/8/17.
 */

public class Utility {

    private static final String TAG = "Utility";

    public static <T> T getFieldValue(Object obj, String fieldName) {
        Class<?> objClass = obj.getClass();
        while ( objClass != null ) {
            try {
                Field field = objClass.getDeclaredField(fieldName);
                field.setAccessible(true);
                return (T) field.get(obj);
            } catch ( NoSuchFieldException e ) {
                e.printStackTrace();
                objClass = objClass.getSuperclass();
            } catch ( IllegalAccessException e ) {
                e.printStackTrace();
                return null;
            }
        }

        return null;
    }

    public static Class<?> getClassFromName(String name) {
        try {
            return Class.forName(name);
        } catch ( ClassNotFoundException e ) {
            e.printStackTrace();
            return null;
        }
    }

    public static float convertDpToPixel(float dp, Context context) {
        if (context == null) {
            Log.e(TAG, "Context is null");
            return 0;
        }

        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return dp * (metrics.densityDpi / 160f);
    }

    public static Bitmap getBitmapFromView(View view, Context context) {
        int specWidth = View.MeasureSpec.makeMeasureSpec((int) convertDpToPixel(360, context),
                View.MeasureSpec.AT_MOST);
        int specHeight = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        view.measure(specWidth, specHeight);

        Bitmap returnedBitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);

        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.draw(canvas);

        return returnedBitmap;
    }

    public static String saveBitmapToFile(Context context, Bitmap bitmap, String name) {
        if ( bitmap == null ) {
            return null;
        }

        File file = context.getExternalFilesDir(DIRECTORY_PICTURES);
        if ( file == null ) {
            throw new RuntimeException("Unable to create external storage file");
        }

        File imgFile = new File(file, name + ".jpeg");

        try {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, new FileOutputStream(imgFile));
        } catch ( FileNotFoundException e ) {
            e.printStackTrace();
        }

        return imgFile.getAbsolutePath();
    }

    public static String convertToBase64(String path) {
        if ( path == null ) {
            Log.e(TAG, "convertToBase64() path is null");
            return null;
        }

        File file = new File(path);
        if ( !file.exists() || file.isDirectory() ) {
            Log.e(TAG, "File " + file + " does not exist or is a directory");
            return null;
        }

        long length = file.length();
        byte[] data = new byte[(int) length];
        FileInputStream stream =  null;

        try {
            stream = new FileInputStream(file);
            int readSize = stream.read(data, 0, (int) length);
        } catch ( IOException e ) {
            e.printStackTrace();
        } finally {
            if ( stream != null ) {
                try {
                    stream.close();
                } catch ( IOException e ) {
                    e.printStackTrace();
                }
            }
        }

        return Base64.encodeToString(data, Base64.DEFAULT);
    }

    @SuppressLint("HardwareIds")
    public static String getAndroidDeviceId(Context context) {
        String deviceId = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        Log.d(TAG, "Device ID is - " + deviceId);
        return deviceId;
    }
}
