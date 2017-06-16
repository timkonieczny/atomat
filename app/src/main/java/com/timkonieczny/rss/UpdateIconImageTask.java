package com.timkonieczny.rss;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.support.v7.graphics.Palette;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

class UpdateIconImageTask extends AsyncTask<Source, Void, Source> {

    UpdateIconImageListener updateIconImageListener = null;
    private Resources resources;

    UpdateIconImageTask(Resources resources){
        this.resources = resources;
    }

    @Override
    protected Source doInBackground(Source... params) {
        Bitmap image;
        try {
            InputStream stream = (new URL(params[0].icon)).openStream();
            image = BitmapFactory.decodeStream(stream);
            params[0].iconBitmap = image;
            params[0].iconDrawable = new BitmapDrawable(resources, image);
            params[0].colorPalette = (new Palette.Builder(params[0].iconBitmap)).generate();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return params[0];
    }

    @Override
    protected void onPostExecute(Source source) {
        super.onPostExecute(source);
        if(updateIconImageListener!=null) updateIconImageListener.onIconImageUpdated(source);
    }
}
