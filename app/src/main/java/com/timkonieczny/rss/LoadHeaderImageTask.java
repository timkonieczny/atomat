package com.timkonieczny.rss;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;

class LoadHeaderImageTask extends AsyncTask<Article, Void, Article> {

    private LoadHeaderImageListener loadHeaderImageListener;

    LoadHeaderImageTask(LoadHeaderImageListener loadHeaderImageListener){
        this.loadHeaderImageListener = loadHeaderImageListener;
    }

    @Override
    protected Article doInBackground(Article... params) {
        Bitmap image = null;
        try {
            InputStream stream = params[0].headerImage.openStream();
            image = BitmapFactory.decodeStream(stream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        params[0].headerImageBitmap = image;
        return params[0];
    }

    @Override
    protected void onPostExecute(Article article) {
        super.onPostExecute(article);
        loadHeaderImageListener.onImageLoaded(article);
    }
}
