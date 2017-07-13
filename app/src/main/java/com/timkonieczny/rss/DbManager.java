package com.timkonieczny.rss;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

class DbManager extends SQLiteOpenHelper {

    private static final String DB_NAME = "atomat.db";
    private static final int DB_VERSION = 1;
    SQLiteDatabase db = null;

    DbManager(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createSourceTable(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + SourcesTable.TABLE_NAME);
        onCreate(db);
    }

    // retrieves db if it exists (onCreate() not called), otherwise creates it (onCreate() called).
    void initializeDb(){
        if(db == null) db = getWritableDatabase();
    }

    private void createSourceTable(SQLiteDatabase db){
        db.execSQL("CREATE TABLE " + SourcesTable.TABLE_NAME + " (" +
                SourcesTable._ID + " INTEGER," +
                SourcesTable.COLUMN_NAME_ID + " TEXT," +
                SourcesTable.COLUMN_NAME_URL + " TEXT PRIMARY KEY," +
                SourcesTable.COLUMN_NAME_TITLE + " TEXT," +
                SourcesTable.COLUMN_NAME_ICON + " TEXT," +
                SourcesTable.COLUMN_NAME_ICON_FILE + " TEXT," +
                SourcesTable.COLUMN_NAME_LINK + " TEXT," +
                SourcesTable.COLUMN_NAME_UPDATED + " INT)");
    }

    Cursor getSources(){
        String[] projection = {
                SourcesTable._ID,
                SourcesTable.COLUMN_NAME_ICON,
                SourcesTable.COLUMN_NAME_ICON_FILE,
                SourcesTable.COLUMN_NAME_ID,
                SourcesTable.COLUMN_NAME_LINK,
                SourcesTable.COLUMN_NAME_TITLE,
                SourcesTable.COLUMN_NAME_UPDATED,
                SourcesTable.COLUMN_NAME_URL
        };

        return MainActivity.dbManager.db.query(
                SourcesTable.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null
        );
    }

    void loadSource(Cursor cursor, Resources resources, Context context){
        String url = cursor.getString(cursor.getColumnIndexOrThrow(DbManager.SourcesTable.COLUMN_NAME_URL));
        if(!MainActivity.sources.containsRssUrl(url)) {    // only create source if it doesn't exist yet
            Source source = new Source(resources, context, url);
            source.id = cursor.getString(cursor.getColumnIndexOrThrow(DbManager.SourcesTable.COLUMN_NAME_ID));

            String link = cursor.getString(cursor.getColumnIndexOrThrow(DbManager.SourcesTable.COLUMN_NAME_LINK));
            if (link != null) try {
                source.link = new URL(link);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            source.title = cursor.getString(cursor.getColumnIndexOrThrow(DbManager.SourcesTable.COLUMN_NAME_TITLE));
            source.icon = cursor.getString(cursor.getColumnIndexOrThrow(DbManager.SourcesTable.COLUMN_NAME_ICON));
            // If icon cell of source is empty, null is returned.
            source.iconFileName = cursor.getString(cursor.getColumnIndexOrThrow(DbManager.SourcesTable.COLUMN_NAME_ICON_FILE));

            long updated = cursor.getLong(cursor.getColumnIndexOrThrow(DbManager.SourcesTable.COLUMN_NAME_UPDATED));
            if (updated != -1) source.updated = new Date(updated);

            MainActivity.sources.add(source);
        }
    }

    void saveSource(Source source){
        ContentValues values = new ContentValues();
        values.put(DbManager.SourcesTable.COLUMN_NAME_ID, source.id);
        values.put(DbManager.SourcesTable.COLUMN_NAME_URL, source.rssUrl);
        values.put(DbManager.SourcesTable.COLUMN_NAME_TITLE, source.title);
        if(source.icon!=null) values.put(DbManager.SourcesTable.COLUMN_NAME_ICON, source.icon);
        values.put(DbManager.SourcesTable.COLUMN_NAME_LINK, source.link.toString());
        values.put(DbManager.SourcesTable.COLUMN_NAME_UPDATED, source.updated.getTime());
        MainActivity.dbManager.db.insert(DbManager.SourcesTable.TABLE_NAME, null, values);
    }

    protected boolean sourceExists(String url){
        Cursor cursor = db.query(SourcesTable.TABLE_NAME, new String[]{SourcesTable.COLUMN_NAME_URL},
                SourcesTable.COLUMN_NAME_URL + "=?", new String[]{url}, null, null, null);
        boolean sourceExists = cursor.getCount() > 0;
        cursor.close();
        return sourceExists;
    }

    protected void printSource(Cursor cursor){
        Log.d("Feed",
                DbManager.SourcesTable._ID + " = " + cursor.getLong(cursor.getColumnIndexOrThrow(DbManager.SourcesTable._ID)) + "\n" +
                        DbManager.SourcesTable.COLUMN_NAME_ID + " = " + cursor.getString(cursor.getColumnIndexOrThrow(DbManager.SourcesTable.COLUMN_NAME_ID)) + "\n" +
                        DbManager.SourcesTable.COLUMN_NAME_ICON + " = " + cursor.getString(cursor.getColumnIndexOrThrow(DbManager.SourcesTable.COLUMN_NAME_ICON)) + "\n" +
                        DbManager.SourcesTable.COLUMN_NAME_ICON_FILE + " = " + cursor.getString(cursor.getColumnIndexOrThrow(DbManager.SourcesTable.COLUMN_NAME_ICON_FILE)) + "\n" +
                        DbManager.SourcesTable.COLUMN_NAME_LINK + " = " + cursor.getString(cursor.getColumnIndexOrThrow(DbManager.SourcesTable.COLUMN_NAME_LINK)) + "\n" +
                        DbManager.SourcesTable.COLUMN_NAME_TITLE + " = " + cursor.getString(cursor.getColumnIndexOrThrow(DbManager.SourcesTable.COLUMN_NAME_TITLE)) + "\n" +
                        DbManager.SourcesTable.COLUMN_NAME_UPDATED + " = " + cursor.getLong(cursor.getColumnIndexOrThrow(DbManager.SourcesTable.COLUMN_NAME_UPDATED)) + "\n" +
                        DbManager.SourcesTable.COLUMN_NAME_URL + " = " + cursor.getString(cursor.getColumnIndexOrThrow(DbManager.SourcesTable.COLUMN_NAME_URL)) + "\n"
        );
    }

    void saveSourceIcon(String iconFileName, String url){
        MainActivity.dbManager.initializeDb();
        ContentValues values = new ContentValues();
        values.put(DbManager.SourcesTable.COLUMN_NAME_ICON_FILE, iconFileName);
        MainActivity.dbManager.db.update(DbManager.SourcesTable.TABLE_NAME, values,
                DbManager.SourcesTable.COLUMN_NAME_URL + "= ?", new String[]{url});
    }

    private class SourcesTable implements BaseColumns {
        static final String TABLE_NAME = "sources";
        static final String COLUMN_NAME_URL = "url";
        static final String COLUMN_NAME_ID = "id";
        static final String COLUMN_NAME_TITLE = "title";
        static final String COLUMN_NAME_ICON = "icon";
        static final String COLUMN_NAME_ICON_FILE = "icon_file";
        static final String COLUMN_NAME_LINK = "link";
        static final String COLUMN_NAME_UPDATED = "updated";
    }
}