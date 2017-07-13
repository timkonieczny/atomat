package com.timkonieczny.rss;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

class SourcesAdapter extends BaseAdapter implements UpdateIconImageListener {

    static ArrayList<String> keys;
    private LayoutInflater layoutInflater;
    private GridView gridView;
    private Context context;

    SourcesAdapter(Context context){
        keys = new ArrayList<>();
        for (Map.Entry<String, Source> entry : MainActivity.sources.entrySet()) {
            keys.add(entry.getKey());
        }
        layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.context = context;
    }

    @Override
    public int getCount() {
        Log.d("SourcesAdapter", "getCount() "+MainActivity.sources.size());
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
        Log.d("SourcesAdapter", "getView()");
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

        Source source = MainActivity.sources.get(keys.get(position));
        Log.d("SourcesAdapter", MainActivity.sources.get(keys.get(position)).toString());
        if(source.iconDrawable != null) iconImageView.setImageDrawable(source.iconDrawable);
        else if(source.iconFileName!=null){
            source.loadIconFromInternalStorage();
            iconImageView.setImageDrawable(source.iconDrawable);
        }else if(source.icon != null) source.setUpdateIconImageListener(this);
        else iconImageView.setImageDrawable(null);

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