package com.timkonieczny.rss;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.graphics.Palette;

import java.io.FileInputStream;
import java.io.IOException;

class Source implements ImageListener{

    String title, icon, iconFileName, rssUrl, link;
    Bitmap iconBitmap;
    Drawable iconDrawable;
    Palette colorPalette;

    private Resources resources;
    private Context context;

    private UpdateIconImageTask task;
    private UpdateIconImageListener updateIconImageListener;

    Source(Resources resources, Context context, String rssUrl){
        title = null;
        icon = null;
        link = null;
        iconBitmap = null;
        iconDrawable = null;
        colorPalette = null;
        this.rssUrl = rssUrl;
        this.resources = resources;
        this.context = context;
        task = null;
    }

    Source(Resources resources, Context context, String rssUrl, String title, String link, String icon, String iconFileName){
        this.title = title;
        this.link = link;
        this.icon = icon;
        this.iconFileName = iconFileName;
        iconBitmap = null;
        iconDrawable = null;
        colorPalette = null;
        this.rssUrl = rssUrl;
        this.resources = resources;
        this.context = context;
        task = null;
    }

    Drawable getIconDrawable(UpdateIconImageListener listener){
        if(iconDrawable != null) return iconDrawable;
        else if(iconFileName!=null){
            loadIconFromInternalStorage();
            return iconDrawable;
        }else if(icon != null){
            if(task == null){
                iconFileName = (title.replaceAll("[^a-zA-Z_0-9]", "")+System.currentTimeMillis()).toLowerCase()+".jpg";
                task = new UpdateIconImageTask(resources, context, this, iconFileName);
                task.execute(this);
            }
            updateIconImageListener = listener;
        }
        return null;
    }

    private void loadIconFromInternalStorage(){
        try {
            FileInputStream fileInputStream = context.openFileInput(iconFileName);
            iconBitmap = BitmapFactory.decodeStream(fileInputStream);
            fileInputStream.close();
            iconDrawable = new BitmapDrawable(context.getResources(), iconBitmap);
            colorPalette = (new Palette.Builder(iconBitmap)).generate();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString(){
        return "Title:\t\t\t"+title+
            "\nIcon:\t\t\t\t"+icon+
            "\nLink:\t\t\t\t"+ link+
            "\nIcon Bitmap:\t\t"+(iconBitmap != null)+
            "\nIcon Drawable:\t"+(iconDrawable != null)+
            "\nIcon File Name:\t"+iconFileName+
            "\nRSS URL:\t\t\t"+rssUrl;
    }

    @Override
    public void onImageLoaded(int index, Drawable drawable) {
        updateIconImageListener.onIconImageUpdated(this);
    }
}