package com.timkonieczny.rss;

import android.os.AsyncTask;
import android.util.Log;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;


class BackgroundFeedTask extends AsyncTask<Void, Void, Void>{

    private DbManager dbManager;

    BackgroundFeedTask(DbManager dbManager){
        this.dbManager = dbManager;
    }

    @Override
    protected Void doInBackground(Void... v) {
        Log.d("BackgroundFeedTask", "running...");
        SourceUpdater sourceUpdater = new SourceUpdater(dbManager);

        try {
            sourceUpdater.parseAll();
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        dbManager.close();
    }
}