package com.timkonieczny.rss;


import android.animation.Animator;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.EditText;
import android.widget.GridView;

import java.util.regex.Pattern;


public class SourcesFragment extends Fragment implements FeedListener{

    private boolean isLayoutRevealed = false;
    private Pattern httpPattern = Pattern.compile("\\Ahttp(s)?+://");
    private Pattern xmlPattern = Pattern.compile("\\.xml\\Z");

    private View view;
    private View fab;
    private EditText urlEditText;
    private TextInputLayout urlTextInputLayout;

    private SourcesAdapter sourcesAdapter;

    public SourcesFragment() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sources, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.view = view;

        ActionBar actionBar = ((MainActivity)getActivity()).getSupportActionBar();
        if(actionBar!=null) actionBar.setTitle(R.string.title_fragment_sources);

        GridView gridView = (GridView) view.findViewById(R.id.sources_grid);
        sourcesAdapter = new SourcesAdapter(getContext());
        gridView.setAdapter(sourcesAdapter);

        fab = view.findViewById(R.id.add_source_fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isLayoutRevealed){
                    closeCircularReveal(view);
                }else {
                    openCircularReveal(view);
                }
            }
        });

        urlEditText = (EditText) view.findViewById(R.id.feed_url_edit_text);
        urlTextInputLayout = (TextInputLayout) view.findViewById(R.id.text_input_layout_url);
        final FeedListener feedListener = this;
        view.findViewById(R.id.add_source_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                urlTextInputLayout.setErrorEnabled(false);
                String url = urlEditText.getText().toString().replaceAll(" ", "");
                if(!url.equals("")) {
                    if (!httpPattern.matcher(url).find()) url = "http://" + url;
//                    if (URLUtil.isValidUrl(url) && xmlPattern.matcher(url).find()) {
                    if (URLUtil.isValidUrl(url)) {
                        if(MainActivity.sources.containsKey(url))
                            urlTextInputLayout.setError("This website is already in your sources");
                        else {
                            SourcesAdapter.keys.add(url);

                            new Feed(url, feedListener, getFragmentManager(), getContext(), getResources());
                        }
                    } else urlTextInputLayout.setError("Enter an URL that points to an XML file");
                }else urlTextInputLayout.setError("Enter a valid URL");
            }
        });
    }

    void openCircularReveal(View view){
        View revealingView = view.findViewById(R.id.fab_content);

        view.findViewById(R.id.feed_url_edit_text).requestFocus();
        Animator animator = ViewAnimationUtils.createCircularReveal(
                revealingView,
                view.getRight(),
                view.getBottom(),
                0,
                (float) Math.hypot(view.getWidth(), view.getHeight()));
        revealingView.setVisibility(View.VISIBLE);
        ((FloatingActionButton)fab).setImageDrawable(getActivity().getDrawable(R.drawable.ic_add_to_close));
        ((AnimationDrawable)((FloatingActionButton)fab).getDrawable()).start();
        animator.start();
        isLayoutRevealed = !isLayoutRevealed;
    }

    void closeCircularReveal(View view){
        final View revealingView = view.findViewById(R.id.fab_content);

        view.findViewById(R.id.feed_url_edit_text).clearFocus();
        Animator animator = ViewAnimationUtils.createCircularReveal(
                revealingView,
                view.getRight(),
                view.getBottom(),
                (float) Math.hypot(view.getWidth(), view.getHeight()),
                0);

        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {}

            @Override
            public void onAnimationEnd(Animator animation) {
                urlEditText.setText("");
                revealingView.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {}

            @Override
            public void onAnimationRepeat(Animator animation) {}
        });
        ((FloatingActionButton)fab).setImageDrawable(getActivity().getDrawable(R.drawable.ic_close_to_add));
        ((AnimationDrawable)((FloatingActionButton)fab).getDrawable()).start();
        animator.start();
        isLayoutRevealed = !isLayoutRevealed;
    }

    @Override
    public void onFeedUpdated(boolean hasNewArticles) {
        closeCircularReveal(view);
        sourcesAdapter.notifyDataSetChanged();
    }
}
