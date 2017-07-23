package com.timkonieczny.rss;

import android.app.FragmentManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import java.util.ArrayList;
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
        if(db == null){
            db = getWritableDatabase();
            db.execSQL("PRAGMA foreign_keys = ON");
        }
    }

    private void createTables(SQLiteDatabase db){

        db.execSQL("CREATE TABLE " + SourcesTable.TABLE_NAME + " (" +
                SourcesTable._ID +                              " INTEGER PRIMARY KEY, " +
                SourcesTable.COLUMN_NAME_URL +                  " TEXT, " +
                SourcesTable.COLUMN_NAME_TITLE +                " TEXT, " +
                SourcesTable.COLUMN_NAME_ICON_URL +                 " TEXT, " +
                SourcesTable.COLUMN_NAME_ICON_PATH +            " TEXT, " +
                SourcesTable.COLUMN_NAME_WEBSITE +                 " TEXT)");

        db.execSQL("CREATE TABLE " + ArticlesTable.TABLE_NAME + " ( " +
                ArticlesTable._ID +                             " INTEGER PRIMARY KEY, " +
                ArticlesTable.COLUMN_NAME_URL +                " TEXT, " +
                ArticlesTable.COLUMN_NAME_SOURCE_ID +           " INTEGER, " +
                ArticlesTable.COLUMN_NAME_TITLE +               " TEXT, " +
                ArticlesTable.COLUMN_NAME_AUTHOR +              " TEXT, " +
                ArticlesTable.COLUMN_NAME_PUBLISHED +           " INTEGER, " +
                ArticlesTable.COLUMN_NAME_CONTENT +             " TEXT, " +
                "FOREIGN KEY (" + ArticlesTable.COLUMN_NAME_SOURCE_ID + ") " +
                "REFERENCES " + SourcesTable.TABLE_NAME + "(" + SourcesTable._ID + ") " +
                "ON DELETE CASCADE)");

        db.execSQL("CREATE TABLE " + ImagesTable.TABLE_NAME + " (" +
                ImagesTable._ID +                               " INTEGER PRIMARY KEY, " +
                ImagesTable.COLUMN_NAME_ARTICLE_ID +            " INTEGER, " +
                ImagesTable.COLUMN_NAME_WIDTH +                 " INTEGER, " +
                ImagesTable.COLUMN_NAME_HEIGHT +                " INTEGER, " +
                ImagesTable.COLUMN_NAME_URL +                   " TEXT, " +
                ImagesTable.COLUMN_NAME_PATH +                  " TEXT, " +
                ImagesTable.COLUMN_NAME_TYPE +                 " INTEGER, " +
                "FOREIGN KEY (" + ImagesTable.COLUMN_NAME_ARTICLE_ID + ") " +
                "REFERENCES " + ArticlesTable.TABLE_NAME + "(" + ArticlesTable._ID + ") " +
                "ON DELETE CASCADE)");
    }

    void load(Context context, FragmentManager fragmentManager, long articleLifetime){

        getDb();

        // get valid articles
        long validTime = System.currentTimeMillis()-articleLifetime;

        String query = "SELECT " +
                SourcesTable.TABLE_NAME + "." + SourcesTable._ID +                      " AS "+ SourcesTable.TABLE_NAME + SourcesTable._ID + ", \n" +
                SourcesTable.TABLE_NAME + "." + SourcesTable.COLUMN_NAME_URL +          " AS "+ SourcesTable.TABLE_NAME + "_" + SourcesTable.COLUMN_NAME_URL + ", \n" +
                SourcesTable.TABLE_NAME + "." + SourcesTable.COLUMN_NAME_TITLE +        " AS "+ SourcesTable.TABLE_NAME + "_" + SourcesTable.COLUMN_NAME_TITLE + ", \n" +
                SourcesTable.TABLE_NAME + "." + SourcesTable.COLUMN_NAME_ICON_URL +         " AS "+ SourcesTable.TABLE_NAME + "_" + SourcesTable.COLUMN_NAME_ICON_URL + ", \n" +
                SourcesTable.TABLE_NAME + "." + SourcesTable.COLUMN_NAME_ICON_PATH +    " AS "+ SourcesTable.TABLE_NAME + "_" + SourcesTable.COLUMN_NAME_ICON_PATH + ", \n" +
                SourcesTable.TABLE_NAME + "." + SourcesTable.COLUMN_NAME_WEBSITE +         " AS "+ SourcesTable.TABLE_NAME + "_" + SourcesTable.COLUMN_NAME_WEBSITE + ", \n" +

                ArticlesTable.TABLE_NAME +"." + ArticlesTable._ID +                     " AS "+ ArticlesTable.TABLE_NAME + ArticlesTable._ID + ", \n" +
                ArticlesTable.TABLE_NAME +"." + ArticlesTable.COLUMN_NAME_URL +        " AS "+ ArticlesTable.TABLE_NAME + "_" + ArticlesTable.COLUMN_NAME_URL + ", \n" +
                ArticlesTable.TABLE_NAME +"." + ArticlesTable.COLUMN_NAME_SOURCE_ID +   " AS "+ ArticlesTable.TABLE_NAME + "_" + ArticlesTable.COLUMN_NAME_SOURCE_ID + ", \n" +
                ArticlesTable.TABLE_NAME +"." + ArticlesTable.COLUMN_NAME_TITLE +       " AS "+ ArticlesTable.TABLE_NAME + "_" + ArticlesTable.COLUMN_NAME_TITLE + ", \n" +
                ArticlesTable.TABLE_NAME +"." + ArticlesTable.COLUMN_NAME_AUTHOR +      " AS "+ ArticlesTable.TABLE_NAME + "_" + ArticlesTable.COLUMN_NAME_AUTHOR + ", \n" +
                ArticlesTable.TABLE_NAME +"." + ArticlesTable.COLUMN_NAME_PUBLISHED +   " AS "+ ArticlesTable.TABLE_NAME + "_" + ArticlesTable.COLUMN_NAME_PUBLISHED + ", \n" +
                ArticlesTable.TABLE_NAME +"." + ArticlesTable.COLUMN_NAME_CONTENT +     " AS "+ ArticlesTable.TABLE_NAME + "_" + ArticlesTable.COLUMN_NAME_CONTENT + ", \n" +

                ImagesTable.TABLE_NAME + "." + ImagesTable._ID +                        " AS "+ ImagesTable.TABLE_NAME + ImagesTable._ID + ", \n" +
                ImagesTable.TABLE_NAME + "." + ImagesTable.COLUMN_NAME_ARTICLE_ID +     " AS "+ ImagesTable.TABLE_NAME + "_" + ImagesTable.COLUMN_NAME_ARTICLE_ID + ", \n" +
                ImagesTable.TABLE_NAME + "." + ImagesTable.COLUMN_NAME_URL +            " AS "+ ImagesTable.TABLE_NAME + "_" + ImagesTable.COLUMN_NAME_URL + ", \n" +
                ImagesTable.TABLE_NAME + "." + ImagesTable.COLUMN_NAME_PATH +           " AS "+ ImagesTable.TABLE_NAME + "_" + ImagesTable.COLUMN_NAME_PATH + ", \n" +
                ImagesTable.TABLE_NAME + "." + ImagesTable.COLUMN_NAME_WIDTH +          " AS "+ ImagesTable.TABLE_NAME + "_" + ImagesTable.COLUMN_NAME_WIDTH + ", \n" +
                ImagesTable.TABLE_NAME + "." + ImagesTable.COLUMN_NAME_HEIGHT +         " AS "+ ImagesTable.TABLE_NAME + "_" + ImagesTable.COLUMN_NAME_HEIGHT + ", \n" +
                ImagesTable.TABLE_NAME + "." + ImagesTable.COLUMN_NAME_TYPE +          " AS "+ ImagesTable.TABLE_NAME + "_" + ImagesTable.COLUMN_NAME_TYPE +

                " \nFROM " +        SourcesTable.TABLE_NAME +
                " \nLEFT JOIN " +   ArticlesTable.TABLE_NAME +
                " ON " +            ArticlesTable.TABLE_NAME + "." + ArticlesTable.COLUMN_NAME_SOURCE_ID + " = " + SourcesTable.TABLE_NAME + "." + SourcesTable._ID +
                " AND " +           ArticlesTable.TABLE_NAME + "." + ArticlesTable.COLUMN_NAME_PUBLISHED + " > " + validTime +
                " \nLEFT JOIN " +   ImagesTable.TABLE_NAME +
                " ON " +            ImagesTable.COLUMN_NAME_ARTICLE_ID + " = " + ArticlesTable.TABLE_NAME + "." + ArticlesTable._ID;

        Cursor cursor = db.rawQuery(query, null);

        while (cursor.moveToNext()) {
            long sourceDbId = getLong(cursor, SourcesTable.TABLE_NAME + SourcesTable._ID);
            if (!MainActivity.sources.containsDbId(sourceDbId)) {
                String sourceUrl = getString(cursor, SourcesTable.TABLE_NAME + "_" + SourcesTable.COLUMN_NAME_URL);
                String sourceTitle = getString(cursor, SourcesTable.TABLE_NAME + "_" + SourcesTable.COLUMN_NAME_TITLE);
                String sourceIcon = getString(cursor, SourcesTable.TABLE_NAME + "_" + SourcesTable.COLUMN_NAME_ICON_URL);
                String sourceIconFile = getString(cursor, SourcesTable.TABLE_NAME + "_" + SourcesTable.COLUMN_NAME_ICON_PATH);
                String sourceLink = getString(cursor, SourcesTable.TABLE_NAME + "_" + SourcesTable.COLUMN_NAME_WEBSITE);
                MainActivity.sources.add(new Source(context, sourceUrl, sourceTitle, sourceLink, sourceIcon, sourceIconFile, sourceDbId));
            }

            long articleDbId = getLong(cursor, ArticlesTable.TABLE_NAME + ArticlesTable._ID);
            if (!MainActivity.articles.containsDbId(articleDbId)) {
                String articleLink = getString(cursor, ArticlesTable.TABLE_NAME + "_" + ArticlesTable.COLUMN_NAME_URL);
                long articleSourceId = getLong(cursor, ArticlesTable.TABLE_NAME + "_" + ArticlesTable.COLUMN_NAME_SOURCE_ID);
                String articleTitle = getString(cursor, ArticlesTable.TABLE_NAME + "_" + ArticlesTable.COLUMN_NAME_TITLE);
                String articleAuthor = getString(cursor, ArticlesTable.TABLE_NAME + "_" + ArticlesTable.COLUMN_NAME_AUTHOR);
                long articlePublished = getLong(cursor, ArticlesTable.TABLE_NAME + "_" + ArticlesTable.COLUMN_NAME_PUBLISHED);
                String articleContent = getString(cursor, ArticlesTable.TABLE_NAME + "_" + ArticlesTable.COLUMN_NAME_CONTENT);
                Source source = MainActivity.sources.getByDbId(articleSourceId);
                MainActivity.articles.add(new Article(context, fragmentManager, articleTitle, articleAuthor, articleLink, articlePublished, articleContent, source, articleDbId));
            }

            long imageDbId = getLong(cursor, ImagesTable.TABLE_NAME + ImagesTable._ID);
            if(imageDbId != 0) {
                int imageArticleId = getInt(cursor, ImagesTable.TABLE_NAME + "_" + ImagesTable.COLUMN_NAME_ARTICLE_ID);
                String imageUrl = getString(cursor, ImagesTable.TABLE_NAME + "_" + ImagesTable.COLUMN_NAME_URL);
                String imagePath = getString(cursor, ImagesTable.TABLE_NAME + "_" + ImagesTable.COLUMN_NAME_PATH);
                int imageWidth = getInt(cursor, ImagesTable.TABLE_NAME + "_" + ImagesTable.COLUMN_NAME_WIDTH);
                int imageHeight = getInt(cursor, ImagesTable.TABLE_NAME + "_" + ImagesTable.COLUMN_NAME_HEIGHT);
                int imageIndex = getInt(cursor, ImagesTable.TABLE_NAME + "_" + ImagesTable.COLUMN_NAME_TYPE);

                Article article = MainActivity.articles.getByDbId(imageArticleId);
                if (imageIndex == Image.TYPE_HEADER) {
                    article.header = new Image(Image.TYPE_HEADER, imageDbId, imageUrl, imagePath, imageWidth, imageHeight);
                    article.getImage(null, Image.TYPE_HEADER);
                } else {
                    Image image = new Image(Image.TYPE_INLINE, imageDbId, imageUrl, imagePath, imageWidth, imageHeight);
                    if (article.inlineImages == null) article.inlineImages = new ArrayList<>();
                    for (int i = article.inlineImages.size() + 1; i <= imageIndex + 1; i++)
                        article.inlineImages.add(null);
                    article.inlineImages.set(imageIndex, image);
                }
            }
        }

        cursor.close();
        deleteOldArticles(validTime);
    }

    void createSource(Source source){
        getDb();
        ContentValues values = new ContentValues();
        values.put(SourcesTable.COLUMN_NAME_URL, source.rssUrl);
        values.put(SourcesTable.COLUMN_NAME_TITLE, source.title);
        if(source.icon.url!=null) values.put(SourcesTable.COLUMN_NAME_ICON_URL, source.icon.url);
        values.put(SourcesTable.COLUMN_NAME_WEBSITE, source.link);
        source.dbId = db.insert(SourcesTable.TABLE_NAME, null, values);
        MainActivity.sources.addDbId(source);
    }

    void deleteSource(Source source){
        db.delete(SourcesTable.TABLE_NAME, SourcesTable._ID + " = ?", new String[]{String.valueOf(source.dbId)});
    }

    void createArticles(List<Article> articles){
        getDb();
        for(int i = 0; i < articles.size(); i++) {
            Article article = articles.get(i);
            ContentValues values = new ContentValues();
            values.put(ArticlesTable.COLUMN_NAME_URL, article.link);
            values.put(ArticlesTable.COLUMN_NAME_SOURCE_ID, article.source.dbId);
            values.put(ArticlesTable.COLUMN_NAME_TITLE, article.title);
            values.put(ArticlesTable.COLUMN_NAME_AUTHOR, article.author);
            values.put(ArticlesTable.COLUMN_NAME_PUBLISHED, article.published.getTime());
            values.put(ArticlesTable.COLUMN_NAME_CONTENT, article.content);
            article.dbId = db.insert(ArticlesTable.TABLE_NAME, null, values);
        }
    }

    private int deleteOldArticles(long validTime) {

        String query = "SELECT " + ImagesTable.TABLE_NAME + "." + ImagesTable.COLUMN_NAME_PATH + ", " +
                ImagesTable.TABLE_NAME + "." + ImagesTable._ID +
                " FROM " + ImagesTable.TABLE_NAME +
                " INNER JOIN " + ArticlesTable.TABLE_NAME +
                " ON " + ImagesTable.TABLE_NAME + "." + ImagesTable.COLUMN_NAME_ARTICLE_ID +
                " = " + ArticlesTable.TABLE_NAME + "." + ArticlesTable._ID +
                " WHERE " + ArticlesTable.TABLE_NAME + "." + ArticlesTable.COLUMN_NAME_PUBLISHED + " < ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(validTime)});

        while(cursor.moveToNext()){
            // delete from MainActivity.articles
            Long dbId = getLong(cursor, ArticlesTable._ID);
            if(MainActivity.articles.containsDbId(dbId)) MainActivity.articles.getByDbId(dbId).destroy();
            if(MainActivity.articles.containsDbId(dbId)) MainActivity.articles.removeByDbId(dbId);
        }
        cursor.close();
        // delete db entry
        return db.delete(ArticlesTable.TABLE_NAME, ArticlesTable.COLUMN_NAME_PUBLISHED+"<?", new String[]{String.valueOf(validTime)});
    }

    long insertImage(ContentValues values){
        return db.insert(ImagesTable.TABLE_NAME, null, values);
    }

    void updateValue(String table, String column, String value, String whereColumn, String whereValue){
        getDb();
        ContentValues values = new ContentValues();
        values.put(column, value);
        db.update(table, values, whereColumn + "= ?", new String[]{whereValue});
    }

    private String getString(Cursor cursor, String column){
        return cursor.getString(cursor.getColumnIndexOrThrow(column));
    }

    private Long getLong(Cursor cursor, String column){
        return cursor.getLong(cursor.getColumnIndexOrThrow(column));
    }

    private Integer getInt(Cursor cursor, String column){
        return cursor.getInt(cursor.getColumnIndexOrThrow(column));
    }

    @Override
    public String toString(){
        String debug = super.toString() + "\n";
        String query =
                "SELECT " + SourcesTable.TABLE_NAME + "." + SourcesTable._ID + " AS "+ SourcesTable.TABLE_NAME + SourcesTable._ID + ", " +
                        ArticlesTable.TABLE_NAME +"." + ArticlesTable._ID + " AS "+ ArticlesTable.TABLE_NAME + ArticlesTable._ID + ", " +
                        ImagesTable.TABLE_NAME + "." + ImagesTable._ID + " AS "+ ImagesTable.TABLE_NAME + ImagesTable._ID +
                        " \nFROM " + SourcesTable.TABLE_NAME +
                        " \nLEFT JOIN " + ArticlesTable.TABLE_NAME +
                        " ON " + ArticlesTable.COLUMN_NAME_SOURCE_ID +
                        " = " + SourcesTable.TABLE_NAME + "." + SourcesTable._ID +
                        " \nLEFT JOIN " + ImagesTable.TABLE_NAME +
                        " ON " + ImagesTable.COLUMN_NAME_ARTICLE_ID +
                        " = " + ArticlesTable.TABLE_NAME + "." + ArticlesTable._ID;
        Cursor cursor = db.rawQuery(query, null);

        debug += "Query:\n" + query + "\n\nSources\tArticles\tImages\n";

        while (cursor.moveToNext()){
            long sourceDbId = getLong(cursor, SourcesTable.TABLE_NAME + SourcesTable._ID);
            long articleDbId = getLong(cursor, ArticlesTable.TABLE_NAME + ArticlesTable._ID);
            long imageDbId = getLong(cursor, ImagesTable.TABLE_NAME + ImagesTable._ID);
            debug += sourceDbId + "\t\t\t" +
                    articleDbId + "\t\t\t" +
                    imageDbId + "\n";
        }
        cursor.close();
        return debug;
    }

    class SourcesTable implements BaseColumns {
        static final String TABLE_NAME = "sources";
        static final String COLUMN_NAME_URL = "url";
        static final String COLUMN_NAME_TITLE = "title";
        static final String COLUMN_NAME_ICON_URL = "icon_url";
        static final String COLUMN_NAME_ICON_PATH = "icon_path";
        static final String COLUMN_NAME_WEBSITE = "website";
    }

    private class ArticlesTable implements BaseColumns {
        static final String TABLE_NAME = "articles";
        static final String COLUMN_NAME_SOURCE_ID = "source_id";
        static final String COLUMN_NAME_URL = "url";
        static final String COLUMN_NAME_TITLE = "title";
        static final String COLUMN_NAME_AUTHOR = "author";
        static final String COLUMN_NAME_PUBLISHED = "published";
        static final String COLUMN_NAME_CONTENT = "content";
    }

    class ImagesTable implements BaseColumns {
        static final String TABLE_NAME = "images";
        static final String COLUMN_NAME_ARTICLE_ID = "article_id";
        static final String COLUMN_NAME_URL = "url";
        static final String COLUMN_NAME_PATH = "path";
        static final String COLUMN_NAME_WIDTH = "width";
        static final String COLUMN_NAME_HEIGHT = "height";
        static final String COLUMN_NAME_TYPE = "type";
    }
}