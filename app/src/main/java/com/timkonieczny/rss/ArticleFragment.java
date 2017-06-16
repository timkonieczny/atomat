package com.timkonieczny.rss;


import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ArticleFragment extends Fragment implements UpdateHeaderImageListener, UpdateIconImageListener{

    private Bundle arguments;

    private TextView sourceTitle;
    private ImageView headerImage;

    public ArticleFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        arguments = getArguments();
        return inflater.inflate(R.layout.fragment_article, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /*MainActivity.toggle.setDrawerIndicatorEnabled(false);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
        MainActivity.toggle.syncState();*/

        Article article = MainActivity.articles.get(arguments.getInt("index"));

        headerImage = (ImageView) view.findViewById(R.id.article_header);
        sourceTitle = (TextView) view.findViewById(R.id.source_title);

        ((TextView) view.findViewById(R.id.article_title)).setText(article.title);
        ((TextView) view.findViewById(R.id.article_author)).setText(article.author);
        sourceTitle.setText(article.source.title);
        ((TextView) view.findViewById(R.id.article_content)).setText(article.content);
        // TODO: Make links clickable
        // TODO: Load inline images / media

        if(article.headerImageBitmap!=null) headerImage.setImageBitmap(article.headerImageBitmap);
        else if(article.headerImage!=null) article.setUpdateHeaderImageListener(this);

        if(article.source.iconDrawable != null){
            sourceTitle.setCompoundDrawablesWithIntrinsicBounds(article.source.iconDrawable, null, null, null);
        }else if(article.source.icon != null){
            article.source.setUpdateIconImageListener(this);
        }
    }

    @Override
    public void onIconImageUpdated(Source source) {
        sourceTitle.setCompoundDrawablesWithIntrinsicBounds(source.iconDrawable, null, null, null);
    }

    @Override
    public void onHeaderImageUpdated(Article article) {
        headerImage.setImageBitmap(article.headerImageBitmap);
    }
}
