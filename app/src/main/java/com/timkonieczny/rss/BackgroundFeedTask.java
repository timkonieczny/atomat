package com.timkonieczny.rss;

import android.os.AsyncTask;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;


class BackgroundFeedTask extends AsyncTask<Void, Void, Void>{

    private DbManager dbManager;

    BackgroundFeedTask(DbManager dbManager){
        this.dbManager = dbManager;
    }

    @Override
    protected Void doInBackground(Void... v) {
        AtomParser atomParser = new AtomParser(dbManager);

        try {
            atomParser.parseAll();
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