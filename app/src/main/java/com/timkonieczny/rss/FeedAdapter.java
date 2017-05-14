package com.timkonieczny.rss;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class FeedAdapter extends ArrayAdapter<Entry> {

    public FeedAdapter(Context context, int view, ArrayList<Entry> objects) {
        super(context, view, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Entry entry = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.article_list_item, parent, false);
        }
        // Lookup view for data population
        TextView sourceTitle = (TextView) convertView.findViewById(R.id.source_title);
        TextView sourceIcon = (TextView) convertView.findViewById(R.id.source_icon);
        TextView sourceLink = (TextView) convertView.findViewById(R.id.source_link);
        TextView sourceId = (TextView) convertView.findViewById(R.id.source_id);
        TextView sourceUpdated = (TextView) convertView.findViewById(R.id.source_updated);

        TextView entryTitle = (TextView) convertView.findViewById(R.id.entry_title);
        TextView entryLink = (TextView) convertView.findViewById(R.id.entry_link);
        TextView entryId = (TextView) convertView.findViewById(R.id.entry_id);
        TextView entryUpdated = (TextView) convertView.findViewById(R.id.entry_updated);
        TextView entryPublished = (TextView) convertView.findViewById(R.id.entry_published);
        TextView entryAuthor = (TextView) convertView.findViewById(R.id.entry_author);
        TextView entryContent = (TextView) convertView.findViewById(R.id.entry_content);

        // Populate the data into the template view using the data object
        sourceTitle.setText(entry.source.title);
        sourceIcon.setText(entry.source.icon);
        sourceLink.setText(entry.source.link);
        sourceId.setText(entry.source.id);
        sourceUpdated.setText(entry.source.updated.toString());

        entryTitle.setText(entry.title);
        entryLink.setText(entry.link);
        entryId.setText(entry.id);
        entryUpdated.setText(entry.updated.toString());
        entryPublished.setText(entry.published.toString());
        entryAuthor.setText(entry.author);
        entryContent.setText(entry.content);
        // Return the completed view to render on screen
        return convertView;
    }

}
