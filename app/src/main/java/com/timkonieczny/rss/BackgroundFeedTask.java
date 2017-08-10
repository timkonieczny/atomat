package com.timkonieczny.rss;

import android.os.AsyncTask;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;


class BackgroundFeedTask extends AsyncTask<Long, Void, Void>{

    @Override
    protected Void doInBackground(Long... sourceIds) {
        SourceUpdater sourceUpdater = new SourceUpdater();

        try {
            for (Long sourceId : sourceIds) sourceUpdater.parse(sourceId, null);
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}