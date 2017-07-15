package com.timkonieczny.rss;

import android.app.FragmentManager;
import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

class Feed extends AsyncTask<Void, Void, Boolean> implements DbOpenListener{

    private FeedListener feedListener;

    private HashSet<String> existingLinks;
    private Comparator<Article> descending;

    private FragmentManager fragmentManager;
    private Context context;
    private Resources resources;

    private String newSource;

    Feed(Object listener, FragmentManager fragmentManager, Context context, Resources resources){

        this.fragmentManager = fragmentManager;
        this.context = context;
        this.resources = resources;
        this.feedListener = (FeedListener)listener;
        this.newSource = null;

        descending = new Comparator<Article>() {
            @Override
            public int compare(Article a1, Article a2) {
                return a2.published.compareTo(a1.published);
            }
        };

        existingLinks = getExistingArticlesLinks();

        (new DbOpenTask(MainActivity.dbManager, this)).execute();
    }

    Feed(String newSource, Object listener, FragmentManager fragmentManager, Context context, Resources resources){

        this.fragmentManager = fragmentManager;
        this.context = context;
        this.resources = resources;
        this.feedListener = (FeedListener)listener;
        this.newSource = newSource;

        descending = new Comparator<Article>() {
            @Override
            public int compare(Article a1, Article a2) {
                return a2.published.compareTo(a1.published);
            }
        };

        existingLinks = getExistingArticlesLinks();

        (new DbOpenTask(MainActivity.dbManager, this)).execute();
    }

    @Override
    public void onDbOpened() {
        this.execute();
    }

    @Override
    protected final Boolean doInBackground(Void... params) {
        List<Article> articles = new ArrayList<>(0);
        MainActivity.dbManager.loadSources(resources, context);
        for(int i = 0; i < MainActivity.sources.size(); i++){
            updateSource(MainActivity.sources.get(i), articles, false);
        }


        if(newSource != null) updateSource(new Source(resources, context, newSource), articles, true);

        Article article;
        for(int i = 0; i < articles.size(); i++){
            article = articles.get(i);
            if(existingLinks.contains(article.link)){
                articles.remove(i);
                i--;
            }else{
                article.onClickListener = new ArticleOnClickListener(article, fragmentManager);
                if(article.headerImage!=null) article.updateHeaderImage();
            }
        }

        MainActivity.articles.addAll(articles);
        Collections.sort(MainActivity.articles, descending);

        return articles.size() > 0;
    }

    private void updateSource(Source source, List<Article> articles, boolean isNew){
        try {
            HttpURLConnection connection = (HttpURLConnection) (new URL(source.rssUrl)).openConnection();
            connection.setReadTimeout(10000);
            connection.setConnectTimeout(15000);
            connection.connect();
            InputStream stream = connection.getInputStream();

            SourceUpdater sourceUpdater = new SourceUpdater();
            articles.addAll(sourceUpdater.parse(stream, source, isNew));

            if(isNew){
                MainActivity.sources.add(sourceUpdater.source);
                MainActivity.dbManager.saveSource(sourceUpdater.source);
            }

        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPostExecute(Boolean hasNewArticles) {
        super.onPostExecute(hasNewArticles);
        if(feedListener!=null) feedListener.onFeedUpdated(hasNewArticles);
    }

    private HashSet<String> getExistingArticlesLinks(){
        HashSet<String> links = new HashSet<>();
        for(int i = 0; i < MainActivity.articles.size(); i++){
            links.add(MainActivity.articles.get(i).link);
        }
        return links;
    }
}