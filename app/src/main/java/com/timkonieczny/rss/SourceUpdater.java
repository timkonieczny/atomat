package com.timkonieczny.rss;

import android.app.FragmentManager;
import android.content.Context;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class SourceUpdater {

    private SimpleDateFormat[] dateFormats;
    private Pattern imgWithWhitespacePattern, imgPattern, stylePattern;
    protected Source source;
    private boolean updateSource;
    private Context context;
    private FragmentManager fragmentManager;
    private XmlPullParser parser;

    private HashSet<String> feedTags;
    private HashSet<String> feedTitleTags;
    private HashSet<String> feedIconTags;
    private HashSet<String> feedLinkTags;

    private HashSet<String> entryTags;
    private HashSet<String> entryPublishedTags;
    private HashSet<String> entryTitleTags;
    private HashSet<String> entryContentTags;
    private HashSet<String> entryLinkTags;
    private HashSet<String> entryAuthorTags;

    SourceUpdater(Context context, FragmentManager fragmentManager) {
        this.context = context;
        this.fragmentManager = fragmentManager;

        initializeTagDictionaries();
        imgWithWhitespacePattern = Pattern.compile("\\A\\s*<img(.*?)/>\\s*");  // <imgPattern ... /> at beginning of input, including trailing whitespaces
        imgPattern = Pattern.compile("<img(?:.*?)src=\"(.*?)\"(?:.*?)/>"); // src attribute of <imgPattern ... />
        stylePattern = Pattern.compile("<style>(?:.*?)</style>"); // src attribute of <imgPattern ... />
        dateFormats = new SimpleDateFormat[]{
                new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ssX", Locale.US),
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.US)
        };

        parser = Xml.newPullParser();
        try {
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
    }

    List<Article> parse(InputStream inputStream, Source source, boolean updateSource)
            throws XmlPullParserException, IOException {

        this.updateSource = updateSource;
        this.source = source;
        parser.setInput(inputStream, null);

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            if(feedTags.contains(parser.getName())){
                return readFeed(parser);
            }
        }

        inputStream.close();
        return null;
    }

    private List<Article> readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
        ArrayList<Article> articles = new ArrayList<>();

        if(feedTags.contains(parser.getName())) {
            parser.require(XmlPullParser.START_TAG, null, parser.getName());
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }

                String name = parser.getName();

                // Starts by looking for the entry tag
                if (entryTags.contains(name)) {
                    articles.add(readEntry(parser));
                } else if (updateSource) {
                    if (feedTitleTags.contains(name)) {
                        source.title = readTag(parser, name);
                    } else if (feedIconTags.contains(name)) {
                        source.icon.url = readTag(parser, name);
                    } else if (feedLinkTags.contains(name)) {
                        String linkUrl = parser.getAttributeValue(null, "href");
                        if(linkUrl!=null) {
                            source.link = linkUrl;
                            parser.next();
                        }else
                            source.link = readTag(parser, name);
                    }else{
                        skip(parser);
                    }
                } else {
                    skip(parser);
                }
            }
        }
        for(int i = 0; i < articles.size(); i++){
            Article article = articles.get(i);
            article.source = source;
        }
        return articles;
    }

    // Parses the contents of an entry. If it encounters a title, summary, or link tag, hands them
    // off  to their respective methods for processing. Otherwise, skips the tag.
    private Article readEntry(XmlPullParser parser) throws XmlPullParserException, IOException {

        Article article = new Article(context, fragmentManager);

        parser.require(XmlPullParser.START_TAG, null, parser.getName());
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();

            if(entryTitleTags.contains(name)){
                article.title = readTag(parser, name);
            }else if(entryContentTags.contains(name)){
                article.content = readTag(parser, name);
                Matcher matcher = imgPattern.matcher(article.content);
                if (matcher.find()) article.header.url = matcher.group(1);
                String content = imgWithWhitespacePattern.matcher(article.content).replaceFirst("");
                content = stylePattern.matcher(content).replaceAll("");
                article.content = content;
            }else if(entryLinkTags.contains(name)){
                article.link = parser.getAttributeValue(null, "href");
                if(article.link!=null) parser.next();
                else article.link = readTag(parser, name);
            }else if(entryAuthorTags.contains(name)){
                article.author = readAuthors(parser);
            }else if(entryPublishedTags.contains(name)){
                ParsePosition parsePosition = new ParsePosition(0);
                int i = 0;
                String dateString = readTag(parser, name);
                while(article.published == null && i < dateFormats.length){
                    article.published = dateFormats[i].parse(dateString, parsePosition);
                    i++;
                }
            }else{
                skip(parser);
            }
        }
        return article;
    }

    // Processes tags in the feed.
    private String readTag(XmlPullParser parser, String name) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, name);
        String result = readText(parser);
        parser.require(XmlPullParser.END_TAG, null, name);
        return result;
    }

    // For the tags title and summary, extracts their text values.
    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private String readAuthors(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        parser.require(XmlPullParser.START_TAG, null, parser.getName());
        while(parser.next() != XmlPullParser.END_TAG){
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                if(parser.getEventType() == XmlPullParser.TEXT && !parser.getText().trim().equals("")) {
                    result += parser.getText();
                }
                continue;
            }
            result+=readTag(parser, "name");
        }
        parser.require(XmlPullParser.END_TAG, null, parser.getName());
        return result.replaceAll("/(, )\\Z/", "");
    }

    // Skips tags the parser isn't interested in. Uses depth to handle nested tags. i.e.,
    // if the next tag after a START_TAG isn't a matching END_TAG, it keeps going until it
    // finds the matching END_TAG (as indicated by the value of "depth" being 0).
    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    private void initializeTagDictionaries() {  // FIXME: call toLowerCase() on all parsed tags
        feedTags = createHashSet("feed", "channel");
        feedTitleTags = createHashSet("title");
        feedIconTags = createHashSet("icon");
        feedLinkTags = createHashSet("link");

        entryTags = createHashSet("entry", "item");
        entryPublishedTags = createHashSet("published", "pubDate");
        entryTitleTags = createHashSet("title");
        entryContentTags = createHashSet("content", "content:encoded");
        entryLinkTags = createHashSet("link");
        entryAuthorTags = createHashSet("author", "dc:creator");
    }

    private HashSet<String> createHashSet(String... strings){
        HashSet<String> hashSet = new HashSet<>();
        Collections.addAll(hashSet, strings);
        return hashSet;
    }
}