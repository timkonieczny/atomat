package com.timkonieczny.rss;

import android.content.Context;
import android.os.AsyncTask;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;


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

                ArrayList<String> newTitles = atomParser.newStoriesTitles;
                String notificationContent = "";
                for(int i = 0; i < newTitles.size(); i++){
                    notificationContent += newTitles.get(i);
                    if(i < newTitles.size()-1){
                        notificationContent += ", ";
                    }
                }

                String notificationTitle = newArticles + " " + context.getString( newArticles == 1 ? R.string.new_story : R.string.new_stories) + " from ";
                for(int i = 0; i < atomParser.newArticlesSourcesTitles.size(); i++) {
                    notificationTitle += atomParser.newArticlesSourcesTitles.get(i);

                    if(i < atomParser.newArticlesSourcesTitles.size()-2){
                        notificationTitle += ", ";
                    }else if(i == atomParser.newArticlesSourcesTitles.size()-2){
                        notificationTitle += " " + context.getString(R.string.and) + " ";
                    }
                }
                notificationHelper.notify(notificationHelper.getNotification(notificationTitle, notificationContent, R.mipmap.ic_launcher, newTitles));
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