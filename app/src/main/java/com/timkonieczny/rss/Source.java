package com.timkonieczny.rss;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import java.net.URL;
import java.util.Date;

class Source {

    String title, icon, id;
    URL link;
    Bitmap iconBitmap;
    Drawable iconDrawable;
    Date updated;
    boolean isStub;

    private Resources resources;

    private UpdateIconImageTask task;

    Source(Resources resources){
        title = null;
        icon = null;
        id = null;
        link = null;
        iconBitmap = null;
        iconDrawable = null;
        updated = null;
        isStub = true;
        this.resources = resources;
    }

    void updateIconImage(){
        task = new UpdateIconImageTask(resources);
        task.execute(this);
    }

    void setUpdateIconImageListener(UpdateIconImageListener listener){
        task.updateIconImageListener = listener;
    }

    @Override
    public String toString(){
        return "Title:\t\t\t"+title+
            "\nIcon:\t\t\t"+icon+
            "\nID:\t\t\t\t"+id+
            "\nLink:\t\t\t"+link.toString()+
            "\nIcon Bitmap:\t"+(iconBitmap != null)+
            "\nIcon Drawable:\t"+(iconDrawable != null)+
            "\nUpdated:\t\t"+updated.toString();
    }

}
