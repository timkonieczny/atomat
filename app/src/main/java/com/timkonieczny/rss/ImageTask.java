package com.timkonieczny.rss;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

class ImageTask extends AsyncTask<Image, Void, Void> {

    private Context context;
    ImageListener imageListener;
    private int index;
    private String fileName;
    private long parentDbId;

    ImageTask(Context context, int index, String fileName, long parentDbId){
        this.index = index;
        this.context = context;
        this.fileName = fileName;
        this.parentDbId = parentDbId;
    }

    @Override
    protected Void doInBackground(Image... images) {
        try {
            InputStream stream = (new URL(images[0].url)).openStream();
            Bitmap bitmap = BitmapFactory.decodeStream(stream);

            saveImageInInternalStorage(bitmap);
            images[0].fileName = fileName;
            images[0].width = bitmap.getWidth();
            images[0].height = bitmap.getHeight();
            saveImagePathInDb(parentDbId, images[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void v) {
        super.onPostExecute(v);
        if(imageListener != null) imageListener.onImageLoaded(index);
    }

    private void saveImageInInternalStorage(Bitmap bitmap) throws IOException {
        FileOutputStream fileOutputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
        fileOutputStream.close();
    }

    private void saveImagePathInDb(long parentDbId, Image image){
        DbManager dbManager = new DbManager(context);
        if(index == Image.TYPE_ICON){
            dbManager.updateValue(DbManager.SourcesTable.TABLE_NAME,
                    DbManager.SourcesTable.COLUMN_NAME_ICON_PATH, image.fileName,
                    DbManager.SourcesTable._ID, String.valueOf(parentDbId));
        }else if(index == Image.TYPE_HEADER){
            ContentValues values = new ContentValues();
            values.put(DbManager.ImagesTable.COLUMN_NAME_PATH, image.fileName);
            values.put(DbManager.ImagesTable.COLUMN_NAME_WIDTH, image.width);
            values.put(DbManager.ImagesTable.COLUMN_NAME_HEIGHT, image.height);
            dbManager.updateImage(parentDbId, index, values); // header image exists in db already
        }else if(index == Image.TYPE_INLINE){
            ContentValues values = new ContentValues();
            values.put(DbManager.ImagesTable.COLUMN_NAME_PATH, image.fileName);
            values.put(DbManager.ImagesTable.COLUMN_NAME_WIDTH, image.width);
            values.put(DbManager.ImagesTable.COLUMN_NAME_HEIGHT, image.height);

            values.put(DbManager.ImagesTable.COLUMN_NAME_TYPE, index);
            values.put(DbManager.ImagesTable.COLUMN_NAME_ARTICLE_ID, parentDbId);
            values.put(DbManager.ImagesTable.COLUMN_NAME_URL, image.url);
            image.dbId = dbManager.insertImage(values);
        }
        dbManager.close();
    }
}