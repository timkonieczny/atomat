package com.timkonieczny.rss;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

class SourcesAdapter extends BaseAdapter implements SourceChangedListener {

    private LayoutInflater layoutInflater;
    private GridView gridView;
    private Context context;
    private SourceChangedListener sourceChangedListener;

    SourcesAdapter(Context context){
        layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.context = context;
        sourceChangedListener = this;
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
        final View view;

        if (convertView == null) view = layoutInflater.inflate(R.layout.source_item, parent, false);
        else view = convertView;

        ImageView iconImageView = (ImageView) view.findViewById(R.id.source_icon);
        View backgroundView = view.findViewById(R.id.source_background);
        int backgroundColor = context.getResources().getColor(R.color.cardview_dark_background, context.getTheme());

        final Source source = MainActivity.sources.get(position);
        iconImageView.setImageDrawable(source.getIconDrawable(sourceChangedListener));
        if(source.icon.palette != null) backgroundView.setBackgroundColor(source.icon.palette.getVibrantColor(backgroundColor));
        else backgroundView.setBackgroundColor(backgroundColor);

        ((TextView)view.findViewById(R.id.source_title)).setText(source.title);

        view.findViewById(R.id.source_item_options_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(context, v, Gravity.NO_GRAVITY, R.attr.actionOverflowMenuStyle, 0);
                popupMenu.setOnMenuItemClickListener(source);
                source.sourceChangedListener = sourceChangedListener;
                popupMenu.inflate(R.menu.source_options_popup);
                popupMenu.show();
            }
        });

        return view;
    }

    @Override
    public void onSourceChanged(Source source) {
        gridView.invalidateViews();
    }
}