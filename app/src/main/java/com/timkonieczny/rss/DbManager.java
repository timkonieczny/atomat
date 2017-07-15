package com.timkonieczny.rss;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

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
    void getDb(){
        if(db == null) db = getWritableDatabase();
    }

    private void createSourceTable(SQLiteDatabase db){
        db.execSQL("CREATE TABLE " + SourcesTable.TABLE_NAME + " (" +
                SourcesTable._ID + " INTEGER," +
                SourcesTable.COLUMN_NAME_URL + " TEXT PRIMARY KEY," +
                SourcesTable.COLUMN_NAME_TITLE + " TEXT," +
                SourcesTable.COLUMN_NAME_ICON + " TEXT," +
                SourcesTable.COLUMN_NAME_ICON_FILE + " TEXT," +
                SourcesTable.COLUMN_NAME_LINK + " TEXT)");
    }

    void loadSources(Resources resources, Context context){
        getDb();
        // Get sources from Db
        String[] projection = {
                SourcesTable._ID,                   SourcesTable.COLUMN_NAME_ICON,
                SourcesTable.COLUMN_NAME_ICON_FILE, SourcesTable.COLUMN_NAME_LINK,
                SourcesTable.COLUMN_NAME_TITLE,     SourcesTable.COLUMN_NAME_URL
        };
        Cursor cursor = db.query(SourcesTable.TABLE_NAME, projection, null, null, null, null, null);

        // Convert sources to Source objects
        while (cursor.moveToNext()) {
            printSource(cursor);
            String url = cursor.getString(cursor.getColumnIndexOrThrow(SourcesTable.COLUMN_NAME_URL));

            if (!MainActivity.sources.containsRssUrl(url)) {    // only create source if it doesn't exist yet
                MainActivity.sources.add(new Source(resources, context, url,
                        cursor.getString(cursor.getColumnIndexOrThrow(SourcesTable.COLUMN_NAME_TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(SourcesTable.COLUMN_NAME_ICON)),
                        cursor.getString(cursor.getColumnIndexOrThrow(SourcesTable.COLUMN_NAME_LINK))
                ));
            }
        }
        cursor.close();
    }

    void saveSource(Source source){
        getDb();
        ContentValues values = new ContentValues();
        values.put(SourcesTable.COLUMN_NAME_URL, source.rssUrl);
        values.put(SourcesTable.COLUMN_NAME_TITLE, source.title);
        if(source.icon!=null) values.put(SourcesTable.COLUMN_NAME_ICON, source.icon);
        values.put(SourcesTable.COLUMN_NAME_LINK, source.link);
        db.insert(SourcesTable.TABLE_NAME, null, values);
    }

    void saveSourceIcon(String iconFileName, String url){
        getDb();
        ContentValues values = new ContentValues();
        values.put(SourcesTable.COLUMN_NAME_ICON_FILE, iconFileName);
        db.update(SourcesTable.TABLE_NAME, values,
                SourcesTable.COLUMN_NAME_URL + "= ?", new String[]{url});
    }

    private void printSource(Cursor cursor){
        Log.d("DbManager",
                SourcesTable._ID + " = " + cursor.getLong(cursor.getColumnIndexOrThrow(SourcesTable._ID)) + "\n" +
                        SourcesTable.COLUMN_NAME_ICON + " = " + cursor.getString(cursor.getColumnIndexOrThrow(SourcesTable.COLUMN_NAME_ICON)) + "\n" +
                        SourcesTable.COLUMN_NAME_ICON + " = " + (cursor.getString(cursor.getColumnIndexOrThrow(SourcesTable.COLUMN_NAME_ICON))==null) + "\n" +
                        SourcesTable.COLUMN_NAME_ICON_FILE + " = " + cursor.getString(cursor.getColumnIndexOrThrow(SourcesTable.COLUMN_NAME_ICON_FILE)) + "\n" +
                        SourcesTable.COLUMN_NAME_LINK + " = " + cursor.getString(cursor.getColumnIndexOrThrow(SourcesTable.COLUMN_NAME_LINK)) + "\n" +
                        SourcesTable.COLUMN_NAME_TITLE + " = " + cursor.getString(cursor.getColumnIndexOrThrow(SourcesTable.COLUMN_NAME_TITLE)) + "\n" +
                        SourcesTable.COLUMN_NAME_URL + " = " + cursor.getString(cursor.getColumnIndexOrThrow(SourcesTable.COLUMN_NAME_URL)) + "\n"
        );
    }

    private class SourcesTable implements BaseColumns {
        static final String TABLE_NAME = "sources";
        static final String COLUMN_NAME_URL = "url";
        static final String COLUMN_NAME_TITLE = "title";
        static final String COLUMN_NAME_ICON = "icon";
        static final String COLUMN_NAME_ICON_FILE = "icon_file";
        static final String COLUMN_NAME_LINK = "link";
    }
}