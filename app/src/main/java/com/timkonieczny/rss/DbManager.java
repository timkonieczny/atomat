package com.timkonieczny.rss;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

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
                SourcesTable._ID + " INTEGER PRIMARY KEY," +
                SourcesTable.COLUMN_NAME_ID + " TEXT," +
                SourcesTable.COLUMN_NAME_URL + " TEXT," +
                SourcesTable.COLUMN_NAME_TITLE + " TEXT," +
                SourcesTable.COLUMN_NAME_ICON + " TEXT," +
                SourcesTable.COLUMN_NAME_LINK + " TEXT," +
                SourcesTable.COLUMN_NAME_UPDATED + " INT)");
    }

    class SourcesTable implements BaseColumns {
        static final String TABLE_NAME = "sources";
        static final String COLUMN_NAME_URL = "url";
        static final String COLUMN_NAME_ID = "id";
        static final String COLUMN_NAME_TITLE = "title";
        static final String COLUMN_NAME_ICON = "icon";
        static final String COLUMN_NAME_LINK = "link";
        static final String COLUMN_NAME_UPDATED = "updated";
    }
}