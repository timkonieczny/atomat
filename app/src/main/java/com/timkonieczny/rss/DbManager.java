package com.timkonieczny.rss;

import android.app.FragmentManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

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

    void load(Resources resources, Context context, FragmentManager fragmentManager, long articleLifetime){
        getDb();
        loadSources(resources, context);
        loadArticles(resources, context, fragmentManager, articleLifetime);
    }

    private void loadSources(Resources resources, Context context){
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
            Long dbId = cursor.getLong(cursor.getColumnIndexOrThrow(SourcesTable._ID));

            if (!MainActivity.sources.containsDbId(dbId)) {    // only create source if it doesn't exist yet
                MainActivity.sources.add(new Source(context, resources, url,
                        cursor.getString(cursor.getColumnIndexOrThrow(SourcesTable.COLUMN_NAME_TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(SourcesTable.COLUMN_NAME_LINK)),
                        cursor.getString(cursor.getColumnIndexOrThrow(SourcesTable.COLUMN_NAME_ICON)),
                        cursor.getString(cursor.getColumnIndexOrThrow(SourcesTable.COLUMN_NAME_ICON_FILE)),
                        dbId
                ));
            }
        }
        cursor.close();
    }

    private void loadArticles(Resources resources, Context context,
                              FragmentManager fragmentManager, long articleLifetime){
        // Get sources from Db
        String[] columns = {
                ArticlesTable._ID,                              ArticlesTable.COLUMN_NAME_LINK,
                ArticlesTable.COLUMN_NAME_SOURCE_ID,            ArticlesTable.COLUMN_NAME_TITLE,
                ArticlesTable.COLUMN_NAME_AUTHOR,               ArticlesTable.COLUMN_NAME_PUBLISHED,
                ArticlesTable.COLUMN_NAME_CONTENT,              ArticlesTable.COLUMN_NAME_HEADER_IMAGE,
                ArticlesTable.COLUMN_NAME_HEADER_IMAGE_FILE,    ArticlesTable.COLUMN_NAME_INLINE_IMAGES,
                ArticlesTable.COLUMN_NAME_INLINE_IMAGES_FILES
        };

        // get valid articles
        long validTime = System.currentTimeMillis()-articleLifetime;
        Cursor cursor = db.query(ArticlesTable.TABLE_NAME, columns,
                ArticlesTable.COLUMN_NAME_PUBLISHED+">=?", new String[]{String.valueOf(validTime)},
                null, null, null);

        // Convert sources to Source objects
        while (cursor.moveToNext()) {
            int sourceDbId = cursor.getInt(cursor.getColumnIndexOrThrow(ArticlesTable.COLUMN_NAME_SOURCE_ID));
            long dbId = cursor.getLong(cursor.getColumnIndexOrThrow(ArticlesTable._ID));
            String link = cursor.getString(cursor.getColumnIndexOrThrow(ArticlesTable.COLUMN_NAME_LINK));
            long published = cursor.getLong(cursor.getColumnIndexOrThrow(ArticlesTable.COLUMN_NAME_PUBLISHED));

            if(MainActivity.sources.containsDbId(sourceDbId) && published+articleLifetime > System.currentTimeMillis()){
                if(!MainActivity.articles.containsDbId(dbId)) {
                    String[] inlineImageUrls = splitString(cursor.getString(cursor.getColumnIndexOrThrow(ArticlesTable.COLUMN_NAME_INLINE_IMAGES)));
                    String[] inlineImageFiles = splitString(cursor.getString(cursor.getColumnIndexOrThrow(ArticlesTable.COLUMN_NAME_INLINE_IMAGES_FILES)));

                    Article article = new Article(context, resources, fragmentManager,
                            cursor.getString(cursor.getColumnIndexOrThrow(ArticlesTable.COLUMN_NAME_TITLE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(ArticlesTable.COLUMN_NAME_AUTHOR)),
                            link,
                            new Date(published),
                            cursor.getString(cursor.getColumnIndexOrThrow(ArticlesTable.COLUMN_NAME_CONTENT)),
                            cursor.getString(cursor.getColumnIndexOrThrow(ArticlesTable.COLUMN_NAME_HEADER_IMAGE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(ArticlesTable.COLUMN_NAME_HEADER_IMAGE_FILE)),
                            inlineImageUrls, inlineImageFiles, (Source) MainActivity.sources.getByDbId(sourceDbId),
                            cursor.getInt(cursor.getColumnIndexOrThrow(ArticlesTable._ID)));
                    article.getImage(null, Article.HEADER);
                    MainActivity.articles.add(article);
                }
            }
        }
        cursor.close();

        // delete invalid articles
        deleteOldArticles(context, validTime);
    }

    private int deleteOldArticles(Context context, long validTime) {
        Cursor cursor = db.query(ArticlesTable.TABLE_NAME,
                new String[]{
//                        ArticlesTable.COLUMN_NAME_LINK,
                        ArticlesTable._ID,
                        ArticlesTable.COLUMN_NAME_HEADER_IMAGE_FILE,
                        ArticlesTable.COLUMN_NAME_INLINE_IMAGES_FILES},
                ArticlesTable.COLUMN_NAME_PUBLISHED+"<?", new String[]{String.valueOf(validTime)}, null, null, null, null);
        while(cursor.moveToNext()){
            // delete from MainActivity.articles
            Long dbId = cursor.getLong(cursor.getColumnIndexOrThrow(ArticlesTable._ID));
            if(MainActivity.articles.containsDbId(dbId)) MainActivity.articles.removeByDbId(dbId);
            // delete header image file
            context.deleteFile(cursor.getString(
                    cursor.getColumnIndexOrThrow(ArticlesTable.COLUMN_NAME_HEADER_IMAGE_FILE)));
            // delete inline image files
            for (String inlineImage : cursor.getString(cursor.getColumnIndexOrThrow(ArticlesTable.COLUMN_NAME_INLINE_IMAGES_FILES)).split(" "))
                context.deleteFile(inlineImage);
        }
        cursor.close();
        // delete db entry
        return db.delete(ArticlesTable.TABLE_NAME, ArticlesTable.COLUMN_NAME_PUBLISHED+"<?", new String[]{String.valueOf(validTime)});
    }

    private String[] splitString(String string){
        String[] inlineImageUrls = null;
        if(string != null) {
            inlineImageUrls = string.split(" ");
            for (int i = 0; i < inlineImageUrls.length; i++)
                if(inlineImageUrls[i].equals("null")) inlineImageUrls[i] = null;
        }
        return inlineImageUrls;
    }

    private long getSourceId(String rssUrl){
        Cursor cursor = db.query(SourcesTable.TABLE_NAME, new String[]{SourcesTable._ID},
                SourcesTable.COLUMN_NAME_URL+"=?", new String[]{rssUrl},
                null, null, null, null);
        cursor.moveToFirst();
        long sourceId = cursor.getLong(cursor.getColumnIndexOrThrow(SourcesTable._ID));
        cursor.close();
        return sourceId;
    }

    void createSource(Source source){
        getDb();
        ContentValues values = new ContentValues();
        values.put(SourcesTable.COLUMN_NAME_URL, source.rssUrl);
        values.put(SourcesTable.COLUMN_NAME_TITLE, source.title);
        if(source.icon.url!=null) values.put(SourcesTable.COLUMN_NAME_ICON, source.icon.url);
        values.put(SourcesTable.COLUMN_NAME_LINK, source.link);
        db.insert(SourcesTable.TABLE_NAME, null, values);
        source.dbId = getSourceId(source.rssUrl);
        MainActivity.sources.addDbId(source.dbId);
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
            long sourceId = cursor.getLong(cursor.getColumnIndexOrThrow(SourcesTable._ID));
            cursor.close();
            ContentValues values = new ContentValues();
            values.put(ArticlesTable.COLUMN_NAME_LINK, article.link);
            values.put(ArticlesTable.COLUMN_NAME_SOURCE_ID, sourceId);
            values.put(ArticlesTable.COLUMN_NAME_TITLE, article.title);
            values.put(ArticlesTable.COLUMN_NAME_AUTHOR, article.author);
            values.put(ArticlesTable.COLUMN_NAME_PUBLISHED, article.published.getTime());
            values.put(ArticlesTable.COLUMN_NAME_CONTENT, article.content);
            values.put(ArticlesTable.COLUMN_NAME_HEADER_IMAGE, article.header.url);
            article.dbId = db.insert(ArticlesTable.TABLE_NAME, null, values);
        }
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
}