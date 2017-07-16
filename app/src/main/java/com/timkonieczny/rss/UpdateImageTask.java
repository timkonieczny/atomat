package com.timkonieczny.rss;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

class UpdateImageTask extends AsyncTask<URL, Void, Drawable> {

    private ImageListener listener;
    private int imageSpanIndex;
    private Resources resources;
    private Context context;
    private Article article;

    UpdateImageTask(ImageListener listener, int imageSpanIndex, Resources resources, Context context, Article article){
        this.listener = listener;
        this.imageSpanIndex = imageSpanIndex;
        this.resources = resources;
        this.context = context;
        this.article = article;
    }

    @Override
    protected Drawable doInBackground(URL... urls) {
        try {
            InputStream stream = (urls[0]).openStream();
            Bitmap image = BitmapFactory.decodeStream(stream);

            article.inlineImagesFileNames[imageSpanIndex] = (article.title.replaceAll("[^a-zA-Z_0-9]", "")+System.currentTimeMillis()).toLowerCase()+".jpg";
            saveImageInInternalStorage(image, article.inlineImagesFileNames[imageSpanIndex]);

//            MainActivity.dbManager.saveInlineImageUrl(article.inlineImages[imageSpanIndex], article.link);
//            MainActivity.dbManager.saveInlineImageFile(article.inlineImagesFileNames[imageSpanIndex], article.link);
            MainActivity.dbManager.appendString(article.inlineImages[imageSpanIndex], article.link, DbManager.ArticlesTable.COLUMN_NAME_INLINE_IMAGES);
            MainActivity.dbManager.appendString(article.inlineImagesFileNames[imageSpanIndex], article.link, DbManager.ArticlesTable.COLUMN_NAME_INLINE_IMAGES_FILES);

            return new BitmapDrawable(resources, image);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(Drawable drawable) {
        super.onPostExecute(drawable);
        if(listener != null) listener.onImageLoaded(imageSpanIndex, drawable);
    }

    private void saveImageInInternalStorage(Bitmap bitmap, String fileName) throws IOException {
        FileOutputStream fileOutputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
        fileOutputStream.close();
    }
}
