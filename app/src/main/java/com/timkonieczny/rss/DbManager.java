package com.timkonieczny.rss;

import android.app.FragmentManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.Date;
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
                SourcesTable._ID +                              " INTEGER PRIMARY KEY," +
                SourcesTable.COLUMN_NAME_URL +                  " TEXT," +
                SourcesTable.COLUMN_NAME_TITLE +                " TEXT," +
                SourcesTable.COLUMN_NAME_ICON +                 " TEXT," +
                SourcesTable.COLUMN_NAME_ICON_FILE +            " TEXT," +
                SourcesTable.COLUMN_NAME_LINK +                 " TEXT)");

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

        db.execSQL("CREATE TABLE " + ImagesTable.TABLE_NAME + " (" +
                ImagesTable._ID +                               " INTEGER PRIMARY KEY," +
                ImagesTable.COLUMN_NAME_ARTICLE_ID +            " INTEGER," +
                ImagesTable.COLUMN_NAME_WIDTH +                 " INTEGER," +
                ImagesTable.COLUMN_NAME_HEIGHT +                " INTEGER," +
                ImagesTable.COLUMN_NAME_URL +                   " TEXT," +
                ImagesTable.COLUMN_NAME_PATH +                  " TEXT," +
                ImagesTable.COLUMN_NAME_ABSOLUTE_PATH +         " TEXT," +
                ImagesTable.COLUMN_NAME_INDEX +                 " INTEGER)");

    }

    void load(Context context, FragmentManager fragmentManager, long articleLifetime){
        getDb();
        loadSources(context);
        loadArticles(context, fragmentManager, articleLifetime);
    }

    private void loadSources(Context context){
        // Get sources from Db
        String[] projection = {
                SourcesTable._ID,                   SourcesTable.COLUMN_NAME_ICON,
                SourcesTable.COLUMN_NAME_ICON_FILE, SourcesTable.COLUMN_NAME_LINK,
                SourcesTable.COLUMN_NAME_TITLE,     SourcesTable.COLUMN_NAME_URL
        };
        Cursor cursor = db.query(SourcesTable.TABLE_NAME, projection, null, null, null, null, null);

        // Convert sources to Source objects
        while (cursor.moveToNext()) {
            String url = getString(cursor, SourcesTable.COLUMN_NAME_URL);
            Long dbId = getLong(cursor, SourcesTable._ID);

            if (!MainActivity.sources.containsDbId(dbId)) {    // only create source if it doesn't exist yet
                MainActivity.sources.add(new Source(context, url,
                        getString(cursor, SourcesTable.COLUMN_NAME_TITLE),
                        getString(cursor, SourcesTable.COLUMN_NAME_LINK),
                        getString(cursor, SourcesTable.COLUMN_NAME_ICON),
                        getString(cursor, SourcesTable.COLUMN_NAME_ICON_FILE),
                        dbId
                ));
            }
        }
        cursor.close();
    }

    private void loadArticles(Context context,
                              FragmentManager fragmentManager, long articleLifetime){

        // get valid articles
        long validTime = System.currentTimeMillis()-articleLifetime;

        String query = "SELECT " +
                ArticlesTable.TABLE_NAME + "." + ArticlesTable._ID + ", " +
                ArticlesTable.TABLE_NAME + "." + ArticlesTable.COLUMN_NAME_LINK + ", " +
                ArticlesTable.TABLE_NAME + "." + ArticlesTable.COLUMN_NAME_SOURCE_ID + ", " +
                ArticlesTable.TABLE_NAME + "." + ArticlesTable.COLUMN_NAME_TITLE + ", " +
                ArticlesTable.TABLE_NAME + "." + ArticlesTable.COLUMN_NAME_AUTHOR + ", " +
                ArticlesTable.TABLE_NAME + "." + ArticlesTable.COLUMN_NAME_PUBLISHED + ", " +
                ArticlesTable.TABLE_NAME + "." + ArticlesTable.COLUMN_NAME_CONTENT + ", " +
                ImagesTable.TABLE_NAME + "." + ImagesTable.COLUMN_NAME_URL + ", " +
                ImagesTable.TABLE_NAME + "." + ImagesTable.COLUMN_NAME_PATH + ", " +
                ImagesTable.TABLE_NAME + "." + ImagesTable.COLUMN_NAME_ABSOLUTE_PATH + ", " +
                ImagesTable.TABLE_NAME + "." + ImagesTable.COLUMN_NAME_WIDTH + ", " +
                ImagesTable.TABLE_NAME + "." + ImagesTable.COLUMN_NAME_HEIGHT + ", " +
                ImagesTable.TABLE_NAME + "." + ImagesTable.COLUMN_NAME_INDEX +
                " FROM " + ArticlesTable.TABLE_NAME +
                " LEFT OUTER JOIN " + ImagesTable.TABLE_NAME +
                " ON " + ArticlesTable.TABLE_NAME + "." + ArticlesTable._ID +
                " = " + ImagesTable.TABLE_NAME + "." + ImagesTable.COLUMN_NAME_ARTICLE_ID +
                " AND " + ArticlesTable.TABLE_NAME + "." + ArticlesTable.COLUMN_NAME_PUBLISHED +
                " >= " + validTime;

        Cursor cursor = db.rawQuery(query, null);

        // Convert sources to Source objects
        while (cursor.moveToNext()) {
            long sourceDbId = getLong(cursor, ArticlesTable.COLUMN_NAME_SOURCE_ID);
            long dbId = getLong(cursor, ArticlesTable._ID);
            String link = getString(cursor, ArticlesTable.COLUMN_NAME_LINK);
            long published = getLong(cursor, ArticlesTable.COLUMN_NAME_PUBLISHED);

            if(MainActivity.sources.containsDbId(sourceDbId) && published+articleLifetime > System.currentTimeMillis()){
                Article article;
                if(!MainActivity.articles.containsDbId(dbId)) {
                    article = new Article(context, fragmentManager,
                            getString(cursor, ArticlesTable.COLUMN_NAME_TITLE),
                            getString(cursor, ArticlesTable.COLUMN_NAME_AUTHOR),
                            link,
                            new Date(published),
                            getString(cursor, ArticlesTable.COLUMN_NAME_CONTENT),
                            MainActivity.sources.getByDbId(sourceDbId),
                            getLong(cursor, ArticlesTable._ID));
                    MainActivity.articles.add(article);
                }else article = MainActivity.articles.getByDbId(dbId);

                Image image = new Image();
                image.url = getString(cursor, ImagesTable.COLUMN_NAME_URL);
                image.fileName = getString(cursor, ImagesTable.COLUMN_NAME_PATH);
                image.absolutePath = getString(cursor, ImagesTable.COLUMN_NAME_ABSOLUTE_PATH);
                image.width = getInt(cursor, ImagesTable.COLUMN_NAME_WIDTH);
                image.height = getInt(cursor, ImagesTable.COLUMN_NAME_HEIGHT);

                int imageIndex = getInt(cursor, ImagesTable.COLUMN_NAME_INDEX);

                if(imageIndex == Article.HEADER){
                    article.header = image;
                    article.getImage(null, Article.HEADER);
                }else{
                    if(article.inlineImages == null) article.inlineImages = new ArrayList<>();
                    for(int i = article.inlineImages.size() + 1; i <= imageIndex+1; i++)
                        article.inlineImages.add(null);
                    article.inlineImages.set(imageIndex, image);
                }
            }
        }
        cursor.close();

        // delete invalid articles
        deleteOldArticles(context, validTime);
    }

    private int deleteOldArticles(Context context, long validTime) {

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
            if(MainActivity.articles.containsDbId(dbId)) MainActivity.articles.removeByDbId(dbId);
            // delete header image file
            context.deleteFile(getString(cursor, ImagesTable.COLUMN_NAME_PATH));
            // delete inline image files
            db.delete(ImagesTable.TABLE_NAME, ImagesTable._ID + " = ?", new String[]{getString(cursor, ImagesTable._ID)});
        }
        cursor.close();
        // delete db entry
        return db.delete(ArticlesTable.TABLE_NAME, ArticlesTable.COLUMN_NAME_PUBLISHED+"<?", new String[]{String.valueOf(validTime)});
    }

    void createSource(Source source){
        getDb();
        ContentValues values = new ContentValues();
        values.put(SourcesTable.COLUMN_NAME_URL, source.rssUrl);
        values.put(SourcesTable.COLUMN_NAME_TITLE, source.title);
        if(source.icon.url!=null) values.put(SourcesTable.COLUMN_NAME_ICON, source.icon.url);
        values.put(SourcesTable.COLUMN_NAME_LINK, source.link);
        source.dbId = db.insert(SourcesTable.TABLE_NAME, null, values);
        MainActivity.sources.addDbId(source);
    }

    void updateValue(String table, String column, String value, String whereColumn, String whereValue){
        getDb();
        ContentValues values = new ContentValues();
        values.put(column, value);
        db.update(table, values, whereColumn + "= ?", new String[]{whereValue});
    }

    void createArticles(List<Article> articles){
        getDb();
        for(int i = 0; i < articles.size(); i++) {
            Article article = articles.get(i);

            Cursor cursor = db.query(
                    SourcesTable.TABLE_NAME,
                    new String[]{SourcesTable._ID},
                    SourcesTable.COLUMN_NAME_URL+"=?",
                    new String[]{article.source.rssUrl},
                    null, null, null, null);
            cursor.moveToFirst();
            long sourceId = getLong(cursor, SourcesTable._ID);
            cursor.close();
            ContentValues values = new ContentValues();
            values.put(ArticlesTable.COLUMN_NAME_LINK, article.link);
            values.put(ArticlesTable.COLUMN_NAME_SOURCE_ID, sourceId);
            values.put(ArticlesTable.COLUMN_NAME_TITLE, article.title);
            values.put(ArticlesTable.COLUMN_NAME_AUTHOR, article.author);
            values.put(ArticlesTable.COLUMN_NAME_PUBLISHED, article.published.getTime());
            values.put(ArticlesTable.COLUMN_NAME_CONTENT, article.content);
            article.dbId = db.insert(ArticlesTable.TABLE_NAME, null, values);
            values.clear();
            values.put(ImagesTable.COLUMN_NAME_URL, article.header.url);
            values.put(ImagesTable.COLUMN_NAME_ARTICLE_ID, article.dbId);
            values.put(ImagesTable.COLUMN_NAME_INDEX, Article.HEADER);
            db.insert(ImagesTable.TABLE_NAME, null, values);
        }
    }

    void updateImage(ContentValues values, long dbId, int index){
        db.update(DbManager.ImagesTable.TABLE_NAME, values,
                DbManager.ImagesTable.COLUMN_NAME_ARTICLE_ID + " = ? AND " + DbManager.ImagesTable.COLUMN_NAME_INDEX + " = ?",
                new String[]{String.valueOf(dbId), String.valueOf(index)}
        );
    }

    void insertImage(ContentValues values){
        db.insert(ImagesTable.TABLE_NAME, null, values);
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

    class SourcesTable implements BaseColumns {
        static final String TABLE_NAME = "sources";
        static final String COLUMN_NAME_URL = "url";
        static final String COLUMN_NAME_TITLE = "title";
        static final String COLUMN_NAME_ICON = "icon";
        static final String COLUMN_NAME_ICON_FILE = "icon_file";
        static final String COLUMN_NAME_LINK = "link";
    }

    private class ArticlesTable implements BaseColumns {
        static final String TABLE_NAME = "articles";

        static final String COLUMN_NAME_LINK = "link";
        static final String COLUMN_NAME_SOURCE_ID = "source_id";
        static final String COLUMN_NAME_TITLE = "title";
        static final String COLUMN_NAME_AUTHOR = "author";
        static final String COLUMN_NAME_PUBLISHED = "published";
        static final String COLUMN_NAME_CONTENT = "content";
        static final String COLUMN_NAME_HEADER_IMAGE = "header_image";
        static final String COLUMN_NAME_HEADER_IMAGE_FILE = "header_image_file";
        static final String COLUMN_NAME_INLINE_IMAGES = "inline_images";
        static final String COLUMN_NAME_INLINE_IMAGES_FILES = "inline_images_files";
    }

    // TODO: Add palette to Db?
    class ImagesTable implements BaseColumns {
        static final String TABLE_NAME = "images";
        static final String COLUMN_NAME_ARTICLE_ID = "article_id";
        static final String COLUMN_NAME_URL = "url";
        static final String COLUMN_NAME_PATH = "path";
        static final String COLUMN_NAME_ABSOLUTE_PATH = "absolute_path";
        static final String COLUMN_NAME_WIDTH = "width";
        static final String COLUMN_NAME_HEIGHT = "height";
        static final String COLUMN_NAME_INDEX = "is_integer";
    }
}