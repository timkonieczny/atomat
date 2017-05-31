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
    private UpdateIconImageListener updateIconImageListener;
    private Resources resources;

    Source(UpdateIconImageListener updateIconImageListener, Resources resources){
        this.updateIconImageListener = updateIconImageListener;
        this.resources = resources;
        title = null;
        icon = null;
        id = null;
        link = null;
        iconBitmap = null;
        iconDrawable = null;
        updated = null;
    }

    void updateIconImage(){
        (new UpdateIconImageTask(updateIconImageListener, resources)).execute(this);
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
