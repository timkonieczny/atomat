package com.timkonieczny.rss;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

class SourcesAdapter extends BaseAdapter {

    private ArrayList<String> keys;
    private LayoutInflater layoutInflater;

    SourcesAdapter(Context context){
        keys = new ArrayList<>();
        for (Map.Entry<String, Source> entry : MainActivity.sources.entrySet()) {
            keys.add(entry.getKey());
        }
        layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
        View view;
        if (convertView == null) {
            view = layoutInflater.inflate(R.layout.source_item, parent, false);
        } else {
            view = convertView;
        }

        ((ImageView)view.findViewById(R.id.source_icon)).setImageDrawable(MainActivity.sources.get(keys.get(position)).iconDrawable);
        ((TextView)view.findViewById(R.id.source_title)).setText(MainActivity.sources.get(keys.get(position)).title);
//        ((TextView)view.findViewById(R.id.source_title)).setCompoundDrawablesWithIntrinsicBounds(null, MainActivity.sources.get(keys.get(position)).iconDrawable, null, null);

        // TODO: create color palette for icon and set background color

        return view;
    }
}
