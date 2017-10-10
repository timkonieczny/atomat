package com.timkonieczny.rss;

import android.app.Fragment;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Fade;
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

    int currentOrientation;
    boolean cancelRefresh = false;

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
        if(MainActivity.articles == null) MainActivity.articles = new DbList<>();

        feedAdapter = new FeedAdapter((View.OnClickListener)getActivity());
        setEnterTransition(new Fade());
        setExitTransition(new Fade());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_overview, container, false);
        ActionBar actionBar = ((MainActivity)getActivity()).getSupportActionBar();
        if(actionBar!=null) actionBar.setTitle(R.string.title_fragment_articles);

        RecyclerView recyclerView = (RecyclerView)view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(feedAdapter);

        swipeRefreshLayout = ((SwipeRefreshLayout)view.findViewById(R.id.swipe_refresh_layout));
        swipeRefreshLayout.setOnRefreshListener(this);

        noUpdatesSnackbar = Snackbar.make(view, getResources().getString(R.string.no_updates_snackbar), Snackbar.LENGTH_SHORT);

        currentOrientation = getResources().getConfiguration().orientation;

        cancelRefresh = savedInstanceState != null && savedInstanceState.getInt("orientation") != currentOrientation || !((MainActivity)getActivity()).isActivityResumed;

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!cancelRefresh){
            swipeRefreshLayout.setRefreshing(true);
            updateFeed();
        }
        cancelRefresh = false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("orientation", currentOrientation);
        super.onSaveInstanceState(outState);
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
    public void onFeedUpdated(boolean hasNewArticles, boolean isUpdateComplete, int errorCode) {
        if(isUpdateComplete) {
            switch (errorCode){
                case AtomParser.SUCCESS:
                    if (hasNewArticles) feedAdapter.notifyDataSetChanged();
                    else noUpdatesSnackbar.show();
                    break;
                case AtomParser.ERROR_IO:
                    if(getView()!=null)
                        Snackbar.make(getView(), R.string.error_network, Snackbar.LENGTH_SHORT)
                                .setAction(R.string.retry, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        swipeRefreshLayout.setRefreshing(true);
                                        updateFeed();
                                    }
                                })
                                .show();
                    break;
                case AtomParser.ERROR_XML:
                    if(getView()!=null)
                        Snackbar.make(getView(), R.string.error_invalid_rss, Snackbar.LENGTH_SHORT).show();
                    break;
            }
            swipeRefreshLayout.setRefreshing(false);
        }else if(hasNewArticles) feedAdapter.notifyDataSetChanged();
        if(getActivity()!=null) ((MainActivity)getActivity()).isActivityResumed = false;
    }

    public void updateFeed(){
        // TODO: The Verge: Feed only contains article previews. But ID is also a feed URL containing full articles.
        new Feed(getContext(), this, null).execute();
    }
}
