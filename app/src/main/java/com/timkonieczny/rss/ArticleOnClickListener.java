package com.timkonieczny.rss;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;

class ArticleOnClickListener implements View.OnClickListener {

    private Article article;
    private Context context;

    ArticleOnClickListener(Article article, Context context){
        this.article = article;
        this.context = context;
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(context, ArticleActivity.class);
        intent.putExtra("content", article.content);
        intent.putExtra("author", article.author);
        intent.putExtra("title", article.title);
        intent.putExtra("published", article.published);
        intent.putExtra("source", article.source.title);
        context.startActivity(intent);
    }
}
