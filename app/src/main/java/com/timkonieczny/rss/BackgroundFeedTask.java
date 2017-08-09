package com.timkonieczny.rss;

import android.os.AsyncTask;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;


class BackgroundFeedTask extends AsyncTask<Void, Void, Void> implements DbOpenListener{

    BackgroundFeedTask(){
        (new DbOpenTask(MainActivity.dbManager, this)).execute();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        SourceUpdater sourceUpdater = new SourceUpdater();

        long[] sourceIds = MainActivity.dbManager.getSourceIds();

        try {
            for (long sourceId : sourceIds) {
                sourceUpdater.parse(sourceId, null);
            }
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void onDbOpened() {
        this.execute();
    }
}