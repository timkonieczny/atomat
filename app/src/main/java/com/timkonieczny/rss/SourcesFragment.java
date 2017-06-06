package com.timkonieczny.rss;


import android.animation.Animator;
import android.app.Fragment;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.EditText;
import android.widget.GridView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * A simple {@link Fragment} subclass.
 */
public class SourcesFragment extends Fragment {

    private boolean isLayoutRevealed = false;
    private Pattern httpPattern = Pattern.compile("\\Ahttp(s)?+://");
    private Pattern xmlPattern = Pattern.compile("\\.xml\\Z");

    public SourcesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sources, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        GridView gridView = (GridView) view.findViewById(R.id.sources_grid);
        gridView.setAdapter(new SourcesAdapter(getContext()));

        final View fab = view.findViewById(R.id.add_source_fab);
        final View revealingView = view.findViewById(R.id.fab_content);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /*int[] fabLocation = new int[2];
                fab.getLocationInWindow(fabLocation);
                Log.d("SourcesFragment", "InWindow: " + fabLocation[0] + " " + fabLocation[1]);
                fab.getLocationOnScreen(fabLocation);
                Log.d("SourcesFragment", "OnScreen: " + fabLocation[0] + " " + fabLocation[1]);*/

                if(isLayoutRevealed){
                    view.findViewById(R.id.feed_url_edit_text).clearFocus();
                    Animator animator = ViewAnimationUtils.createCircularReveal(
                            revealingView,
                            view.getRight(),
                            view.getBottom(),
                        /*fabLocation[0]+fab.getWidth()/2,
                        fabLocation[1]-fab.getHeight()/2,
                        Math.round(fab.getX()),
                        Math.round(fab.getY()),*/
                            (float) Math.hypot(view.getWidth(), view.getHeight()),
                            0);

                    animator.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {}

                        @Override
                        public void onAnimationEnd(Animator animation) {
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
                }else {
                    view.findViewById(R.id.feed_url_edit_text).requestFocus();
                    Animator animator = ViewAnimationUtils.createCircularReveal(
                            revealingView,
                            view.getRight(),
                            view.getBottom(),
                        /*fabLocation[0]+fab.getWidth()/2,
                        fabLocation[1]-fab.getHeight()/2,
                        Math.round(fab.getX()),
                        Math.round(fab.getY()),*/
                            0,
                            (float) Math.hypot(view.getWidth(), view.getHeight()));
                    revealingView.setVisibility(View.VISIBLE);
                    ((FloatingActionButton)fab).setImageDrawable(getActivity().getDrawable(R.drawable.ic_add_to_close));
                    ((AnimationDrawable)((FloatingActionButton)fab).getDrawable()).start();
                    animator.start();
                }
                isLayoutRevealed = !isLayoutRevealed;

            }
        });

        final EditText urlEditText = (EditText) view.findViewById(R.id.feed_url_edit_text);
        final TextInputLayout urlTextInputLayout = (TextInputLayout) view.findViewById(R.id.text_input_layout_url);
        view.findViewById(R.id.add_source_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                urlTextInputLayout.setErrorEnabled(false);
                Log.d("SourcesFragment", urlEditText.getText().toString());
                String url = urlEditText.getText().toString().replaceAll(" ", "");
                if(!url.equals("")) {
                    if (!httpPattern.matcher(url).find()) url = "http://" + url;
                    if (URLUtil.isValidUrl(url) && xmlPattern.matcher(url).find()) {
                        Log.d("SourcesFragment", "URL is valid");
                    } else urlTextInputLayout.setError("Enter an URL that points to an XML file");
                }else urlTextInputLayout.setError("Enter a valid URL");
            }
        });
    }
}
