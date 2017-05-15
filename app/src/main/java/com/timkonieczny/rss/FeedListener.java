package com.timkonieczny.rss;

import java.util.ArrayList;

interface FeedListener {
    void onFeedUpdated(ArrayList<Article> articles);
}
