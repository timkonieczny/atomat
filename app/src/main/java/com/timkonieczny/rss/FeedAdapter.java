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

class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.ArticleCardViewHolder> implements LoadImageListener{

    private ArticleCardViewHolder articleCardViewHolder;

    ArrayList<Article> articles;

    FeedAdapter(ArrayList<Article> articles){
        this.articles = articles;
    }

    @Override
    public ArticleCardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.article_card, parent, false);
        articleCardViewHolder = new ArticleCardViewHolder(itemView);
        return articleCardViewHolder;
    }

    @Override
    public void onBindViewHolder(ArticleCardViewHolder holder, int position) {
        Article article = articles.get(position);

        holder.sourceTitle.setText(article.source.title);
        holder.sourceIcon.setText(article.source.icon);
        holder.sourceLink.setText(article.source.link.toString());
        holder.sourceId.setText(article.source.id);
        holder.sourceUpdated.setText(article.source.updated.toString());

        holder.articleTitle.setText(article.title);
        holder.articleLink.setText(article.link.toString());
        holder.articleId.setText(article.id);
        holder.articleUpdated.setText(article.updated.toString());
        holder.articlePublished.setText(article.published.toString());
        holder.articleAuthor.setText(article.author);
        holder.articleContent.setText(article.content);

        (new LoadImageTask(this)).execute(article.headerImage);
    }

    @Override
    public int getItemCount() {
        return articles.size();
    }

    @Override
    public void onImageLoaded(Bitmap bitmap) {
        articleCardViewHolder.articleHeader.setImageBitmap(bitmap); // FIXME: images are not always set correctly
    }

    static class ArticleCardViewHolder extends RecyclerView.ViewHolder{

        CardView cardView;

        TextView sourceTitle,
                sourceIcon,
                sourceLink,
                sourceId,
                sourceUpdated,
                articleTitle,
                articleLink,
                articleId,
                articleUpdated,
                articlePublished,
                articleAuthor,
                articleContent;
        ImageView articleHeader;

        ArticleCardViewHolder(View itemView) {
            super(itemView);

            cardView = (CardView)itemView.findViewById(R.id.card_view);

            sourceTitle = (TextView) itemView.findViewById(R.id.source_title);
            sourceIcon = (TextView) itemView.findViewById(R.id.source_icon);
            sourceLink = (TextView) itemView.findViewById(R.id.source_link);
            sourceId = (TextView) itemView.findViewById(R.id.source_id);
            sourceUpdated = (TextView) itemView.findViewById(R.id.source_updated);

            articleTitle = (TextView) itemView.findViewById(R.id.article_title);
            articleLink = (TextView) itemView.findViewById(R.id.article_link);
            articleId = (TextView) itemView.findViewById(R.id.article_id);
            articleUpdated = (TextView) itemView.findViewById(R.id.article_updated);
            articlePublished = (TextView) itemView.findViewById(R.id.article_published);
            articleAuthor = (TextView) itemView.findViewById(R.id.article_author);
            articleContent = (TextView) itemView.findViewById(R.id.article_content);

            articleHeader = (ImageView) itemView.findViewById(R.id.article_header);
        }
    }
}