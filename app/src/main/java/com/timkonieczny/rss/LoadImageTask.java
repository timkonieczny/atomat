package com.timkonieczny.rss;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

class LoadImageTask extends AsyncTask<URL, Void, Bitmap> {

    private LoadImageListener loadImageListener;

    LoadImageTask(LoadImageListener loadImageListener){
        this.loadImageListener = loadImageListener;
    }

    @Override
    protected Bitmap doInBackground(URL... params) {
        Bitmap image = null;
        try {
            InputStream stream = params[0].openStream();
            image = BitmapFactory.decodeStream(stream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        loadImageListener.onImageLoaded(bitmap);
    }
}
