package com.github.chagall.notificationlistenerexample;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
}
