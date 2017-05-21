package com.timkonieczny.rss;

import android.content.ComponentName;
import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsCallback;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsService;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.customtabs.CustomTabsSession;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ArticleActivity extends AppCompatActivity {

    private CustomTabsClient customTabsClient;
    private CustomTabsSession customTabsSession;
    private CustomTabsIntent intent;
    private CustomTabsIntent.Builder builder;
    private Uri mostLikelyUrl;
    private List<Bundle> likelyUrls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        Bundle extras = this.getIntent().getExtras();
        ((TextView)findViewById(R.id.article_title)).setText(extras.getString("title"));
        ((TextView)findViewById(R.id.article_author)).setText(extras.getString("author"));
        ((TextView)findViewById(R.id.source_title)).setText(extras.getString("source"));

        TextView contentTextView = (TextView)findViewById(R.id.article_content);
        CharSequence contentWithLinkSpans = Html.fromHtml(extras.getString("content"),Html.FROM_HTML_MODE_COMPACT);
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(contentWithLinkSpans);
        URLSpan[] links = spannableStringBuilder.getSpans(0, contentWithLinkSpans.length(), URLSpan.class);
        likelyUrls = new ArrayList<>(links.length-1);
        mostLikelyUrl = null;
        for(int i = 0; i < links.length; i++) {
            final Uri linkUri = Uri.parse(links[i].getURL());
            spannableStringBuilder.setSpan(
                    new ClickableSpan() {
                        public void onClick(View view) {
                            intent.launchUrl(view.getContext(),linkUri);
                        }
                    },
                    spannableStringBuilder.getSpanStart(links[i]),
                    spannableStringBuilder.getSpanEnd(links[i]),
                    spannableStringBuilder.getSpanFlags(links[i])
            );
            spannableStringBuilder.removeSpan(links[i]);
            if(i<links.length-1){
                Bundle bundle = new Bundle();
                bundle.putParcelable(CustomTabsService.KEY_URL, linkUri);
                likelyUrls.add(bundle);
            }else{
                mostLikelyUrl = linkUri;
            }
        }
        if(links.length>0){
            CustomTabsServiceConnection customTabsServiceConnection = new CustomTabsServiceConnection() {
                @Override
                public void onCustomTabsServiceConnected(ComponentName name, CustomTabsClient client) {
                    customTabsClient = client;
                    customTabsClient.warmup(0);
                    customTabsSession = customTabsClient.newSession(new CustomTabsCallback());
                    if(mostLikelyUrl != null) customTabsSession.mayLaunchUrl(mostLikelyUrl, null, likelyUrls);  // TODO: Maybe loading too many links

                    builder = new CustomTabsIntent.Builder(customTabsSession);
                    intent = builder.build();
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    customTabsClient = null;
                }
            };

            CustomTabsClient.bindCustomTabsService(this, "com.android.chrome", customTabsServiceConnection);
        }
        contentTextView.setText(spannableStringBuilder);
        contentTextView.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
