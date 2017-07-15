package com.timkonieczny.rss;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v7.graphics.Palette;
import android.view.View;

import java.util.Date;

class Article {

    Date published;
    String title, author, headerImage, link;
    CharSequence content;
    Bitmap headerImageBitmap = null;
    Palette colorPalette;
    Source source;
    View.OnClickListener onClickListener;
    Drawable[] inlineImages;

    private UpdateHeaderImageTask task;

    void updateHeaderImage() {
        task = new UpdateHeaderImageTask();
        task.execute(this);
    }

    void setUpdateHeaderImageListener(UpdateHeaderImageListener listener){
        task.updateHeaderImageListener = listener;
    }

    @Override
    public String toString(){
        return "Title: " + title +
                "\nAuthors: " + author +
                "\nLink: " + link +
                "\nheaderImage: " + headerImage +
                "\nPublished: " + published.toString();
    }
}
