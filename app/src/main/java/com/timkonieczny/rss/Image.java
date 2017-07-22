package com.timkonieczny.rss;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.graphics.Palette;

import java.io.File;

class Image extends DbRow{
    String url, fileName, absolutePath;
    Drawable drawable;
    Palette palette;
    private ImageTask imageTask;
    int width, height;

    Drawable getDrawable(Context context, ImageListener imageListener, String fileNameSeed, int index){
        if(drawable != null){
            return drawable;
        }else if(this.fileName !=null){
            loadFromInternalStorage(context);
            return drawable;
        }else if(url != null){
            if(imageTask == null) {
                imageTask = new ImageTask(context, index, generateFileName(fileNameSeed));
                imageTask.execute(this);
            }
            imageTask.imageListener = imageListener;
        }
        return null;
    }

    private void loadFromInternalStorage(Context context){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 8;
        Bitmap bitmap = BitmapFactory.decodeFile(
                (new File(context.getFilesDir(), fileName)).getAbsolutePath(), options);
        drawable = new BitmapDrawable(context.getResources(), bitmap);
        palette = (new Palette.Builder(bitmap)).generate();
    }

    private String generateFileName(String name){
        String fileName = name.replaceAll("[^a-zA-Z_0-9]", "").toLowerCase();
        if(fileName.length()>10) fileName = fileName.substring(0, 10);
        fileName += "_" + System.currentTimeMillis() + ".jpg";
        return fileName;
    }
}