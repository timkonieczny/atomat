package com.timkonieczny.rss;

import android.os.AsyncTask;

class DbOpenTask extends AsyncTask<Void,Void,Void> {

    private DbManager dbManager;
    private DbOpenListener dbOpenListener;

    DbOpenTask(DbManager dbManager, DbOpenListener dbOpenListener){
        this.dbManager = dbManager;
        this.dbOpenListener = dbOpenListener;
    }

    @Override
    protected Void doInBackground(Void... params) {
        dbManager.getDb();
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        dbOpenListener.onDbOpened();
    }
}
