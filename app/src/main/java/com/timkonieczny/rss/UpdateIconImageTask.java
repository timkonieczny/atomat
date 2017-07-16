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

class UpdateIconImageTask extends AsyncTask<Source, Void, Source> {

//    UpdateIconImageListener updateIconImageListener = null;
    private Resources resources;
    private Context context;
    private ImageListener imageListener;
    private String fileName;

    UpdateIconImageTask(Resources resources, Context context, ImageListener imageListener, String fileName){
        this.resources = resources;
        this.context = context;
        this.imageListener = imageListener;
        this.fileName = fileName;
    }

    @Override
    protected Source doInBackground(Source... sources) {
        try {
            InputStream stream = (new URL(sources[0].icon)).openStream();
            sources[0].iconBitmap = BitmapFactory.decodeStream(stream);
            sources[0].iconDrawable = new BitmapDrawable(resources, sources[0].iconBitmap);
            sources[0].colorPalette = (new Palette.Builder(sources[0].iconBitmap)).generate();

            saveImageInInternalStorage(sources[0].iconBitmap);

            // TODO: create POJO image class. pass that class instead of source
            // save icon file name in db
            MainActivity.dbManager.updateValue(DbManager.SourcesTable.TABLE_NAME,
                    DbManager.SourcesTable.COLUMN_NAME_ICON_FILE, sources[0].iconFileName,
                    DbManager.SourcesTable.COLUMN_NAME_URL, sources[0].rssUrl);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return sources[0];
    }

    @Override
    protected void onPostExecute(Source source) {
        super.onPostExecute(source);
        imageListener.onImageLoaded(0, null);
    }

    private void saveImageInInternalStorage(Bitmap bitmap) throws IOException {
        FileOutputStream fileOutputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
        fileOutputStream.close();
    }
}
