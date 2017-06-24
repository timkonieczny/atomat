package com.timkonieczny.rss;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

public class OverviewFragment extends Fragment implements FeedListener, UpdateHeaderImageListener, UpdateIconImageListener, SwipeRefreshLayout.OnRefreshListener{

    FeedAdapter feedAdapter;
    Comparator<Article> descending;

    SwipeRefreshLayout swipeRefreshLayout;
    Snackbar noUpdatesSnackbar;

    boolean isInitialRefreshDone = false;

    // Required empty public constructor
    public OverviewFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        descending = new Comparator<Article>() {
            @Override
            public int compare(Article a1, Article a2) {
                return a2.published.compareTo(a1.published);
            }
        };

        if(!isInitialRefreshDone) getSourcesFromSharedPreferences();

        if(MainActivity.articles == null) MainActivity.articles = new ArrayList<>();

        feedAdapter = new FeedAdapter();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_overview, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        RecyclerView recyclerView = (RecyclerView)view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(feedAdapter);

        swipeRefreshLayout = ((SwipeRefreshLayout)view.findViewById(R.id.swipe_refresh_layout));
        swipeRefreshLayout.setOnRefreshListener(this);

        noUpdatesSnackbar = Snackbar.make(view, getResources().getString(R.string.no_updates_snackbar), Snackbar.LENGTH_SHORT);

        if(!isInitialRefreshDone) {
            swipeRefreshLayout.setRefreshing(true);
            updateFeed();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onRefresh() {
        updateFeed();
    }

    @Override
    public void onIconImageUpdated(Source source) {
        feedAdapter.notifyDataSetChanged();     // This is fine since icon only needs to be loaded once.
    }

    @Override
    public void onHeaderImageUpdated(Article article) {

    }

    @Override
    public void onFeedUpdated(boolean hasNewArticles) { // TODO: Save article header and content in storage
        isInitialRefreshDone = true;
        if(hasNewArticles)
            feedAdapter.notifyDataSetChanged();
        else
            noUpdatesSnackbar.show();

        swipeRefreshLayout.setRefreshing(false);
    }

    public void updateFeed(){

        // TODO: The Verge: Feed only contains article previews. But ID is also a feed URL containing full articles.

//        MainActivity.sources.put("https://www.theverge.com/rss/index.xml", new Source(this, getResources()));
        (new Feed(this, getFragmentManager(), getActivity().getPreferences(Context.MODE_PRIVATE))).execute();
    }

    public void getSourcesFromSharedPreferences(){
        if(MainActivity.sources == null) MainActivity.sources = new HashMap<>();

        SharedPreferences sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);

        for (String key : sharedPreferences.getString("sources", "").split(" ")) {
            if(!key.equals("")) {
                Source source = new Source(getResources(), getContext(), key);
                source.id = sharedPreferences.getString(key + "_id", null);

                String link = sharedPreferences.getString(key + "_link", null);
                if(link!=null) try {
                    source.link = new URL(link);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

                source.title = sharedPreferences.getString(key + "_title", null);
                source.icon = sharedPreferences.getString(key + "_icon", null);

                int updated = sharedPreferences.getInt(key + "_updated", -1);
                if(updated != -1) source.updated = new Date(updated);

                MainActivity.sources.put(key, source);
            }
        }
    }
}
