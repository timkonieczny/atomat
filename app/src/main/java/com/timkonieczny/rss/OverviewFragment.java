package com.timkonieczny.rss;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

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
            MainActivity.articles = new ArrayList<>();
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
        int index = MainActivity.articles.indexOf(article);
        if(index>=0)
            feedAdapter.notifyItemChanged(index);   // Article card exists already and only needs to update
        else
            feedAdapter.notifyDataSetChanged();     // Article card doesn't exist yet and needs to be created
    }

    @Override
    public void onFeedUpdated(boolean hasNewArticles) {
        isInitialRefreshDone = true;
        if(hasNewArticles)
            feedAdapter.notifyDataSetChanged();
        else
            noUpdatesSnackbar.show();

        for (Map.Entry<String, Source> entry : MainActivity.sources.entrySet()) {
            Log.d("Feed", "Key (URL):\t\t"+entry.getKey()+"\n"+entry.getValue().toString());
        }

        swipeRefreshLayout.setRefreshing(false);
    }

    public void updateFeed(){

        // TODO: get from shared preferences / set up in SourcesFragment
        if(MainActivity.sources == null)
            MainActivity.sources = new HashMap<>();

        MainActivity.sourcesUrls = new ArrayList<>();
        MainActivity.sourcesUrls.add("https://www.theverge.com/rss/index.xml");
        (new Feed(this, getResources(), getFragmentManager(), MainActivity.sourcesUrls)).execute();
    }
}
