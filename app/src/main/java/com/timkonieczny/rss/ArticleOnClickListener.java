package com.timkonieczny.rss;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
        intent.putExtra("published", article.published);    // TODO: Trying to shared-element-transition from fragment to activity. But here starting point is activity.
        intent.putExtra("source", article.source.title);

        // TODO: Apparently you can't make transitions from a fragment to an activity. Convert ArticleActivity to Fragment...

//        ((Activity) context).getFragmentManager().beginTransaction().
//        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation((Activity)context, ((Activity)context).findViewById(R.id.article_header),"headerImage");
//        context.startActivity(intent, options.toBundle());



        // Create fragment and give it an argument specifying the article it should show
        ArticleFragment newFragment = new ArticleFragment();
        Bundle args = new Bundle();
        args.putString("content", article.content);
        args.putString("author", article.author);
        args.putString("title", article.title);
        args.putString("source", article.source.title);
        newFragment.setArguments(args);

        FragmentTransaction transaction = ((Activity) context).getFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        transaction.replace(R.id.fragment_container, newFragment);
        transaction.addToBackStack(null);

        transaction.addSharedElement(((Activity) context).findViewById(R.id.article_header), "headerImage");

        // Commit the transaction
        transaction.commit();
    }
}
