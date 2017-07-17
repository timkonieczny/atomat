package com.timkonieczny.rss;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.graphics.Palette;
import android.util.Log;

import java.io.FileInputStream;
import java.io.IOException;

class Image{
    String url, fileName;
    Drawable drawable;
    Palette palette;
    private ImageTask imageTask;

    Drawable getDrawable(Context context, Resources resources, ImageListener imageListener, String fileNameSeed, int index){
        if(drawable != null){
            Log.d("Image", "loading from drawable");
            return drawable;
        }else if(this.fileName !=null){
            Log.d("Image", "loading from internal storage");
            loadFromInternalStorage(context);
            return drawable;
        }else if(url != null){
            Log.d("Image", "loading from web");
            if(imageTask == null) {
                imageTask = new ImageTask(context, resources, index, generateFileName(fileNameSeed));
                imageTask.execute(this);
            }
            imageTask.imageListener = imageListener;
        }
        return null;
    }

    private void loadFromInternalStorage(Context context){
        try {
            FileInputStream fileInputStream = context.openFileInput(fileName);
            Bitmap bitmap = BitmapFactory.decodeStream(fileInputStream);
            fileInputStream.close();

            drawable = new BitmapDrawable(context.getResources(), bitmap);
            palette = (new Palette.Builder(bitmap)).generate();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String generateFileName(String name){
        String fileName = name.replaceAll("[^a-zA-Z_0-9]", "").toLowerCase();
        if(fileName.length()>10) fileName = fileName.substring(0, 10);
        fileName += "_" + System.currentTimeMillis() + ".jpg";
        return fileName;
    }
}