package com.timkonieczny.rss;

import android.app.ListActivity;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;

public class MainActivity extends ListActivity implements FeedListener, SwipeRefreshLayout.OnRefreshListener {

    FeedAdapter adapter;
    ArrayList<Entry> entries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        entries = new ArrayList<>(0);

        adapter = new FeedAdapter(this, R.layout.article_list_item, entries);
        this.setListAdapter(adapter);

        ((SwipeRefreshLayout)findViewById(R.id.swipe_refresh_layout)).setOnRefreshListener(this);



        updateSources();
    }

    @Override
    public void onSourcesUpdated(ArrayList<Entry> entries) {
        this.entries = entries;
        adapter.addAll(this.entries);
        adapter.sort(new Comparator<Entry>() {

            @Override
            public int compare(Entry o1, Entry o2) {
                return o2.published.compareTo(o1.published);
            }
        });
        adapter.notifyDataSetChanged();
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
