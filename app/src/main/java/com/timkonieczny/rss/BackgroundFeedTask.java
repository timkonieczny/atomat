package com.timkonieczny.rss;

import android.os.AsyncTask;
import android.util.Log;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;


class BackgroundFeedTask extends AsyncTask<Void, Void, Void>{

    @Override
    protected Void doInBackground(Void... v) {
        SourceUpdater sourceUpdater = new SourceUpdater();

        try {
            sourceUpdater.parseAll();
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}