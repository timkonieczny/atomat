package com.timkonieczny.rss;

import android.content.Context;
import android.os.AsyncTask;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;


class BackgroundFeedTask extends AsyncTask<Void, Void, Void>{

    private DbManager dbManager;
    private Context context;

    BackgroundFeedTask(DbManager dbManager, Context context){
        this.dbManager = dbManager;
        this.context = context;
    }

    @Override
    protected Void doInBackground(Void... v) {
        AtomParser atomParser = new AtomParser(dbManager);

        try {
            int newArticles = atomParser.parseAll();
            if(newArticles>0) {
                NotificationHelper notificationHelper = new NotificationHelper(context);
                notificationHelper.notify(notificationHelper.getNotification(newArticles + context.getString(R.string.new_stories), "This is the Body"));
            }
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