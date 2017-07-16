package com.timkonieczny.rss;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.View;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

class Article implements ImageListener{

    Date published;
    String title, author, headerImage, link, content;
    Bitmap headerImageBitmap = null;
    Palette colorPalette;
    Source source;
    View.OnClickListener onClickListener;
    Drawable[] inlineImagesDrawables;
    String[] inlineImages;
    String[] inlineImagesFileNames;
    UpdateImageListener[] updateImageListeners;
    UpdateImageListener updateImageListener;
    UpdateHeaderImageListener updateHeaderImageListener;

    private UpdateHeaderImageTask task;

    void updateHeaderImage() {
        task = new UpdateHeaderImageTask();
        task.execute(this);
    }

    void setUpdateHeaderImageListener(UpdateHeaderImageListener listener){
        updateHeaderImageListener = listener;
    }

    void getImageDrawables(UpdateImageListener listener, Resources resources, Context context){
        updateImageListener = listener;
        for(int i = 0; i < inlineImages.length; i++){
            if(inlineImagesDrawables[i] != null){
                Log.d("Article", "loading inli1ne image from drawable");
                listener.onImageUpdated(inlineImagesDrawables[i], i);
            }else if(inlineImagesFileNames[i] != null){
                Log.d("Article", "loading inline image from internal storage");
                listener.onImageUpdated(loadImageFromInternalStorage(context, inlineImagesFileNames[i]), i);
            }else {
                try {
                    Log.d("Article", "loading inline image from internet");
                    UpdateImageTask updateImageTask = new UpdateImageTask(this, i, resources, context, this);
//                    UpdateImageTask updateImageTask = new UpdateImageTask(listener, i, resources, context, this);
                    updateImageTask.execute(new URL(inlineImages[i]));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Drawable loadImageFromInternalStorage(Context context, String imageFileName){
        try {
            FileInputStream fileInputStream = context.openFileInput(imageFileName);
            Bitmap bitmap = BitmapFactory.decodeStream(fileInputStream);
            fileInputStream.close();
            return new BitmapDrawable(context.getResources(), bitmap);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String toString(){
        return "Title: " + title +
                "\nAuthors: " + author +
                "\nLink: " + link +
                "\nheaderImage: " + headerImage +
                "\nPublished: " + published.toString();
    }

    @Override
    public void onImageLoaded(int index, Drawable drawable) {
        if(index==-1) updateHeaderImageListener.onHeaderImageUpdated(this);
        else updateImageListener.onImageUpdated(drawable, index);
    }
}
