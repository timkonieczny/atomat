package com.timkonieczny.rss;

import android.app.Fragment;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Comparator;

public class OverviewFragment
        extends Fragment
        implements FeedListener, SourceChangedListener,
        SwipeRefreshLayout.OnRefreshListener{

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

        if(MainActivity.sources == null) MainActivity.sources = new SourcesList();
        if(MainActivity.articles == null) MainActivity.articles = new ArticlesList();

        feedAdapter = new FeedAdapter();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_overview, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        ActionBar actionBar = ((MainActivity)getActivity()).getSupportActionBar();
        if(actionBar!=null) actionBar.setTitle(R.string.title_fragment_articles);

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
    public void onRefresh() {
        updateFeed();
    }

    @Override
    public void onSourceChanged(Source source) {    // updating the whole data set is fine since
        feedAdapter.notifyDataSetChanged();         // icon only needs to be loaded once
    }

    @Override
    public void onFeedUpdated(boolean hasNewArticles, boolean isUpdateComplete) {
        if(isUpdateComplete) {
            isInitialRefreshDone = true;
            if (hasNewArticles) feedAdapter.notifyDataSetChanged();
            else noUpdatesSnackbar.show();
            swipeRefreshLayout.setRefreshing(false);
        }else if(hasNewArticles) feedAdapter.notifyDataSetChanged();
    }

    public void updateFeed(){
        // TODO: The Verge: Feed only contains article previews. But ID is also a feed URL containing full articles.
        new Feed(getContext(), getResources(), this, getFragmentManager());
    }
}
