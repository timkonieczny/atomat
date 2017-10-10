package com.timkonieczny.rss;

import android.content.ComponentName;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsCallback;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsService;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.customtabs.CustomTabsSession;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.URLSpan;
import android.transition.Transition;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.xml.sax.XMLReader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArticleActivity extends AppCompatActivity implements ArticleChangedListener, SourceChangedListener {

    private TextView sourceTitleTextView, contentTextView;
    ImageView headerImageView;
    Toolbar toolbar;
    private SpannableStringBuilder spannableStringBuilder;
    private ImageSpan[] imageSpans;
    private CustomTabsIntent customTabsIntent;
    private CustomTabsServiceConnection customTabsServiceConnection;
    Article article;
    Transition.TransitionListener transitionListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);
        article = MainActivity.articles.getByDbId(getIntent().getExtras().getLong("dbId"));

        transitionListener = new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {}

            @Override
            public void onTransitionEnd(Transition transition) {
                ActionBar actionBar = getSupportActionBar();
                if(actionBar!=null) actionBar.setDisplayHomeAsUpEnabled(true);
                toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onBackPressed();
                    }
                });
            }

            @Override
            public void onTransitionCancel(Transition transition) {}

            @Override
            public void onTransitionPause(Transition transition) {}

            @Override
            public void onTransitionResume(Transition transition) {}
        };

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null) actionBar.setDisplayShowTitleEnabled(false);
        getWindow().getSharedElementEnterTransition().addListener(transitionListener);

        headerImageView = (ImageView) findViewById(R.id.article_header);
        sourceTitleTextView = (TextView) findViewById(R.id.source_title);

        ((TextView) findViewById(R.id.article_title)).setText(article.title);
        ((TextView) findViewById(R.id.article_author)).setText(article.author);
        sourceTitleTextView.setText(article.source.title);

        headerImageView.setImageDrawable(article.getImage(this, Image.TYPE_HEADER));
        if (article.header.palette != null) {
            int color = article.header.palette.getDarkMutedColor(Color.DKGRAY);
            headerImageView.setColorFilter(Color.argb(128, Color.red(color), Color.green(color), Color.blue(color)));
        }
        headerImageView.setTransitionName(article.dbId + "_header");

        sourceTitleTextView.setCompoundDrawablesWithIntrinsicBounds(article.source.getIconDrawable(this), null, null, null);
        contentTextView = (TextView)findViewById(R.id.article_content);

        spannableStringBuilder = new SpannableStringBuilder(Html.fromHtml(
                article.content, Html.FROM_HTML_MODE_COMPACT, null, getCustomTagHandler()));

        setInlineImages();
        setInlineUrls();
        contentTextView.setText(spannableStringBuilder);
        contentTextView.setMovementMethod(LinkMovementMethod.getInstance());
        // TODO: Handle non-image media
    }

    @Override
    protected void onPause() {
        if(customTabsServiceConnection != null){
            unbindService(customTabsServiceConnection);
            customTabsServiceConnection = null;
        }
        super.onPause();
    }


    private Html.TagHandler getCustomTagHandler(){
        return new Html.TagHandler() {
            int startPosition, endPosition;
            @Override
            public void handleTag(boolean opening, String tagName, Editable editable, XMLReader xmlReader) {
                if(tagName.equalsIgnoreCase("figcaption")){
                    if(opening) startPosition = editable.length();
                    else{
                        endPosition = editable.length();    // 13sp = caption font size; 15sp = body font size
                        editable.setSpan(new RelativeSizeSpan(13.0f/15), startPosition, endPosition, Spannable.SPAN_MARK_MARK);
                        editable.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.captionText, getTheme())), startPosition, endPosition, Spannable.SPAN_MARK_MARK);
                    }
                }
            }
        };
    }

    private void setInlineUrls(){
        findViewById(R.id.view_in_browser).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customTabsIntent.launchUrl(view.getContext(), Uri.parse(article.link));
            }
        });


        ArrayList<URLSpan> links = new ArrayList<>(Arrays.asList(
                spannableStringBuilder.getSpans(0, spannableStringBuilder.length(), URLSpan.class)));
        if(links.size() > 0) {
            URLSpan link;
            // remove links from images
            int j0 = 0;
            for (int i = 0; i < links.size(); i++) {
                link = links.get(i);
                for (int j = j0; j < imageSpans.length; j++) {
                    if (spannableStringBuilder.getSpanStart(imageSpans[j]) >= spannableStringBuilder.getSpanStart(link) &&
                            spannableStringBuilder.getSpanEnd(imageSpans[j]) <= spannableStringBuilder.getSpanEnd(link)) {
                        spannableStringBuilder.removeSpan(link);
                        links.remove(i);
                        i--;
                        j0 = j + 1;
                        break;
                    }
                }
            }
            // make links clickable
            List<Bundle> likelyUrls = new ArrayList<>(links.size() - 1);
            for (int i = 0; i < links.size(); i++) {
                link = links.get(i);
                final Uri linkUri = Uri.parse(link.getURL());
                spannableStringBuilder.setSpan(
                        new ClickableSpan() {
                            public void onClick(View view) {
                                customTabsIntent.launchUrl(view.getContext(), linkUri);
                            }
                        },
                        spannableStringBuilder.getSpanStart(link),
                        spannableStringBuilder.getSpanEnd(link),
                        spannableStringBuilder.getSpanFlags(link)
                );
                spannableStringBuilder.removeSpan(link);
                Bundle bundle = new Bundle();
                bundle.putParcelable(CustomTabsService.KEY_URL, linkUri);
                likelyUrls.add(bundle);
            }
            if (links.size() > 0) {
                customTabsServiceConnection = getCustomTabsServiceConnection(likelyUrls);
                CustomTabsClient.bindCustomTabsService(this, "com.android.chrome",
                        customTabsServiceConnection);
            }
        }
    }

    private CustomTabsServiceConnection getCustomTabsServiceConnection(final List<Bundle> likelyUrls){
        return new CustomTabsServiceConnection() {

            private CustomTabsClient client;
            private CustomTabsSession session;

            @Override
            public void onCustomTabsServiceConnected(ComponentName name, CustomTabsClient client) {
                this.client = client;
                this.client.warmup(0);
                session = this.client.newSession(new CustomTabsCallback());
                session.mayLaunchUrl(Uri.parse(article.link), null, likelyUrls);  // TODO: Maybe loading too many links

                customTabsIntent = (new CustomTabsIntent.Builder(session)).build();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                client = null;
            }
        };
    }


    private void setInlineImages(){
        imageSpans = spannableStringBuilder.getSpans(0, spannableStringBuilder.length(), ImageSpan.class);

        if(article.inlineImages == null){
            article.inlineImages = new ArrayList<>();
            for(int i = 0; i < imageSpans.length; i++){
                article.inlineImages.add(new Image(Image.TYPE_INLINE));
                article.inlineImages.get(i).url = imageSpans[i].getSource();
            }
        }

        for(int i = 0; i < article.inlineImages.size(); i++){
            if(article.getImage(this, i) != null) setInlineImage(i);
        }
    }

    private void setInlineImage(int index){
        if(article.getImage(this, index) != null) {
            int imageWidth = MainActivity.viewWidth - contentTextView.getPaddingLeft() - contentTextView.getPaddingRight();
            Image image = article.inlineImages.get(index);
            int imageHeight = (image.drawable.getMinimumHeight() *
                    (MainActivity.viewWidth - contentTextView.getPaddingLeft() - contentTextView.getPaddingRight())) /
                    image.drawable.getMinimumWidth();
            image.drawable.setBounds(0, 0, imageWidth, imageHeight);
            spannableStringBuilder.setSpan(new ImageSpan(image.drawable),
                    spannableStringBuilder.getSpanStart(imageSpans[index]),
                    spannableStringBuilder.getSpanEnd(imageSpans[index]),
                    spannableStringBuilder.getSpanFlags(imageSpans[index]));
            spannableStringBuilder.removeSpan(imageSpans[index]);
        }
    }

    @Override
    public void onSourceChanged(Source source) {
        sourceTitleTextView.setCompoundDrawablesWithIntrinsicBounds(source.icon.drawable, null, null, null);
    }

    @Override
    public void onArticleChanged(Article article, int flag) {
        if(flag == Image.TYPE_HEADER){
            headerImageView.setImageDrawable(article.header.drawable);
            int color = article.header.palette.getDarkMutedColor(Color.DKGRAY);
            headerImageView.setColorFilter(Color.argb(128, Color.red(color), Color.green(color), Color.blue(color)));
        }
        else {
            setInlineImage(flag);
            contentTextView.setText(spannableStringBuilder);
        }
    }
}
