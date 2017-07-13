package com.timkonieczny.rss;

import android.app.FragmentManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.AsyncTask;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

class Feed extends AsyncTask<Void, Void, Boolean> implements DbOpenListener{

    private FeedListener feedListener;

    private HashSet<String> existingIds;
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

        existingIds = getExistingArticlesIds();

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

        existingIds = getExistingArticlesIds();

        (new DbOpenTask(MainActivity.dbManager, this)).execute();
    }

    @Override
    public void onDbOpened() {
        this.execute();
    }

    @Override
    protected final Boolean doInBackground(Void... params) {

        List<Article> articles = new ArrayList<>(0);

        Cursor cursor = MainActivity.dbManager.getSources();

        while (cursor.moveToNext()){
            MainActivity.dbManager.printSource(cursor);

            // retrieves source from db and saves in mainactivity.sources
            MainActivity.dbManager.loadSource(cursor, resources, context);


            // reads feed and adds to db
            updateSource(cursor.getString(cursor.getColumnIndexOrThrow(DbManager.SourcesTable.COLUMN_NAME_URL)), articles);
        }


        if(newSource != null) updateSource(newSource, articles);
        Log.d("Sources (FEED)", "doInBackground() mid1 " + MainActivity.sources.size());

        //printSources();
        Log.d("Sources (FEED)", "doInBackground() mid2 " + MainActivity.sources.size());

        Article article;
        for(int i = 0; i < articles.size(); i++){
            article = articles.get(i);
            article.uniqueId = article.source.id + "_" + article.id;
            if(existingIds.contains(article.uniqueId)){
                articles.remove(i);
                i--;
            }else{
                article.onClickListener = new ArticleOnClickListener(article, fragmentManager);
                if(article.headerImage!=null) article.updateHeaderImage();
            }
        }
        Log.d("Sources (FEED)", "doInBackground() mid3 " + MainActivity.sources.size());

        MainActivity.articles.addAll(articles);
        Collections.sort(MainActivity.articles, descending);

        Log.d("Sources (FEED)", "doInBackground() end " + MainActivity.sources.size());

        return articles.size() > 0;
    }

    private void updateSource(String currentURL, List<Article> articles){
        try {
            HttpURLConnection connection = (HttpURLConnection) (new URL(currentURL)).openConnection();

            connection.setReadTimeout(10000);
            connection.setConnectTimeout(15000);

            // Starts the query
            connection.connect();
            InputStream stream = connection.getInputStream();

            SourceUpdater sourceUpdater = new SourceUpdater();

            if(!MainActivity.dbManager.sourceExists(currentURL)){

                articles.addAll(sourceUpdater.parse(stream, new Source(resources, context, currentURL), true));

                MainActivity.sources.put(sourceUpdater.source.rssUrl, sourceUpdater.source);    //adapter keys?
                Log.d("Sources (FEED)", "cursor.getCount() == 0 " + MainActivity.sources.size());
                SourcesAdapter.keys.add(sourceUpdater.source.rssUrl);

                MainActivity.dbManager.saveSource(sourceUpdater.source);

                MainActivity.sources.get(currentURL).isStub = false;
            }else{
                // TODO: Display error: Source already added
                articles.addAll(sourceUpdater.parse(stream, MainActivity.sources.get(currentURL), false));
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

    private HashSet<String> getExistingArticlesIds(){
        HashSet<String> ids = new HashSet<>();
        for(int i = 0; i < MainActivity.articles.size(); i++){
            ids.add(MainActivity.articles.get(i).uniqueId);
        }
        return ids;
    }
}