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
import java.net.URL;
import java.util.Date;

class Source {

    String title, icon, id, iconFileName;
    URL link;
    Bitmap iconBitmap;
    Drawable iconDrawable;
    Palette colorPalette;
    Date updated;
    boolean isStub;
    private String rssUrl;

    private Resources resources;
    private Context context;

    private UpdateIconImageTask task;

    Source(Resources resources, Context context, String url){
        title = null;
        icon = null;
        id = null;
        link = null;
        iconBitmap = null;
        iconDrawable = null;
        colorPalette = null;
        updated = null;
        isStub = true;
        rssUrl = url;
        this.resources = resources;
        this.context = context;
    }

    void updateIconImage(){
        task = new UpdateIconImageTask(resources, context, rssUrl);
        task.execute(this);
    }

    void loadIconFromInternalStorage(){
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

    void setUpdateIconImageListener(UpdateIconImageListener listener){
        task.updateIconImageListener = listener;
    }

    @Override
    public String toString(){
        return "Title:\t\t\t"+title+
            "\nIcon:\t\t\t\t"+icon+
            "\nID:\t\t\t\t"+id+
            "\nLink:\t\t\t\t"+ ((link==null) ? "null" : link.toString())+
            "\nIcon Bitmap:\t\t"+(iconBitmap != null)+
            "\nIcon Drawable:\t"+(iconDrawable != null)+
            "\nIcon File Name:\t"+iconFileName+
            "\nisStub:\t\t\t"+isStub+
            "\nRSS URL:\t\t\t"+rssUrl+
            "\nUpdated:\t\t\t"+((updated==null) ? "null" : updated.toString());
    }
}