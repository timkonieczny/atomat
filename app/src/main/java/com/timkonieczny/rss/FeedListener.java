package com.timkonieczny.rss;

import java.util.ArrayList;

public interface FeedListener {
    public void onSourcesUpdated(ArrayList<Entry> entries);
}
