package com.timkonieczny.rss;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

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
        Bitmap image = null;
        try {
            InputStream stream = (new URL(params[0].headerImage)).openStream();
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
        updateHeaderImageListener.onHeaderImageUpdated(article);
    }
}
