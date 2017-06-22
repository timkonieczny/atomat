package com.timkonieczny.rss;

import android.graphics.drawable.Drawable;

interface UpdateImageListener {
    void onImageUpdated(Drawable image, int imageSpanIndex);
}
