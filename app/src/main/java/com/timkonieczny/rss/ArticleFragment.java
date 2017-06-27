package com.timkonieczny.rss;

import android.app.Fragment;
import android.content.ComponentName;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsCallback;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsService;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.customtabs.CustomTabsSession;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.text.style.URLSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class ArticleFragment extends Fragment implements UpdateHeaderImageListener, UpdateIconImageListener, UpdateImageListener{

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
    private int contentTextViewWidth;
    private SpannableStringBuilder spannableStringBuilder;
    private Article article;

    public ArticleFragment() {
        // Required empty public constructor
    }


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

        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                contentTextViewWidth = view.getWidth()-contentTextView.getPaddingLeft()-contentTextView.getPaddingRight();
            }
        });

        /*MainActivity.toggle.setDrawerIndicatorEnabled(false);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
        MainActivity.toggle.syncState();*/

        article = MainActivity.articles.get(arguments.getInt("index"));

        headerImage = (ImageView) view.findViewById(R.id.article_header);
        sourceTitle = (TextView) view.findViewById(R.id.source_title);

        ((TextView) view.findViewById(R.id.article_title)).setText(article.title);
        ((TextView) view.findViewById(R.id.article_author)).setText(article.author);
        sourceTitle.setText(article.source.title);

        contentTextView = (TextView)view.findViewById(R.id.article_content);
        spannableStringBuilder = new SpannableStringBuilder(article.content);

        URLSpan[] links = spannableStringBuilder.getSpans(0, article.content.length(), URLSpan.class);
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

        images = spannableStringBuilder.getSpans(0, article.content.length(), ImageSpan.class);
        article.inlineImages = new Drawable[images.length];
        for(int i = 0; i < images.length; i++) {
            try {
                UpdateImageTask updateImageTask = new UpdateImageTask(this, i, getResources());
                updateImageTask.execute(new URL(images[i].getSource()));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }

        // TODO: Handle non-image media

        if(article.headerImageBitmap!=null) headerImage.setImageBitmap(article.headerImageBitmap);
        else if(article.headerImage!=null) article.setUpdateHeaderImageListener(this);

        if(article.source.iconDrawable != null){
            sourceTitle.setCompoundDrawablesWithIntrinsicBounds(article.source.iconDrawable, null, null, null);
        }else if(article.source.icon != null){
            article.source.setUpdateIconImageListener(this);
        }
    }

    @Override
    public void onIconImageUpdated(Source source) {
        sourceTitle.setCompoundDrawablesWithIntrinsicBounds(source.iconDrawable, null, null, null);
    }

    @Override
    public void onHeaderImageUpdated(Article article) {
        headerImage.setImageBitmap(article.headerImageBitmap);
    }

    @Override
    public void onImageUpdated(Drawable image, int imageSpanIndex) {
        article.inlineImages[imageSpanIndex] = image;   // TODO: save / load images in external storage
        article.inlineImages[imageSpanIndex].setBounds(
                0,
                0,
                contentTextViewWidth,
                (article.inlineImages[imageSpanIndex].getMinimumHeight() * contentTextViewWidth) / article.inlineImages[imageSpanIndex].getMinimumWidth()
        );
        spannableStringBuilder.setSpan(
                new ImageSpan(article.inlineImages[imageSpanIndex]),
                spannableStringBuilder.getSpanStart(images[imageSpanIndex]),
                spannableStringBuilder.getSpanEnd(images[imageSpanIndex]),
                spannableStringBuilder.getSpanFlags(images[imageSpanIndex]));
        spannableStringBuilder.removeSpan(images[imageSpanIndex]);
        contentTextView.setText(spannableStringBuilder);
    }
}
