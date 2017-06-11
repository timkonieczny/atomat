package com.timkonieczny.rss;

import android.graphics.Bitmap;
import android.support.v7.graphics.Palette;
import android.view.View;

import java.net.URL;
import java.util.Date;

class Article {

    Date published;
    String title, id, author, uniqueId, headerImage;
    CharSequence content;
    URL link;
    Bitmap headerImageBitmap = null;
    Palette colorPalette;
    Source source;
    View.OnClickListener onClickListener;

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
                "\nContent: " + content +
                "\nLink: " + link +
                "\nID: " + id +
                "\nheaderImage: " + headerImage +
                "\nuniqueID: " + uniqueId;
    }
}
