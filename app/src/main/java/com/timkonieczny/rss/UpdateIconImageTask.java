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

    UpdateIconImageListener updateIconImageListener = null;
    private Resources resources;
    private Context context;

    UpdateIconImageTask(Resources resources, Context context){
        this.resources = resources;
        this.context = context;
    }

    @Override
    protected Source doInBackground(Source... sources) {
        try {
            InputStream stream = (new URL(sources[0].icon)).openStream();
            sources[0].iconBitmap = BitmapFactory.decodeStream(stream);
            sources[0].iconDrawable = new BitmapDrawable(resources, sources[0].iconBitmap);
            sources[0].colorPalette = (new Palette.Builder(sources[0].iconBitmap)).generate();

            saveImageInInternalStorage(sources[0]);

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
        if(updateIconImageListener!=null) updateIconImageListener.onIconImageUpdated(source);
    }

    private void saveImageInInternalStorage(Source source) throws IOException {
        source.iconFileName = (source.title.replaceAll("[^a-zA-Z_0-9]", "")+System.currentTimeMillis()).toLowerCase()+".jpg";
        FileOutputStream fileOutputStream = context.openFileOutput(source.iconFileName, Context.MODE_PRIVATE);
        source.iconBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
        fileOutputStream.close();
    }
}
