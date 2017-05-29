package com.timkonieczny.rss;

import android.app.FragmentManager;
import android.os.Bundle;
import android.view.View;

class ArticleOnClickListener implements View.OnClickListener {

    private Article article;
    private FragmentManager fragmentManager;

    ArticleOnClickListener(Article article, FragmentManager fragmentManager){
        this.article = article;
        this.fragmentManager = fragmentManager;
    }

    @Override
    public void onClick(View v) {

        // Create fragment and give it an argument specifying the article it should show
        ArticleFragment articleFragment = new ArticleFragment();
        Bundle args = new Bundle();
        args.putInt("index", MainActivity.articles.indexOf(article));
        articleFragment.setArguments(args);

        fragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, articleFragment)
                .addToBackStack(null)
                .commit();
    }
}
