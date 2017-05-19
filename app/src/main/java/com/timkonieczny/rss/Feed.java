package com.timkonieczny.rss;

import android.content.Context;
import android.icu.text.SimpleDateFormat;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Xml;
import android.view.View;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Feed extends AsyncTask<URL, Void, ArrayList<Article>> {

    private FeedListener feedListener;
    private UpdateHeaderImageListener updateHeaderImageListener;
    private UpdateIconImageListener updateIconImageListener;

    private ArrayList<Article> articles;
    private HashSet<String> existingIds;

    private SimpleDateFormat dateFormat;
    private Pattern imgWithWhitespace, img;
    private Context context;

    Feed(FeedListener feedListener, UpdateHeaderImageListener updateHeaderImageListener, UpdateIconImageListener updateIconImageListener, Context context, ArrayList<Article> existingArticles){
        this.context = context;
        this.feedListener = feedListener;
        this.updateHeaderImageListener = updateHeaderImageListener;
        this.updateIconImageListener = updateIconImageListener;

        dateFormat = new SimpleDateFormat("yyyy-mm-dd'T'HH:mm:ssX");
        imgWithWhitespace = Pattern.compile("\\A<img(.*?)/>\\s*");  // <img ... /> at beginning of input, including trailing whitespaces
        img = Pattern.compile("<img(?:.*?)src=\"(.*?)\"(?:.*?)/>"); // src attribute of <img ... />

        existingIds = getExistingArticlesIds(existingArticles);
    }

    @Override
    protected ArrayList<Article> doInBackground(URL... params) {

        articles = new ArrayList<>();

        for(URL url : params){
            try {
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.setReadTimeout(10000);
                connection.setConnectTimeout(15000);

                // Starts the query
                connection.connect();
                InputStream stream = connection.getInputStream();

                readStream(stream);

            } catch (IOException | XmlPullParserException e) {
                e.printStackTrace();
            }
        }

        Article article;
        for(int i = 0; i < articles.size(); i++){
            article = articles.get(i);
            article.uniqueId = article.source.id + "_" + article.id;
            if(existingIds.contains(article.uniqueId)){
                articles.remove(i);
                i--;
            }else{
                article.onClickListener = new ArticleOnClickListener(article);
                article.updateHeaderImage();
            }
        }

        return articles;
    }

    @Override
    protected void onPostExecute(ArrayList<Article> articles) {
        super.onPostExecute(articles);
        feedListener.onFeedUpdated(articles);
    }


    private void readStream(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);

            parser.nextTag();
            parser.require(XmlPullParser.START_TAG, null, "feed");

            readSource(parser);
        } finally {
            in.close();
        }
    }

    private void readSource(XmlPullParser parser) throws IOException, XmlPullParserException {
        Source source = new Source(updateIconImageListener, context);
        while(parseNextTag(parser) != XmlPullParser.END_TAG){
            if(parser.getEventType() == XmlPullParser.START_TAG){
                switch (parser.getName()){
                    case "entry":
                        readEntry(parser, source);
                        break;
                    case "title":
                        source.title = readText(parser);
                        break;
                    case "icon":
                        source.icon = readText(parser);
                        source.updateIconImage();
                        break;
                    case "updated":
                        source.updated = readDate(parser);
                        break;
                    case "id":
                        source.id = readText(parser);
                        break;
                    case "link":
                        source.link = new URL(parser.getAttributeValue(null, "href"));
                        parseNextTag(parser);
                        break;
                }
            }else if(parser.getEventType() == XmlPullParser.TEXT){
                parseNextTag(parser);
            }
        }
    }

    private void readEntry(XmlPullParser parser, Source source) throws IOException, XmlPullParserException {
        Article article = new Article(updateHeaderImageListener);
        article.source = source;
        while(parseNextTag(parser) != XmlPullParser.END_TAG){
            if(parser.getEventType() == XmlPullParser.START_TAG){
                switch (parser.getName()){
                    case "published":
                        article.published = readDate(parser);
                        break;
                    case "title":
                        article.title = readText(parser);
                        break;
                    case "updated":
                        article.updated = readDate(parser);
                        break;
                    case "id":
                        article.id = readText(parser);
                        break;
                    case "link":
                        article.link = readLink(parser);
                        break;
                    case "content":
                        article.content = readText(parser);
                        Matcher matcher = img.matcher(article.content);
                        if (matcher.find()) {
                            article.headerImage = matcher.group(1);
                        }
                        article.content = imgWithWhitespace.matcher(article.content).replaceFirst("");
                        break;
                    case "author":
                        if(parseNextTag(parser) == XmlPullParser.START_TAG && parser.getName().equals("name")){
                            if(parseNextTag(parser) == XmlPullParser.TEXT)        // FIXME: support for multiple authors
                                article.author = parser.getText().trim();
                            parseNextTag(parser);
                        }
                        parseNextTag(parser);
                        break;
                }
            }else if(parser.getEventType() == XmlPullParser.TEXT){
                parseNextTag(parser);
            }
        }
        articles.add(article);
    }


    private int parseNextTag(XmlPullParser parser) throws IOException, XmlPullParserException {
        if(parser.next() == XmlPullParser.TEXT && parser.isWhitespace())
            return parseNextTag(parser);
        else
            return parser.getEventType();
    }

    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String text = null;
        if(parseNextTag(parser) == XmlPullParser.TEXT) text = parser.getText().trim();
        parseNextTag(parser);
        return text;
    }

    private Date readDate(XmlPullParser parser) throws IOException, XmlPullParserException {
        Date date = null;
        if(parseNextTag(parser) == XmlPullParser.TEXT)
            try {
                date = dateFormat.parse(parser.getText().trim());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        parseNextTag(parser);
        return date;
    }

    private URL readLink(XmlPullParser parser) throws IOException, XmlPullParserException {
        URL link = new URL(parser.getAttributeValue(null, "href"));
        parseNextTag(parser);
        return link;
    }

    private HashSet<String> getExistingArticlesIds(ArrayList<Article> existingArticles){
        HashSet<String> ids = new HashSet<>();
        for(int i = 0; i < existingArticles.size(); i++){
            ids.add(existingArticles.get(i).uniqueId);
        }
        return ids;
    }

    private void printTag(String debugTag, XmlPullParser parser){
        try {
            switch (parser.getEventType()){
                case XmlPullParser.START_TAG: Log.d(debugTag, "start ("+parser.getName()+")"); break;
                case XmlPullParser.TEXT: if(parser.getText() == null) Log.d(debugTag, "text (null)"); else Log.d(debugTag, "text ("+parser.getText().trim()+")") ; break;
                case XmlPullParser.END_TAG: Log.d(debugTag, "end ("+parser.getName()+")"); break;
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
    }
}