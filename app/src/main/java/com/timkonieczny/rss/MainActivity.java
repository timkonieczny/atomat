package com.timkonieczny.rss;

import android.app.ListActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends ListActivity implements FeedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Feed feed = new Feed();
        feed.feedListener = this;

        try {
            feed.execute(new URL("https://www.theverge.com/rss/index.xml"));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSourcesUpdated(Source source) {
        Log.d("updatedSource", source.toString());

        FeedAdapter adapter = new FeedAdapter(this, R.layout.article_list_item, new Source[]{source});
        this.setListAdapter(adapter);
    }
}
