package com.timkonieczny.rss;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.List;

class DbManager extends SQLiteOpenHelper {

    private static final String DB_NAME = "atomat.db";
    private static final int DB_VERSION = 1;
    SQLiteDatabase db = null;

    DbManager(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);
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

    private void createTables(SQLiteDatabase db){
        db.execSQL("CREATE TABLE " + SourcesTable.TABLE_NAME + " (" +
                SourcesTable._ID +                      " INTEGER PRIMARY KEY," +
                SourcesTable.COLUMN_NAME_URL +          " TEXT," +
                SourcesTable.COLUMN_NAME_TITLE +        " TEXT," +
                SourcesTable.COLUMN_NAME_ICON +         " TEXT," +
                SourcesTable.COLUMN_NAME_ICON_FILE +    " TEXT," +
                SourcesTable.COLUMN_NAME_LINK +         " TEXT)");

        db.execSQL("CREATE TABLE " + ArticlesTable.TABLE_NAME + " (" +
                ArticlesTable._ID +                             " INTEGER PRIMARY KEY," +
                ArticlesTable.COLUMN_NAME_LINK +                " TEXT," +
                ArticlesTable.COLUMN_NAME_SOURCE_ID +           " INTEGER," +
                ArticlesTable.COLUMN_NAME_TITLE +               " TEXT," +
                ArticlesTable.COLUMN_NAME_AUTHOR +              " TEXT," +
                ArticlesTable.COLUMN_NAME_PUBLISHED +           " INTEGER," +
                ArticlesTable.COLUMN_NAME_CONTENT +             " TEXT," +
                ArticlesTable.COLUMN_NAME_HEADER_IMAGE +        " TEXT," +
                ArticlesTable.COLUMN_NAME_HEADER_IMAGE_FILE +   " TEXT," +
                ArticlesTable.COLUMN_NAME_INLINE_IMAGES +       " TEXT," +
                ArticlesTable.COLUMN_NAME_INLINE_IMAGES_FILES + " TEXT)");
    }

    void loadSources(Resources resources, Context context){     // TODO: loadArticles
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
            String url = cursor.getString(cursor.getColumnIndexOrThrow(SourcesTable.COLUMN_NAME_URL));

            if (!MainActivity.sources.containsRssUrl(url)) {    // only create source if it doesn't exist yet
                MainActivity.sources.add(new Source(context, resources, url,
                        cursor.getString(cursor.getColumnIndexOrThrow(SourcesTable.COLUMN_NAME_TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(SourcesTable.COLUMN_NAME_LINK)),
                        cursor.getString(cursor.getColumnIndexOrThrow(SourcesTable.COLUMN_NAME_ICON)),
                        cursor.getString(cursor.getColumnIndexOrThrow(SourcesTable.COLUMN_NAME_ICON_FILE))
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
        if(source.icon.url!=null) values.put(SourcesTable.COLUMN_NAME_ICON, source.icon.url);
        values.put(SourcesTable.COLUMN_NAME_LINK, source.link);
        db.insert(SourcesTable.TABLE_NAME, null, values);
    }

    void updateValue(String table, String column, String value, String whereColumn, String whereValue){
        getDb();
        ContentValues values = new ContentValues();
        values.put(column, value);
        db.update(table, values, whereColumn + "= ?", new String[]{whereValue});
    }

    private void printSource(Cursor cursor){
        Log.d("DbManager",
                SourcesTable._ID + " = " + cursor.getLong(cursor.getColumnIndexOrThrow(SourcesTable._ID)) + "\n" +
                        SourcesTable.COLUMN_NAME_ICON + " = " + cursor.getString(cursor.getColumnIndexOrThrow(SourcesTable.COLUMN_NAME_ICON)) + "\n" +
                        SourcesTable.COLUMN_NAME_ICON_FILE + " = " + cursor.getString(cursor.getColumnIndexOrThrow(SourcesTable.COLUMN_NAME_ICON_FILE)) + "\n" +
                        SourcesTable.COLUMN_NAME_LINK + " = " + cursor.getString(cursor.getColumnIndexOrThrow(SourcesTable.COLUMN_NAME_LINK)) + "\n" +
                        SourcesTable.COLUMN_NAME_TITLE + " = " + cursor.getString(cursor.getColumnIndexOrThrow(SourcesTable.COLUMN_NAME_TITLE)) + "\n" +
                        SourcesTable.COLUMN_NAME_URL + " = " + cursor.getString(cursor.getColumnIndexOrThrow(SourcesTable.COLUMN_NAME_URL)) + "\n"
        );
    }

    void saveArticles(List<Article> articles){
        getDb();
        for(int i = 0; i < articles.size(); i++) {
            
            Cursor cursor = db.query(
                    SourcesTable.TABLE_NAME,
                    new String[]{SourcesTable._ID},
                    SourcesTable.COLUMN_NAME_URL+"=?",
                    new String[]{articles.get(i).source.rssUrl},
                    null, null, null, null);
            cursor.moveToFirst();
            long sourceId = cursor.getLong(cursor.getColumnIndexOrThrow(SourcesTable._ID));
            cursor.close();
            ContentValues values = new ContentValues();
            values.put(ArticlesTable.COLUMN_NAME_LINK, articles.get(i).link);
            values.put(ArticlesTable.COLUMN_NAME_SOURCE_ID, sourceId);
            values.put(ArticlesTable.COLUMN_NAME_TITLE, articles.get(i).title);
            values.put(ArticlesTable.COLUMN_NAME_AUTHOR, articles.get(i).author);
            values.put(ArticlesTable.COLUMN_NAME_PUBLISHED, articles.get(i).published.getTime());
            values.put(ArticlesTable.COLUMN_NAME_CONTENT, articles.get(i).content);
            values.put(ArticlesTable.COLUMN_NAME_HEADER_IMAGE, articles.get(i).header.url);
            db.insert(ArticlesTable.TABLE_NAME, null, values);
        }
    }

    void appendString(String string, String link, String column){
        getDb();

        Cursor cursor = db.query(ArticlesTable.TABLE_NAME, new String[]{column},
                ArticlesTable.COLUMN_NAME_LINK+"= ?", new String[]{link}, null, null, null);
        cursor.moveToFirst();
        String columnValue = cursor.getString(cursor.getColumnIndexOrThrow(column));
        cursor.close();

        if(columnValue == null) columnValue = string;
        else if(!columnValue.contains(string)) columnValue+=" "+string;

        updateValue(ArticlesTable.TABLE_NAME, column, columnValue, ArticlesTable.COLUMN_NAME_LINK, link);
    }

    class SourcesTable implements BaseColumns {
        static final String TABLE_NAME = "sources";
        static final String COLUMN_NAME_URL = "url";
        static final String COLUMN_NAME_TITLE = "title";
        static final String COLUMN_NAME_ICON = "icon";
        static final String COLUMN_NAME_ICON_FILE = "icon_file";
        static final String COLUMN_NAME_LINK = "link";
    }

    class ArticlesTable implements BaseColumns {
        static final String TABLE_NAME = "articles";
        static final String COLUMN_NAME_LINK = "link";
        static final String COLUMN_NAME_SOURCE_ID = "source_url";
        static final String COLUMN_NAME_TITLE = "title";
        static final String COLUMN_NAME_AUTHOR = "author";
        static final String COLUMN_NAME_PUBLISHED = "published";
        static final String COLUMN_NAME_CONTENT = "content";
        static final String COLUMN_NAME_HEADER_IMAGE = "header_image";
        static final String COLUMN_NAME_HEADER_IMAGE_FILE = "header_image_file";
        static final String COLUMN_NAME_INLINE_IMAGES = "inline_images";
        static final String COLUMN_NAME_INLINE_IMAGES_FILES = "inline_images_files";
    }
}