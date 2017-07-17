package com.timkonieczny.rss;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.View;

import java.util.Date;

class Article implements ImageListener{

    Date published;
    String title, author, link, content;
    Source source;
    View.OnClickListener onClickListener;
    private ArticleChangedListener articleChangedListener;

    Image header;
    Image[] inlineImages;

    private Context context;
    private Resources resources;

    static final int HEADER = -1;

    Article(Context context, Resources resources){
        header = new Image();
        this.context = context;
        this.resources = resources;
    }

    Drawable getImage(ArticleChangedListener articleChangedListener, int index){
        this.articleChangedListener = articleChangedListener;
        if(index == HEADER) return header.getDrawable(context, resources, this, title, index);
        else return inlineImages[index].getDrawable(context, resources, this, title, index);
    }

    @Override
    public String toString(){
        return "Title: " + title +
                "\nAuthors: " + author +
                "\nLink: " + link +
                "\nheader: " + header.url +
                "\nPublished: " + published.toString();
    }

    @Override
    public void onImageLoaded(int index) {
        if(index == HEADER) {
            MainActivity.dbManager.updateValue(DbManager.ArticlesTable.TABLE_NAME,
                    DbManager.ArticlesTable.COLUMN_NAME_HEADER_IMAGE_FILE, header.fileName,
                    DbManager.ArticlesTable.COLUMN_NAME_LINK, link);
        }else{
            MainActivity.dbManager.appendString(inlineImages[index].url, link, DbManager.ArticlesTable.COLUMN_NAME_INLINE_IMAGES);
            MainActivity.dbManager.appendString(inlineImages[index].fileName, link, DbManager.ArticlesTable.COLUMN_NAME_INLINE_IMAGES_FILES);
        }
        if (articleChangedListener != null) articleChangedListener.onArticleChanged(this, index);
    }
}