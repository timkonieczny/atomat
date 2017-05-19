package com.timkonieczny.rss;

import android.content.Context;
import android.util.Log;
import android.view.View;

class ArticleOnClickListener implements View.OnClickListener {

    private Article article;
    private Context context;

    ArticleOnClickListener(Article article){
        this.article = article;
//        this.context = context;
    }

    @Override
    public void onClick(View v) {
        Log.d("ArticleOnClickListener", "click");
//        Intent intent = new Intent(context, ArticleActivity.class);
//EditText editText = (EditText) findViewById(R.id.editText);
//String message = editText.getText().toString();
//intent.putExtra(EXTRA_MESSAGE, message);
//        context.startActivity(intent);
    }
}
