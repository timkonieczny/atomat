package com.timkonieczny.rss;

interface ArticleChangedListener {
    void onArticleChanged(Article article, int flag);
}