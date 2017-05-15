package com.timkonieczny.rss;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends Activity implements FeedListener, SwipeRefreshLayout.OnRefreshListener {

    FeedAdapter feedAdapter;
    ArrayList<Article> articles;
    Comparator<Article> descending;

    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        articles = new ArrayList<>(0);
        descending = new Comparator<Article>() {
            @Override
            public int compare(Article a1, Article a2) {
                return a2.published.compareTo(a1.published);
            }
        };

        feedAdapter = new FeedAdapter(articles);

        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(feedAdapter);

        swipeRefreshLayout = ((SwipeRefreshLayout)findViewById(R.id.swipe_refresh_layout));
        swipeRefreshLayout.setOnRefreshListener(this);

        updateFeed();
    }

    @Override
    public void onRefresh() {   // FIXME: Add invisible button to title bar for accessibility
        updateFeed();
    }

    @Override
    public void onFeedUpdated(ArrayList<Article> articles) {
        this.articles = articles;

        feedAdapter.articles.addAll(this.articles);
        Collections.sort(feedAdapter.articles, descending);

        feedAdapter.notifyDataSetChanged();
        swipeRefreshLayout.setRefreshing(false);
    }

    public void updateFeed(){
        try {
            (new Feed(this, articles)).execute(new URL("https://www.theverge.com/rss/index.xml"));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
