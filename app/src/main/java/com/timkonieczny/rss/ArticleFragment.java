package com.timkonieczny.rss;


import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ArticleFragment extends Fragment {

    private Bundle arguments;

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


        if(arguments == null){
            Log.d("ArticleFragment", "arguments is null");
        }else{

            // TODO: this is called when the activity is created, hence arguments is null. Put this in (probably) onAttach

            Article article = MainActivity.articles.get(arguments.getInt("index"));

            ((TextView) view.findViewById(R.id.article_title)).setText(article.title);
            ((TextView) view.findViewById(R.id.article_author)).setText(article.author);
            ((TextView) view.findViewById(R.id.source_title)).setText(article.source.title);
            ((TextView) view.findViewById(R.id.article_content)).setText(article.content);

            if(article.headerImageBitmap != null)
                ((ImageView) view.findViewById(R.id.article_header)).setImageBitmap(article.headerImageBitmap);
        }
    }
}
