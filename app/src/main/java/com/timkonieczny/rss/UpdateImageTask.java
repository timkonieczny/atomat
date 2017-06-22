package com.timkonieczny.rss;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

class UpdateImageTask extends AsyncTask<URL, Void, Drawable> {

    private Bitmap image;
    private UpdateImageListener listener;
    private int imageSpanIndex;
    private Resources resources;

    UpdateImageTask(UpdateImageListener listener, int imageSpanIndex, Resources resources){
        this.listener = listener;
        this.imageSpanIndex = imageSpanIndex;
        this.resources = resources;
    }

    @Override
    protected Drawable doInBackground(URL... urls) {
        try {
            InputStream stream = (urls[0]).openStream();
            image = BitmapFactory.decodeStream(stream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new BitmapDrawable(resources, image);
    }

    @Override
    protected void onPostExecute(Drawable drawable) {
        super.onPostExecute(drawable);
        if(listener != null) listener.onImageUpdated(drawable, imageSpanIndex);
    }
}
