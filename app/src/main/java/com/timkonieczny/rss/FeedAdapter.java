package com.timkonieczny.rss;

import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.ArticleCardViewHolder>
        implements ArticleChangedListener, SourceChangedListener {

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
        holder.sourceTitle.setCompoundDrawablesWithIntrinsicBounds(article.source.getIconDrawable(this), null, null, null);
        holder.articleTitle.setText(article.title);
        holder.articleAuthor.setText(article.author);
        holder.articleHeader.setImageDrawable(article.getImage(this, Article.HEADER));

        if(article.header.palette!=null) {
            int color = article.header.palette.getDarkVibrantColor(Color.DKGRAY);
            holder.articleHeader.setColorFilter(Color.argb(128, Color.red(color), Color.green(color), Color.blue(color)));
        }
    }

    @Override
    public int getItemCount() {
        return MainActivity.articles.size();
    }

    @Override
    public void onArticleChanged(Article article, int flag) {
        int index = MainActivity.articles.indexOf(article);
        if(index>=0)
            notifyItemChanged(index);   // Article card exists already and only needs to update
        else
            notifyDataSetChanged();     // Article card doesn't exist yet and needs to be created
    }

    @Override
    public void onSourceChanged(Source source) {
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