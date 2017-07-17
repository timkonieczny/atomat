package com.timkonieczny.rss;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.support.v7.graphics.Palette;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

class ImageTask extends AsyncTask<Image, Void, Void> {

    private Context context;
    private Resources resources;
    ImageListener imageListener;
    private int index;
    private String fileName;

    ImageTask(Context context, Resources resources, int index, String fileName){
        this.index = index;
        this.resources = resources;
        this.context = context;
        this.fileName = fileName;
    }

    @Override
    protected Void doInBackground(Image... images) {
        try {
            InputStream stream = (new URL(images[0].url)).openStream();
            Bitmap bitmap = BitmapFactory.decodeStream(stream);

            images[0].drawable = new BitmapDrawable(resources, bitmap);
            images[0].palette = (new Palette.Builder(bitmap)).generate();

            saveImageInInternalStorage(bitmap);
            images[0].fileName = fileName;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void v) {
        super.onPostExecute(v);
        if(imageListener != null) imageListener.onImageLoaded(index);
    }

    private void saveImageInInternalStorage(Bitmap bitmap) throws IOException {
        FileOutputStream fileOutputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
        fileOutputStream.close();
    }
}