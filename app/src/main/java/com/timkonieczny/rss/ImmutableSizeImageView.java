package com.timkonieczny.rss;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

// Prevents the parent of an ImageView to change it's size when the image is set
public class ImmutableSizeImageView extends android.support.v7.widget.AppCompatImageView {
    public ImmutableSizeImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        if(getDrawable()!=null)
            setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
        else
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

}
