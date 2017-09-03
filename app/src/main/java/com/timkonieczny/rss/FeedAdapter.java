package com.timkonieczny.rss;

import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.CardViewHolder>
        implements ArticleChangedListener, SourceChangedListener {

    private View.OnClickListener onClickListener;
    private static final int VIEW_TYPE_ARTICLE = 0;
    private static final int VIEW_TYPE_MISSING_ARTICLES = 1;

    FeedAdapter(View.OnClickListener onClickListener){
        this.onClickListener = onClickListener;
    }

    @Override
    public CardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType){
            case VIEW_TYPE_ARTICLE:
                return new ArticleCardViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.article_card, parent, false));
            case VIEW_TYPE_MISSING_ARTICLES:
                return new CardViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.articles_missing_card, parent, false));
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(CardViewHolder holder, int position) {
        if(getItemViewType(position) == VIEW_TYPE_ARTICLE) {
            Article article = MainActivity.articles.get(position);

            article.onClickListener = onClickListener;
            ArticleCardViewHolder articleCardViewHolder = (ArticleCardViewHolder)holder;

            articleCardViewHolder.cardView.setTag(article.dbId);
            articleCardViewHolder.cardView.setOnClickListener(article.onClickListener);

            articleCardViewHolder.sourceTitle.setText(article.source.title);
            articleCardViewHolder.sourceTitle.setCompoundDrawablesWithIntrinsicBounds(article.source.getIconDrawable(this), null, null, null);
            articleCardViewHolder.articleTitle.setText(article.title);
            articleCardViewHolder.articleAuthor.setText(article.author);
            articleCardViewHolder.articleHeader.setTransitionName(article.dbId + "_header");
            articleCardViewHolder.articleHeader.setImageDrawable(article.getImage(this, Image.TYPE_HEADER));

            if (article.header.palette != null) {
                int color = article.header.palette.getDarkVibrantColor(Color.DKGRAY);
                articleCardViewHolder.articleHeader.setColorFilter(Color.argb(128, Color.red(color), Color.green(color), Color.blue(color)));
            }
        }else if(getItemViewType(position) == VIEW_TYPE_MISSING_ARTICLES){
            holder.sourceTitle.setText(MainActivity.articles.get(position).source.title);
        }
    }

    @Override
    public int getItemCount() {
        return MainActivity.articles.size();
    }

    @Override
    public int getItemViewType(int position) {
//        return VIEW_TYPE_MISSING_ARTICLES;          // TODO: return correct type
        return VIEW_TYPE_ARTICLE;
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

    static class CardViewHolder extends RecyclerView.ViewHolder{

        TextView sourceTitle;

        CardViewHolder(View itemView) {
            super(itemView);
            sourceTitle = (TextView) itemView.findViewById(R.id.source_title);
        }
    }

    private class ArticleCardViewHolder extends CardViewHolder{

        CardView cardView;

        TextView articleTitle,
                articleAuthor;
        ImageView articleHeader;

        ArticleCardViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView)itemView.findViewById(R.id.card_view);
            articleTitle = (TextView) itemView.findViewById(R.id.article_title);
            articleAuthor = (TextView) itemView.findViewById(R.id.article_author);
            articleHeader = (ImageView) itemView.findViewById(R.id.article_header);
        }
    }
}