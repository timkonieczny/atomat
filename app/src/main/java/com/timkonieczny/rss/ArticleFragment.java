package com.timkonieczny.rss;

import android.app.Fragment;
import android.content.ComponentName;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsCallback;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsService;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.customtabs.CustomTabsSession;
import android.support.v7.app.ActionBar;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import org.xml.sax.XMLReader;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class ArticleFragment extends Fragment implements ArticleChangedListener, SourceChangedListener/*, ImageListener*/{

    private Bundle arguments;

    private TextView sourceTitle;
    private ImageView headerImage;
    private Uri mostLikelyUrl;
    private List<Bundle> likelyUrls;

    private CustomTabsClient customTabsClient;
    private CustomTabsSession customTabsSession;
    private CustomTabsIntent intent;
    private CustomTabsIntent.Builder builder;
    private ImageSpan[] images;
    private TextView contentTextView;
    private SpannableStringBuilder spannableStringBuilder;
    private Article article;

    public ArticleFragment() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        arguments = getArguments();
        return inflater.inflate(R.layout.fragment_article, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(MainActivity.viewWidth == 0) view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                MainActivity.viewWidth = view.getWidth();
            }
        });

        /*MainActivity.toggle.setDrawerIndicatorEnabled(false);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
        MainActivity.toggle.syncState();*/

        article = MainActivity.articles.get(arguments.getInt("index"));

        ActionBar actionBar = ((MainActivity)getActivity()).getSupportActionBar();
        if(actionBar!=null) actionBar.setTitle(article.title);

        headerImage = (ImageView) view.findViewById(R.id.article_header);
        sourceTitle = (TextView) view.findViewById(R.id.source_title);

        ((TextView) view.findViewById(R.id.article_title)).setText(article.title);
        ((TextView) view.findViewById(R.id.article_author)).setText(article.author);
        sourceTitle.setText(article.source.title);

        contentTextView = (TextView)view.findViewById(R.id.article_content);

        Html.TagHandler figcaptionHandler = new Html.TagHandler() {
            int startPosition, endPosition;
            @Override
            public void handleTag(boolean opening, String tagName, Editable editable, XMLReader xmlReader) {
                if(tagName.equalsIgnoreCase("figcaption")){
                    if(opening) startPosition = editable.length();
                    else{
                        endPosition = editable.length();    // 13sp = caption font size; 15sp = body font size
                        editable.setSpan(new RelativeSizeSpan(13.0f/15), startPosition, endPosition, Spannable.SPAN_MARK_MARK);
                        editable.setSpan(new ForegroundColorSpan(Color.parseColor("#616161")), startPosition, endPosition, Spannable.SPAN_MARK_MARK);
                    }
                }
            }
        };
        CharSequence content = Html.fromHtml(article.content, Html.FROM_HTML_MODE_COMPACT, null, figcaptionHandler);

        spannableStringBuilder = new SpannableStringBuilder(content);

        URLSpan[] links = spannableStringBuilder.getSpans(0, content.length(), URLSpan.class);
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

            CustomTabsClient.bindCustomTabsService(getContext(), "com.android.chrome", customTabsServiceConnection);
        }
        contentTextView.setText(spannableStringBuilder);
        contentTextView.setMovementMethod(LinkMovementMethod.getInstance());

        images = spannableStringBuilder.getSpans(0, content.length(), ImageSpan.class);


        if(article.inlineImages == null){
            article.inlineImages = new Image[images.length];
            for(int i = 0; i < article.inlineImages.length; i++){
                article.inlineImages[i] = new Image();
                article.inlineImages[i].url = images[i].getSource();
            }
        }

        for(int i = 0; i < article.inlineImages.length; i++){
            if(article.getImage(this, i) != null){
                setInlineImage(i);
            }
        }

        // TODO: Handle non-image media

        headerImage.setImageDrawable(article.getImage(this, Article.HEADER));

        sourceTitle.setCompoundDrawablesWithIntrinsicBounds(article.source.getIconDrawable(this), null, null, null);
    }

    @Override
    public void onSourceChanged(Source source) {
        sourceTitle.setCompoundDrawablesWithIntrinsicBounds(source.icon.drawable, null, null, null);
    }

    @Override
    public void onArticleChanged(Article article, int flag) {
        if(flag == Article.HEADER) headerImage.setImageDrawable(article.header.drawable);
        else setInlineImage(flag);
    }

    private void setInlineImage(int index){
        int imageWidth = MainActivity.viewWidth-contentTextView.getPaddingLeft()-contentTextView.getPaddingRight();
        int imageHeight = (article.inlineImages[index].drawable.getMinimumHeight() *
                (MainActivity.viewWidth-contentTextView.getPaddingLeft()-contentTextView.getPaddingRight())) /
                article.inlineImages[index].drawable.getMinimumWidth();
        article.inlineImages[index].drawable.setBounds(0, 0, imageWidth, imageHeight);
        spannableStringBuilder.setSpan(
                new ImageSpan(article.inlineImages[index].drawable),
                spannableStringBuilder.getSpanStart(images[index]),
                spannableStringBuilder.getSpanEnd(images[index]),
                spannableStringBuilder.getSpanFlags(images[index]));
        spannableStringBuilder.removeSpan(images[index]);
        contentTextView.setText(spannableStringBuilder);
    }
}