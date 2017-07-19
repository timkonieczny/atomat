package com.timkonieczny.rss;

import android.app.FragmentManager;
import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

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

class Feed extends AsyncTask<Void, Boolean, Boolean> implements DbOpenListener{

    private Context context;
    private Resources resources;
    private FragmentManager fragmentManager;
    private FeedListener feedListener;
    private SourceUpdater sourceUpdater;

    private Comparator<Article> descending;
    private String newSource;

    Feed(Context context, Resources resources, FeedListener feedListener, FragmentManager fragmentManager){
        this(context, resources, feedListener, fragmentManager, null);
    }

    Feed(Context context, Resources resources, FeedListener feedListener, FragmentManager fragmentManager, String newSource){
        this.context = context;
        this.resources = resources;
        this.feedListener = feedListener;
        this.fragmentManager = fragmentManager;
        this.newSource = newSource;
        descending = new Comparator<Article>() {
            @Override
            public int compare(Article a1, Article a2) {
                return a2.published.compareTo(a1.published);
            }
        };
        sourceUpdater = new SourceUpdater(context, resources, fragmentManager);
        (new DbOpenTask(MainActivity.dbManager, this)).execute();
    }

    @Override
    public void onDbOpened() {
        this.execute();
    }

    @Override
    protected final Boolean doInBackground(Void... params) {

        long articleLifetime = PreferenceManager
                .getDefaultSharedPreferences(context)
                .getInt("pref_sync", 1209600)*1000;

        int before = MainActivity.articles.size();
        MainActivity.dbManager.load(resources, context, fragmentManager, articleLifetime);
        Collections.sort(MainActivity.articles, descending);
        publishProgress(before != MainActivity.articles.size());

        List<Article> articles = new ArrayList<>(0);
        for(int i = 0; i < MainActivity.sources.size(); i++){
            updateSource(MainActivity.sources.get(i), articles, false);
        }

        if(newSource != null) updateSource(new Source(context, resources, newSource), articles, true);

        HashSet<String> existingLinks = getExistingArticleLinks();

        Article article;
        for(int i = 0; i < articles.size(); i++){
            article = articles.get(i);
            if(existingLinks.contains(article.link) ||
                    article.published.getTime()<System.currentTimeMillis()-articleLifetime){
                articles.remove(i);
                i--;
            }else article.getImage(null, Article.HEADER);
        }

        MainActivity.dbManager.createArticles(articles);
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

            articles.addAll(sourceUpdater.parse(stream, source, isNew));

            if(isNew){
                MainActivity.sources.add(sourceUpdater.source);
                MainActivity.dbManager.createSource(sourceUpdater.source);
            }

        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onProgressUpdate(Boolean... hasNewArticles) {
        super.onProgressUpdate(hasNewArticles);
        feedListener.onFeedUpdated(hasNewArticles[0], false);
    }

    @Override
    protected void onPostExecute(Boolean hasNewArticles) {
        super.onPostExecute(hasNewArticles);
        feedListener.onFeedUpdated(hasNewArticles, true);
    }

    private HashSet<String> getExistingArticleLinks(){
        HashSet<String> links = new HashSet<>();
        for(int i = 0; i < MainActivity.articles.size(); i++){
            links.add(MainActivity.articles.get(i).link);
        }
        return links;
    }
}