package com.timkonieczny.rss;

import android.app.FragmentManager;
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

    Article(Context context, Resources resources, FragmentManager fragmentManager){
        header = new Image();
        this.context = context;
        this.resources = resources;
        this.onClickListener = new ArticleOnClickListener(this, fragmentManager);
    }

    Article(Context context, Resources resources, FragmentManager fragmentManager,
            String title, String author, String link, Date published, String content,
            String headerUrl, String headerFileName, String[] inlineImageUrls,
            String[] inlineImageFileNames, Source source){

        this(context, resources, fragmentManager);
        this.title = title;
        this.author = author;
        this.link = link;
        this.published = published;
        this.content = content;
        this.source = source;
        this.header.url = headerUrl;
        this.header.fileName = headerFileName;
        if(inlineImageUrls != null) {
            inlineImages = new Image[inlineImageUrls.length];
            for (int i = 0; i < this.inlineImages.length; i++) {
                inlineImages[i].url = inlineImageUrls[i];
                if(inlineImageFileNames != null) inlineImages[i].fileName = inlineImageFileNames[i];
            }
        }
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
            String inlineImageUrls = "";
            String inlineImageFiles = "";
            for (Image inlineImage : inlineImages) {
                if (inlineImage.url != null) inlineImageUrls += " " + inlineImage.url;
                else inlineImageUrls += " null";
                if (inlineImage.fileName != null) inlineImageFiles += " " + inlineImage.fileName;
                else inlineImageFiles += " null";
            }
            inlineImageUrls = inlineImageUrls.replaceFirst(" ", "");
            inlineImageFiles = inlineImageFiles.replaceFirst(" ", "");
            MainActivity.dbManager.updateValue(DbManager.ArticlesTable.TABLE_NAME,
                    DbManager.ArticlesTable.COLUMN_NAME_INLINE_IMAGES, inlineImageUrls,
                    DbManager.ArticlesTable.COLUMN_NAME_LINK+"= ?", link);

            MainActivity.dbManager.updateValue(DbManager.ArticlesTable.TABLE_NAME,
                    DbManager.ArticlesTable.COLUMN_NAME_INLINE_IMAGES_FILES, inlineImageFiles,
                    DbManager.ArticlesTable.COLUMN_NAME_LINK+"= ?", link);
        }
        if (articleChangedListener != null) articleChangedListener.onArticleChanged(this, index);
    }
}