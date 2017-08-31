package com.timkonieczny.rss;

import android.content.Context;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

class Feed extends AsyncTask<Void, Boolean, Boolean> implements DbOpenListener{

    private Context context;
    private FeedListener feedListener;
    private SourceUpdater sourceUpdater;

    private String newSource;


    Feed(Context context, FeedListener feedListener, String newSource){
        this.context = context;
        this.feedListener = feedListener;
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
                .getString("pref_cleanup", "1209600"))*1000;

        int before = MainActivity.articles.size();
        MainActivity.dbManager.load(context, articleLifetime);
        publishProgress(before != MainActivity.articles.size());

        before = MainActivity.articles.size();
        try {
            if(newSource == null)
                sourceUpdater.parseAll();
            else
                sourceUpdater.parse(DbRow.DEFAULT_DB_ID, newSource, null, null, true);
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }

        MainActivity.dbManager.load(context, articleLifetime);

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