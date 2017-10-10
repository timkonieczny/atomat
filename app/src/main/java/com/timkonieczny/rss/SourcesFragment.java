package com.timkonieczny.rss;

import android.animation.Animator;
import android.app.Fragment;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.URLUtil;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;

import java.util.regex.Pattern;


public class SourcesFragment extends Fragment implements FeedListener{

    private Pattern httpPattern = Pattern.compile("\\Ahttp(s)?+://");
    private Pattern xmlPattern = Pattern.compile("\\.xml\\Z");

    private View view;
    private FloatingActionButton fab;
    private EditText urlEditText;
    private TextInputLayout urlTextInputLayout;
    private LinearLayout sourceInputLayout, sourceLoadingLayout;
    private View revealingView;

    private SourcesAdapter sourcesAdapter;

    boolean isAttached;

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

        fab = (FloatingActionButton) view.findViewById(R.id.add_source_fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCircularReveal(view);
                fab.hide();
            }
        });

        urlEditText = (EditText) view.findViewById(R.id.feed_url_edit_text);
        urlTextInputLayout = (TextInputLayout) view.findViewById(R.id.text_input_layout_url);
        sourceInputLayout = (LinearLayout) view.findViewById(R.id.add_source_input_layout);
        sourceLoadingLayout = (LinearLayout) view.findViewById(R.id.add_source_loading_layout);
        revealingView = view.findViewById(R.id.fab_content);
        view.findViewById(R.id.close_circular_reveal_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeCircularReveal(view);
            }
        });

        view.findViewById(R.id.add_source_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addSource();
            }
        });
    }

    private void addSource(){
        urlTextInputLayout.setErrorEnabled(false);
        String url = urlEditText.getText().toString().replaceAll(" ", "");
        if(!url.equals("")) {
            if (!httpPattern.matcher(url).find()) url = "http://" + url;
            if (URLUtil.isValidUrl(url)) {
                if(MainActivity.sources.containsRssUrl(url))
                    urlTextInputLayout.setError(getResources().getString(R.string.error_duplicate_source));
                else {
                    sourceInputLayout.setVisibility(View.GONE);
                    sourceLoadingLayout.setVisibility(View.VISIBLE);
                    new Feed(getContext(), this, url).execute();
                }
            } else urlTextInputLayout.setError(getResources().getString(R.string.error_no_xml));
        }else urlTextInputLayout.setError(getResources().getString(R.string.error_invalid_url));
    }

    void openCircularReveal(View view){
        if(revealingView.getVisibility() == View.GONE){     // This avoids fab pressing during hide() animation
            urlEditText.requestFocus();
            Animator animator = ViewAnimationUtils.createCircularReveal(
                    revealingView,
                    view.getRight(),
                    view.getBottom(),
                    0,
                    (float) Math.hypot(view.getWidth(), view.getHeight()));
            revealingView.setVisibility(View.VISIBLE);
            fab.setImageDrawable(getActivity().getDrawable(R.drawable.ic_add_to_close));
            ((AnimationDrawable) fab.getDrawable()).start();
            animator.start();
        }
    }

    void closeCircularReveal(View view){
        urlEditText.clearFocus();
        urlTextInputLayout.setError(null);

        ((InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(urlEditText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

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
                sourceLoadingLayout.setVisibility(View.GONE);
                sourceInputLayout.setVisibility(View.VISIBLE);
                urlEditText.setText("");
                revealingView.setVisibility(View.GONE);
                fab.show();
            }

            @Override
            public void onAnimationCancel(Animator animation) {}

            @Override
            public void onAnimationRepeat(Animator animation) {}
        });
        fab.setImageDrawable(getActivity().getDrawable(R.drawable.ic_close_to_add));
        ((AnimationDrawable)fab.getDrawable()).start();
        animator.start();
    }

    @Override
    public void onFeedUpdated(boolean hasNewArticles, boolean isUpdateComplete, int errorCode) {
        switch (errorCode){
            case AtomParser.SUCCESS:
                if(isUpdateComplete) {
                    closeCircularReveal(view);
                    sourcesAdapter.notifyDataSetChanged();
                }
                break;
            case AtomParser.ERROR_IO:
                if(getView()!=null) {
                    Snackbar.make(getView(), R.string.error_network, Snackbar.LENGTH_LONG)
                            .setAction(R.string.retry, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    addSource();
                                }
                            }).show();
                }
                sourceLoadingLayout.setVisibility(View.GONE);
                sourceInputLayout.setVisibility(View.VISIBLE);
                break;
            case AtomParser.ERROR_XML:
                urlTextInputLayout.setError(getResources().getString(R.string.error_invalid_rss));
                sourceLoadingLayout.setVisibility(View.GONE);
                sourceInputLayout.setVisibility(View.VISIBLE);
                break;
        }
    }

    boolean onBackPressed(){
        if(revealingView.getVisibility() == View.VISIBLE){
            closeCircularReveal(revealingView);
            return false;
        }
        return true;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        isAttached = false;
        ((MainActivity)getActivity()).isAnyFragmentAttached = false;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        isAttached = true;
        ((MainActivity)getActivity()).isAnyFragmentAttached = true;
    }}