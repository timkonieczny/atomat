package com.timkonieczny.rss;

import android.app.ListActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends ListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            new Feed((TextView) findViewById(R.id.debug_text)).execute(new URL("https://www.theverge.com/rss/index.xml"));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        // TODO: How to do listview?

        String[] fromColumns = {"Title"};

        SimpleCursorAdapter mAdapter = new SimpleCursorAdapter(this,R.layout.list_item, null, fromColumns, new int[]{R.id.list_item_text}, 0);
        setListAdapter(mAdapter);

    }
}
