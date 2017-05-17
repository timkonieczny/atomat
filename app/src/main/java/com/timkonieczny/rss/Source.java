package com.timkonieczny.rss;

import android.content.Context;
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
    private Context context;

    Source(UpdateIconImageListener updateIconImageListener, Context context){
        this.updateIconImageListener = updateIconImageListener;
        this.context = context;
    }

    void updateIconImage(){
        (new UpdateIconImageTask(updateIconImageListener, context)).execute(this);
    }

}
