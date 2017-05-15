package com.timkonieczny.rss;

import android.app.Activity;
import android.app.ListActivity;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;

public class MainActivity extends Activity implements FeedListener, SwipeRefreshLayout.OnRefreshListener {

    FeedAdapter adapter;
    FeedAdapter2 feedAdapter2;
    ArrayList<Entry> entries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        entries = new ArrayList<>(0);

//        adapter = new FeedAdapter(this, R.layout.article_list_item, entries);
//        this.setListAdapter(adapter);

        ((SwipeRefreshLayout)findViewById(R.id.swipe_refresh_layout)).setOnRefreshListener(this);


        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        feedAdapter2 = new FeedAdapter2(entries);
        recyclerView.setAdapter(feedAdapter2);

        /*LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        FeedAdapter2 feedAdapter2 = new FeedAdapter2(entries);
        recyclerView.setAdapter(feedAdapter2);*/

        updateSources();
    }

    @Override
    public void onSourcesUpdated(ArrayList<Entry> entries) {
        this.entries = entries;
        feedAdapter2.entries.addAll(this.entries);
        feedAdapter2.entries.sort(new Comparator<Entry>() {

            @Override
            public int compare(Entry o1, Entry o2) {
                return o2.published.compareTo(o1.published);
            }
        });

        /*adapter.addAll(this.entries);
        adapter.sort(new Comparator<Entry>() {

            @Override
            public int compare(Entry o1, Entry o2) {
                return o2.published.compareTo(o1.published);
            }
        });*/
        feedAdapter2.notifyDataSetChanged();
        ((SwipeRefreshLayout)findViewById(R.id.swipe_refresh_layout)).setRefreshing(false);
    }

    @Override
    public void onRefresh() {
        updateSources();
    }

    public void updateSources(){
        try {
            (new Feed(this, entries)).execute(new URL("https://www.theverge.com/rss/index.xml"));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
