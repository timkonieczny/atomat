package com.timkonieczny.rss;

import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.ArticleCardViewHolder> implements UpdateHeaderImageListener, UpdateIconImageListener{

    FeedAdapter(){}

    @Override
    public ArticleCardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.article_card, parent, false);
        return new ArticleCardViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ArticleCardViewHolder holder, int position) {
        Article article = MainActivity.articles.get(position);

        holder.cardView.setOnClickListener(article.onClickListener);

        holder.sourceTitle.setText(article.source.title);
        if(article.source.iconDrawable != null) {
            holder.sourceTitle.setCompoundDrawablesWithIntrinsicBounds(article.source.iconDrawable, null, null, null);
        }else if(article.source.iconFileName!=null){
            article.source.loadIconFromInternalStorage();
            holder.sourceTitle.setCompoundDrawablesWithIntrinsicBounds(article.source.iconDrawable, null, null, null);
        }else if(article.source.icon != null){
            article.source.setUpdateIconImageListener(this);
        }else{  // remove old icon (this is a recycled view.)
            holder.sourceTitle.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        }
        holder.articleTitle.setText(article.title);
        holder.articleAuthor.setText(article.author);

        if(article.headerImageBitmap!=null) holder.articleHeader.setImageBitmap(article.headerImageBitmap);
        else if(article.headerImage!=null) article.setUpdateHeaderImageListener(this);

        if(article.colorPalette!=null) {
            int color = article.colorPalette.getDarkVibrantColor(Color.DKGRAY);
            holder.articleHeader.setColorFilter(Color.argb(128, Color.red(color), Color.green(color), Color.blue(color)));
        }
    }

    @Override
    public int getItemCount() {
        return MainActivity.articles.size();
    }

    @Override
    public void onHeaderImageUpdated(Article article) {
        int index = MainActivity.articles.indexOf(article);
        if(index>=0)
            notifyItemChanged(index);   // Article card exists already and only needs to update
        else
            notifyDataSetChanged();     // Article card doesn't exist yet and needs to be created
    }

    @Override
    public void onIconImageUpdated(Source source) {
        notifyDataSetChanged();
    }

    static class ArticleCardViewHolder extends RecyclerView.ViewHolder{

        CardView cardView;

        TextView sourceTitle,
                articleTitle,
                articleAuthor;
        ImageView articleHeader;

        ArticleCardViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView)itemView.findViewById(R.id.card_view);
            sourceTitle = (TextView) itemView.findViewById(R.id.source_title);
            articleTitle = (TextView) itemView.findViewById(R.id.article_title);
            articleAuthor = (TextView) itemView.findViewById(R.id.article_author);
            articleHeader = (ImageView) itemView.findViewById(R.id.article_header);
        }
    }
}