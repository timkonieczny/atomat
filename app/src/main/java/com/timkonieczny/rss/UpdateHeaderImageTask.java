package com.timkonieczny.rss;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.graphics.Palette;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

class UpdateHeaderImageTask extends AsyncTask<Article, Void, Article> {

    private UpdateHeaderImageListener updateHeaderImageListener;

    UpdateHeaderImageTask(UpdateHeaderImageListener updateHeaderImageListener){
        this.updateHeaderImageListener = updateHeaderImageListener;
    }

    @Override
    protected Article doInBackground(Article... params) {
        Bitmap image;
        try {
            InputStream stream = (new URL(params[0].headerImage)).openStream();
            image = BitmapFactory.decodeStream(stream);
            params[0].headerImageBitmap = image;
            params[0].colorPalette = (new Palette.Builder(params[0].headerImageBitmap)).generate();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return params[0];
    }

    @Override
    protected void onPostExecute(Article article) {
        super.onPostExecute(article);
        updateHeaderImageListener.onHeaderImageUpdated(article);
    }
}
