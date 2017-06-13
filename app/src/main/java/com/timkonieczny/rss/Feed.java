package com.timkonieczny.rss;

import android.app.FragmentManager;
import android.os.AsyncTask;
import android.util.Log;

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

    Feed(Object listener, FragmentManager fragmentManager){

        this.fragmentManager = fragmentManager;
        this.feedListener = (FeedListener)listener;

        descending = new Comparator<Article>() {
            @Override
            public int compare(Article a1, Article a2) {
                return a2.published.compareTo(a1.published);
//                return (int)(a2.published.getTime()-a1.published.getTime());
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
                    MainActivity.sources.get(currentURL).isStub = false;
                }else{
                    articles.addAll(sourceUpdater.parse(stream, MainActivity.sources.get(currentURL), false));
                }

            } catch (IOException | XmlPullParserException e) {
                e.printStackTrace();
            }
        }

       /* for(int i = 0; i < MainActivity.articles.size(); i++){
            Article article1 = MainActivity.articles.get(i);
            Log.d("Feed", "Before adding: "+article1.source.title + "\t" + article1.published);
        }

        for(int i = 0; i < articles.size(); i++){
            Article article1 = articles.get(i);
            Log.d("Feed", "Articles to add: "+article1.source.title + "\t" + article1.published);
        }*/

        Article article;
        for(int i = 0; i < articles.size(); i++){
            article = articles.get(i);
            article.uniqueId = article.source.id + "_" + article.id;
            if(existingIds.contains(article.uniqueId)){
                articles.remove(i);
                i--;
            }else{
                article.onClickListener = new ArticleOnClickListener(article, fragmentManager);
                article.updateHeaderImage();
            }
        }

        MainActivity.articles.addAll(articles);
        Collections.sort(MainActivity.articles, descending);

        for(int i = 0; i < MainActivity.articles.size(); i++){
            Article article1 = MainActivity.articles.get(i);
            Log.d("Feed", "After adding: "+article1.published+"\t"+article1.source.title + "\t" + article1.title);
        }

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