package com.timkonieczny.rss;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import java.util.ArrayList;

public class MainActivity extends FragmentActivity {

    protected static ArrayList<Article> articles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
