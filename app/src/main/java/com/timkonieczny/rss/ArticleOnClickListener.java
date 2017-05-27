package com.timkonieczny.rss;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.transition.Fade;
import android.util.Log;
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

        Log.d("ArticleOnClickListener", v.findViewById(R.id.article_header).getContentDescription().toString());

        // Create fragment and give it an argument specifying the article it should show
        ArticleFragment newFragment = new ArticleFragment();
        Bundle args = new Bundle();
        args.putInt("index", MainActivity.articles.indexOf(article));
        newFragment.setArguments(args);

        newFragment.setEnterTransition(new Fade());
//        newFragment.setSharedElementEnterTransition(new CustomTransition());

        FragmentTransaction transaction = fragmentManager.beginTransaction();

//        transaction.addSharedElement(v.findViewById(R.id.article_header), "imageHeader");

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        transaction.replace(R.id.fragment_container, newFragment);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
    }
}
