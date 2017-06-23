package com.timkonieczny.rss;

import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

class Feed extends AsyncTask<Void, Void, Boolean> {

    private FeedListener feedListener;

    private HashSet<String> existingIds;
    private Comparator<Article> descending;

    private FragmentManager fragmentManager;

    private SharedPreferences sharedPreferences;

    Feed(Object listener, FragmentManager fragmentManager, SharedPreferences sharedPreferences){

        this.fragmentManager = fragmentManager;
        this.feedListener = (FeedListener)listener;
        this.sharedPreferences = sharedPreferences;

        descending = new Comparator<Article>() {
            @Override
            public int compare(Article a1, Article a2) {
                return a2.published.compareTo(a1.published);
            }
        };

        existingIds = getExistingArticlesIds();
    }

    @Override
    protected final Boolean doInBackground(Void... params) {

        List<Article> articles = new ArrayList<>(0);

        for (Map.Entry<String, Source> entry : MainActivity.sources.entrySet()) {

            String currentURL = entry.getKey();
            try {
                HttpURLConnection connection = (HttpURLConnection) (new URL(currentURL)).openConnection();

                connection.setReadTimeout(10000);
                connection.setConnectTimeout(15000);

                // Starts the query
                connection.connect();
                InputStream stream = connection.getInputStream();

                SourceUpdater sourceUpdater = new SourceUpdater();

                if(MainActivity.sources.get(currentURL).isStub){
                    articles.addAll(sourceUpdater.parse(stream, MainActivity.sources.get(currentURL), true));

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("sources", sharedPreferences.getString("sources", "") + " "+currentURL);
                    editor.putString(currentURL + "_id", sourceUpdater.source.id);
                    editor.putString(currentURL + "_title", sourceUpdater.source.title);
                    if(sourceUpdater.source.icon!=null) editor.putString(currentURL + "_icon", sourceUpdater.source.icon);
                    editor.putString(currentURL + "_link", sourceUpdater.source.link.toString());
                    editor.putInt(currentURL + "_updated", (int)sourceUpdater.source.updated.getTime());
                    editor.apply();

                    MainActivity.sources.get(currentURL).isStub = false;
                }else{
                    articles.addAll(sourceUpdater.parse(stream, MainActivity.sources.get(currentURL), false));
                }

            } catch (IOException | XmlPullParserException e) {
                e.printStackTrace();
            }
        }

        Article article;
        for(int i = 0; i < articles.size(); i++){
            article = articles.get(i);
            article.uniqueId = article.source.id + "_" + article.id;
            if(existingIds.contains(article.uniqueId)){
                articles.remove(i);
                i--;
            }else{
                article.onClickListener = new ArticleOnClickListener(article, fragmentManager);
                if(article.headerImage!=null) article.updateHeaderImage();
            }
        }

        MainActivity.articles.addAll(articles);
        Collections.sort(MainActivity.articles, descending);

        return articles.size() > 0;
    }

    @Override
    protected void onPostExecute(Boolean hasNewArticles) {
        super.onPostExecute(hasNewArticles);
        if(feedListener!=null) feedListener.onFeedUpdated(hasNewArticles);
    }

    private HashSet<String> getExistingArticlesIds(){
        HashSet<String> ids = new HashSet<>();
        for(int i = 0; i < MainActivity.articles.size(); i++){
            ids.add(MainActivity.articles.get(i).uniqueId);
        }
        return ids;
    }
}