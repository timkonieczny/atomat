package com.timkonieczny.rss;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

class SourcesAdapter extends BaseAdapter implements UpdateIconImageListener {

    private LayoutInflater layoutInflater;
    private GridView gridView;
    private Context context;

    SourcesAdapter(Context context){
        layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.context = context;
    }

    @Override
    public int getCount() {
        return MainActivity.sources.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        gridView = (GridView) parent;
        View view;

        if (convertView == null) {
            view = layoutInflater.inflate(R.layout.source_item, parent, false);
        } else {
            view = convertView;
        }

        ImageView iconImageView = (ImageView) view.findViewById(R.id.source_icon);
        View backgroundView = view.findViewById(R.id.source_background);
        int backgroundColor = context.getResources().getColor(R.color.cardview_dark_background, context.getTheme());

        Source source = MainActivity.sources.get(position);
        iconImageView.setImageDrawable(source.getIconDrawable(this));
        if(source.colorPalette != null) backgroundView.setBackgroundColor(source.colorPalette.getVibrantColor(backgroundColor));
        else backgroundView.setBackgroundColor(backgroundColor);

        ((TextView)view.findViewById(R.id.source_title)).setText(source.title);

        return view;
    }

    @Override
    public void onIconImageUpdated(Source source) {
        gridView.invalidateViews();
    }
}