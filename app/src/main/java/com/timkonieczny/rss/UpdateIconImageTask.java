package com.timkonieczny.rss;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

class UpdateIconImageTask extends AsyncTask<Source, Void, Source> {

    private UpdateIconImageListener updateIconImageListener;
    private Context context;

    UpdateIconImageTask(UpdateIconImageListener updateIconImageListener, Context context){
        this.updateIconImageListener = updateIconImageListener;
        this.context = context;
    }

    @Override
    protected Source doInBackground(Source... params) {
        Bitmap image;
        try {
            InputStream stream = (new URL(params[0].icon)).openStream();
            image = BitmapFactory.decodeStream(stream);
            params[0].iconBitmap = image;
            params[0].iconDrawable = new BitmapDrawable( context.getResources(), image);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return params[0];
    }

    @Override
    protected void onPostExecute(Source source) {
        super.onPostExecute(source);
        updateIconImageListener.onIconImageUpdated(source);
    }
}
