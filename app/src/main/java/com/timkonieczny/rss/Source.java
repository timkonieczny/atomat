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

class Source {

    String title, icon, iconFileName, rssUrl, link;
    Bitmap iconBitmap;
    Drawable iconDrawable;
    Palette colorPalette;

    private Resources resources;
    private Context context;

    private UpdateIconImageTask task;

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

    Source(Resources resources, Context context, String rssUrl, String title, String icon, /*String id,*/ String link/*, Date updated*/){
        this.title = title;
        this.icon = icon;
        this.link = link;
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
                task = new UpdateIconImageTask(resources, context);
                task.execute(this);
            }
            task.updateIconImageListener = listener;
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
}