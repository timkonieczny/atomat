package com.timkonieczny.rss;

import android.app.FragmentManager;
import android.content.Context;
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
    private FragmentManager fragmentManager;
    private FeedListener feedListener;
    private SourceUpdater sourceUpdater;

    private String newSource;

    Feed(Context context, FeedListener feedListener, FragmentManager fragmentManager){
        this(context, feedListener, fragmentManager, null);
    }

    Feed(Context context, FeedListener feedListener, FragmentManager fragmentManager, String newSource){
        this.context = context;
        this.feedListener = feedListener;
        this.fragmentManager = fragmentManager;
        this.newSource = newSource;
        sourceUpdater = new SourceUpdater();
        (new DbOpenTask(MainActivity.dbManager, this)).execute();
    }

    @Override
    public void onDbOpened() {
        this.execute();
    }

    @Override
    protected final Boolean doInBackground(Void... params) {

        long articleLifetime = Integer.parseInt(PreferenceManager
                .getDefaultSharedPreferences(context)
                .getString("pref_sync", "1209600"))*1000;

        int before = MainActivity.articles.size();
        MainActivity.dbManager.load(context, fragmentManager, articleLifetime);
        publishProgress(before != MainActivity.articles.size());

        before = MainActivity.articles.size();
        try {
            for (int i = 0; i < MainActivity.sources.size(); i++) {
                sourceUpdater.parse(MainActivity.sources.get(i).dbId, null);
            }
            if (newSource != null) sourceUpdater.parse(Source.DEFAULT_DB_ID, newSource);
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }

        MainActivity.dbManager.load(context, fragmentManager, articleLifetime);

        return before != MainActivity.articles.size();
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
}