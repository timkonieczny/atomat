package com.timkonieczny.rss;

import android.app.FragmentManager;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;

import java.util.ArrayList;
import java.util.Date;

class Article extends DbRow implements ImageListener{

    Date published;
    String title, author, link, content;
    Source source;
    View.OnClickListener onClickListener;
    private ArticleChangedListener articleChangedListener;

    Image header;
    ArrayList<Image> inlineImages;

    private Context context;

    Article(Context context, FragmentManager fragmentManager){
        header = new Image(Image.TYPE_HEADER);
        this.context = context;
        this.onClickListener = new ArticleOnClickListener(this, fragmentManager);
    }

    Article(Context context, FragmentManager fragmentManager,
            String title, String author, String link, Date published, String content,
            Source source, long dbId){

        this(context, fragmentManager);
        this.title = title;
        this.author = author;
        this.link = link;
        this.published = published;
        this.content = content;
        this.source = source;
        this.dbId = dbId;
    }

    Drawable getImage(ArticleChangedListener articleChangedListener, int index){
        this.articleChangedListener = articleChangedListener;
        if(index == Image.TYPE_HEADER) return header.getDrawable(context, this, title, index);
        else return inlineImages.get(index).getDrawable(context, this, title, index);
    }

    void destroy(){
        if(header != null) header.destroy(context);
        if(inlineImages!=null)
            for(int i = 0; i < inlineImages.size(); i++){
                inlineImages.get(i).destroy(context);
                inlineImages.remove(i);
                i--;
            }
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
        Image image;
        if(index == Image.TYPE_HEADER) image = header;
        else image = inlineImages.get(index);
        ContentValues values = new ContentValues();
        values.put(DbManager.ImagesTable.COLUMN_NAME_PATH, image.fileName);
        values.put(DbManager.ImagesTable.COLUMN_NAME_INDEX, index);
        values.put(DbManager.ImagesTable.COLUMN_NAME_ARTICLE_ID, dbId);
        values.put(DbManager.ImagesTable.COLUMN_NAME_URL, image.url);
        values.put(DbManager.ImagesTable.COLUMN_NAME_WIDTH, image.width);
        values.put(DbManager.ImagesTable.COLUMN_NAME_HEIGHT, image.height);
        image.dbId = MainActivity.dbManager.insertImage(values);

        if (articleChangedListener != null) articleChangedListener.onArticleChanged(this, index);
    }
}