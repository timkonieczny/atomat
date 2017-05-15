package com.timkonieczny.rss;

import android.graphics.Bitmap;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class FeedAdapter2 extends RecyclerView.Adapter<FeedAdapter2.EntryCardViewHolder> implements LoadImageListener{

    public ArrayList<Entry> entries;
    EntryCardViewHolder entryCardViewHolder;

    public FeedAdapter2(ArrayList<Entry> entries){
        this.entries = entries;
    }

    @Override
    public EntryCardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.article_list_item, parent, false);
        entryCardViewHolder = new EntryCardViewHolder(v);
        return entryCardViewHolder;
    }

    @Override
    public void onBindViewHolder(EntryCardViewHolder holder, int position) {
        Entry entry = entries.get(position);
        // Populate the data into the template view using the data object
        holder.sourceTitle.setText(entry.source.title);
        holder.sourceIcon.setText(entry.source.icon);
        holder.sourceLink.setText(entry.source.link.toString());
        holder.sourceId.setText(entry.source.id);
        holder.sourceUpdated.setText(entry.source.updated.toString());

        holder.entryTitle.setText(entry.title);
        holder.entryLink.setText(entry.link.toString());
        holder.entryId.setText(entry.id);
        holder.entryUpdated.setText(entry.updated.toString());
        holder.entryPublished.setText(entry.published.toString());
        holder.entryAuthor.setText(entry.author);
        holder.entryContent.setText(entry.content);

        (new LoadImageTask(this)).execute(entry.headerImage);
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

/*    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }*/

    @Override
    public void onImageLoaded(Bitmap bitmap) {
        entryCardViewHolder.entryHeader.setImageBitmap(bitmap);
        entryCardViewHolder.entryHeader.setAdjustViewBounds(true);
    }

    public static class EntryCardViewHolder extends RecyclerView.ViewHolder{

        CardView cardView;

        TextView sourceTitle,
                sourceIcon,
                sourceLink,
                sourceId,
                sourceUpdated,
                entryTitle,
                entryLink,
                entryId,
                entryUpdated,
                entryPublished,
                entryAuthor,
                entryContent;
        public ImageView entryHeader;

        public EntryCardViewHolder(View itemView) {
            super(itemView);

            cardView = (CardView)itemView.findViewById(R.id.card_view);

            sourceTitle = (TextView) itemView.findViewById(R.id.source_title);
            sourceIcon = (TextView) itemView.findViewById(R.id.source_icon);
            sourceLink = (TextView) itemView.findViewById(R.id.source_link);
            sourceId = (TextView) itemView.findViewById(R.id.source_id);
            sourceUpdated = (TextView) itemView.findViewById(R.id.source_updated);

            entryTitle = (TextView) itemView.findViewById(R.id.entry_title);
            entryLink = (TextView) itemView.findViewById(R.id.entry_link);
            entryId = (TextView) itemView.findViewById(R.id.entry_id);
            entryUpdated = (TextView) itemView.findViewById(R.id.entry_updated);
            entryPublished = (TextView) itemView.findViewById(R.id.entry_published);
            entryAuthor = (TextView) itemView.findViewById(R.id.entry_author);
            entryContent = (TextView) itemView.findViewById(R.id.entry_content);

            entryHeader = (ImageView) itemView.findViewById(R.id.entry_header);
        }
    }
}