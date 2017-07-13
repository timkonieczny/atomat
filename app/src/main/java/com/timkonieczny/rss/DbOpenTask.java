package com.timkonieczny.rss;

import android.os.AsyncTask;

public class DbOpenTask extends AsyncTask<Void,Void,Void> {

    DbManager dbManager;
    DbOpenListener dbOpenListener;

    public DbOpenTask(DbManager dbManager, DbOpenListener dbOpenListener){
        this.dbManager = dbManager;
        this.dbOpenListener = dbOpenListener;
    }

    @Override
    protected Void doInBackground(Void... params) {
        dbManager.initializeDb();
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        dbOpenListener.onDbOpened();
    }
}
