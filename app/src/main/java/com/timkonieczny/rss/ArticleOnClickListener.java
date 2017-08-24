package com.timkonieczny.rss;

import android.app.FragmentManager;
import android.os.Bundle;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.View;
import android.widget.ImageView;

class ArticleOnClickListener implements View.OnClickListener {

    private Article article;
    private FragmentManager fragmentManager;
    ImageView sharedElement;

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

        Transition transition = TransitionInflater.from(sharedElement.getContext()).inflateTransition(R.transition.article_header_transition);
        articleFragment.setSharedElementEnterTransition(transition);
        articleFragment.setSharedElementReturnTransition(transition);
        articleFragment.setEnterTransition(new Slide());
        articleFragment.setExitTransition(new Slide());
        sharedElement.clearColorFilter();

        fragmentManager
                .beginTransaction()
                .addSharedElement(sharedElement, article.dbId + "_header")
                .replace(R.id.fragment_container, articleFragment)
                .addToBackStack(null)
                .commit();
    }
}
