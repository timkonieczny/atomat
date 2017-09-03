package com.timkonieczny.rss;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;

import java.util.ArrayList;
import java.util.Date;

class Article extends DbRow implements ImageListener{

    Date published;
    String title, author, content;
    private String link;
    Source source;
    boolean isPlaceholder;
    View.OnClickListener onClickListener;
    private ArticleChangedListener articleChangedListener;

    Image header;
    ArrayList<Image> inlineImages;

    private Context context;

    Article(Context context,
            String title, String author, String link, long published, String content,
            Source source, long dbId, boolean isPlaceholder){

        header = new Image(Image.TYPE_HEADER);
        this.context = context;
        this.title = title;
        this.author = author;
        this.link = link;
        this.published = new Date(published);
        this.content = content;
        this.source = source;
        this.dbId = dbId;
        this.isPlaceholder = isPlaceholder;
    }

    Drawable getImage(ArticleChangedListener articleChangedListener, int index){
        this.articleChangedListener = articleChangedListener;
        if(index == Image.TYPE_HEADER) return header.getDrawable(context, this, title, index, dbId);
        else return inlineImages.get(index).getDrawable(context, this, title, index, dbId);
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
        if (articleChangedListener != null) articleChangedListener.onArticleChanged(this, index);
    }
}