package com.timkonieczny.rss;

import android.graphics.Bitmap;

import java.net.URL;
import java.util.Date;

class Article {

    Date published, updated;
    String title, content, id, author, uniqueId, headerImage;
    URL link;
    Bitmap headerImageBitmap;
    Source source;
    private UpdateHeaderImageListener updateHeaderImageListener;

    Article(UpdateHeaderImageListener updateHeaderImageListener){
        this.updateHeaderImageListener = updateHeaderImageListener;
    }

    void updateHeaderImage() {
        (new UpdateHeaderImageTask(updateHeaderImageListener)).execute(this);
    }
}
