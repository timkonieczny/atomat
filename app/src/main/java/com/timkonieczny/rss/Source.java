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
    }

    void updateIconImage(){
        (new UpdateIconImageTask(updateIconImageListener, resources)).execute(this);
    }

}
