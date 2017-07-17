package com.timkonieczny.rss;

interface FeedListener {
    void onFeedUpdated(boolean hasNewArticles, boolean isUpdateComplete);
}