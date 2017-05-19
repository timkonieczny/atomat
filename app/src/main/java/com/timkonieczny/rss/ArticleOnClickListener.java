package com.timkonieczny.rss;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import static android.provider.AlarmClock.EXTRA_MESSAGE;

class ArticleOnClickListener implements View.OnClickListener {

    private Article article;
    private Context context;

    ArticleOnClickListener(Article article, Context context){
        this.article = article;
        this.context = context;
    }

    @Override
    public void onClick(View v) {
        Log.d("ArticleOnClickListener", "click");
        Intent intent = new Intent(context, ArticleActivity.class);
//        EditText editText = (EditText) findViewById(R.id.editText);
//        String message = editText.getText().toString();
        intent.putExtra("content", article.content);
        intent.putExtra("author", article.author);
        intent.putExtra("title", article.title);
        intent.putExtra("published", article.published);
        context.startActivity(intent);
    }
}
