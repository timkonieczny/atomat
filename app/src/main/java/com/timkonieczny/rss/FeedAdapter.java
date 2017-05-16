package com.timkonieczny.rss;

import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.ArticleCardViewHolder> {

    ArrayList<Article> articles;

    FeedAdapter(){
        articles = new ArrayList<>();
    }

    @Override
    public ArticleCardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.article_card, parent, false);
        return new ArticleCardViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ArticleCardViewHolder holder, int position) {
        Article article = articles.get(position);

        holder.sourceTitle.setText(article.source.title);
        holder.sourceIcon.setText(article.source.icon);

        holder.articleTitle.setText(article.title);
        holder.articleAuthor.setText(article.author);

        holder.articleHeader.setImageBitmap(article.headerImageBitmap);

        if(article.colorPalette!=null) {
            int color = article.colorPalette.getDarkMutedColor(Color.DKGRAY);
            holder.articleHeader.setColorFilter(Color.argb(128, Color.red(color), Color.green(color), Color.blue(color)));
        }
    }

    @Override
    public int getItemCount() {
        return articles.size();
    }

    static class ArticleCardViewHolder extends RecyclerView.ViewHolder{

        CardView cardView;

        TextView sourceTitle,
                sourceIcon,
                articleTitle,
                articleAuthor;
        ImageView articleHeader;

        ArticleCardViewHolder(View itemView) {
            super(itemView);

            cardView = (CardView)itemView.findViewById(R.id.card_view);

            sourceTitle = (TextView) itemView.findViewById(R.id.source_title);
            sourceIcon = (TextView) itemView.findViewById(R.id.source_icon);
//            sourceLink = (TextView) itemView.findViewById(R.id.source_link);
//            sourceId = (TextView) itemView.findViewById(R.id.source_id);
//            sourceUpdated = (TextView) itemView.findViewById(R.id.source_updated);

            articleTitle = (TextView) itemView.findViewById(R.id.article_title);
//            articleLink = (TextView) itemView.findViewById(R.id.article_link);
//            articleId = (TextView) itemView.findViewById(R.id.article_id);
//            articleUpdated = (TextView) itemView.findViewById(R.id.article_updated);
//            articlePublished = (TextView) itemView.findViewById(R.id.article_published);
            articleAuthor = (TextView) itemView.findViewById(R.id.article_author);
//            articleContent = (TextView) itemView.findViewById(R.id.article_content);

            articleHeader = (ImageView) itemView.findViewById(R.id.article_header);
        }
    }
}