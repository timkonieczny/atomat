package com.timkonieczny.rss;

import android.content.ContentValues;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class AtomParser {

    static final int SUCCESS = 0;
    static final int ERROR_IO = 1;
    static final int ERROR_XML = 2;

    private DbManager dbManager;
    private SimpleDateFormat[] dateFormats;
    private Pattern imgWithWhitespacePattern, imgPattern, stylePattern;
    private XmlPullParser parser;
    private ContentValues sourceContentValues;
    private ArrayList<ContentValues> newArticles;
    private ArrayList<ContentValues> newImages;
    private HashSet<String> existingLinks;
    private boolean hasOverlappingArticles;
    private long oldestArticleDate;

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
    private HashSet<String> authorNameTags;

    ArrayList<String> newStoriesTitles;
    ArrayList<String> newArticlesSourcesTitles;

    AtomParser(DbManager dbManager) {
        this.dbManager = dbManager;

        initializeTagDictionaries();
        imgWithWhitespacePattern = Pattern.compile("\\A\\s*<img(.*?)>\\s*");  // <imgPattern ... /> at beginning of input, including trailing whitespaces
        imgPattern = Pattern.compile("<img(?:.*?)src=\"(.*?)\"(?:.*?)>"); // src attribute of <imgPattern ... />
        stylePattern = Pattern.compile("<style>(?:.*?)</style>"); // src attribute of <imgPattern ... />
        dateFormats = new SimpleDateFormat[]{
                new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ssX", Locale.US),
                new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ssz", Locale.US),
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.US),
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.US)
        };

        parser = Xml.newPullParser();
        try {
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
    }


    int parseAll() throws XmlPullParserException, IOException {
        int totalNewArticles = 0;
        newArticlesSourcesTitles = new ArrayList<>();
        String[][] sources = dbManager.getSourceInfos();
        for (String[] source : sources) {
            int newArticles = parse(Long.parseLong(source[0]), source[1], source[2], source[3], false);
            if(newArticles > 0) {
                totalNewArticles += newArticles;
                newArticlesSourcesTitles.add(source[4]);
            }
        }
        return totalNewArticles;
    }

    int parse(long dbId, String url, String lastModified, String eTag, boolean isNewSource) throws XmlPullParserException, IOException {

        if(newStoriesTitles == null) newStoriesTitles = new ArrayList<>();
        newArticles = new ArrayList<>();
        newImages = new ArrayList<>();
        hasOverlappingArticles = false;
        oldestArticleDate = -1;

        HttpURLConnection connection = (HttpURLConnection) (new URL(url)).openConnection();
        connection.setReadTimeout(10000);
        connection.setConnectTimeout(15000);
        if(lastModified != null)
            connection.setRequestProperty("If-Modified-Since", lastModified);
        if(eTag != null)
            connection.setRequestProperty("If-None-Match", eTag);

        connection.connect();
        switch (connection.getResponseCode()){      // if file has changed since last request
            case HttpURLConnection.HTTP_OK:         // otherwise HttpURLConnection.HTTP_NOT_MODIFIED
                lastModified = connection.getHeaderField("Last-Modified");
                eTag = connection.getHeaderField("ETag");

                InputStream inputStream = connection.getInputStream();

                sourceContentValues = new ContentValues();

                sourceContentValues.put(DbManager.SourcesTable.COLUMN_NAME_LAST_MODIFIED, lastModified);
                sourceContentValues.put(DbManager.SourcesTable.COLUMN_NAME_ETAG, eTag);

                parser.setInput(inputStream, null);

                while (parser.next() != XmlPullParser.END_TAG) {
                    if (parser.getEventType() != XmlPullParser.START_TAG) {
                        continue;
                    }
                    if(feedTags.contains(parser.getName())){
                        readFeed(parser, isNewSource);

                        if(isNewSource){
                            sourceContentValues.put(DbManager.SourcesTable.COLUMN_NAME_URL, url);
                            dbId = dbManager.insertRow(DbManager.SourcesTable.TABLE_NAME, sourceContentValues);
                        }else if (!hasOverlappingArticles){
                            ContentValues contentValues = new ContentValues();
                            contentValues.put(DbManager.ArticlesTable.COLUMN_NAME_IS_PLACEHOLDER, true);
                            contentValues.put(DbManager.ArticlesTable.COLUMN_NAME_PUBLISHED, oldestArticleDate-1);
                            contentValues.put(DbManager.ArticlesTable.COLUMN_NAME_SOURCE_ID, dbId);
                            newArticles.add(contentValues);
                            newImages.add(null);
                        }

                        dbManager.bulkInsertArticles(newArticles, newImages, dbId);
                        break;
                    }
                }
                inputStream.close();
                break;
        }

        connection.disconnect();
        return newArticles.size();
    }

    private void readFeed(XmlPullParser parser, boolean isNewSource) throws XmlPullParserException, IOException {

        String title;
        String iconUrl;
        existingLinks = dbManager.getExistingArticleLinks();

        if(feedTags.contains(parser.getName())) {
            parser.require(XmlPullParser.START_TAG, null, parser.getName());
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }

                String name = parser.getName();

                // Starts by looking for the entry tag
                if (entryTags.contains(name)) {
                    readEntry(parser);
                } else if (isNewSource) {
                    if (feedTitleTags.contains(name)) {
                        title = readTag(parser, name);
                        sourceContentValues.put(DbManager.SourcesTable.COLUMN_NAME_TITLE, title);
                    } else if (feedIconTags.contains(name)) {
                        iconUrl = readTag(parser, name);
                        sourceContentValues.put(DbManager.SourcesTable.COLUMN_NAME_ICON_URL, iconUrl);
                    } else if (feedLinkTags.contains(name)) {
                        String linkUrl = null;
                        if(parser.getAttributeValue(null, "rel")!=null && parser.getAttributeValue(null, "rel").equals("alternate"))
                            linkUrl = parser.getAttributeValue(null, "href");
                        if(linkUrl!=null)
                            parser.next();
                        else{
                            linkUrl = readTag(parser, name);
                        }
                        if(linkUrl!=null && !linkUrl.equals("")) {
                            sourceContentValues.put(DbManager.SourcesTable.COLUMN_NAME_WEBSITE, linkUrl);
                        }
                    }else{
                        skip(parser);
                    }
                } else {
                    skip(parser);
                }
            }
        }
    }

    // Parses the contents of an entry. If it encounters a title, summary, or link tag, hands them
    // off  to their respective methods for processing. Otherwise, skips the tag.
    private void readEntry(XmlPullParser parser) throws XmlPullParserException, IOException {
        String title = "null";
        String content = "null";
        String headerUri = null;
        String link = null;
        String author = "null";
        String published = "";

        parser.require(XmlPullParser.START_TAG, null, parser.getName());
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();

            if(entryTitleTags.contains(name)){
                title = readTag(parser, name);
            }else if(entryContentTags.contains(name)){
                content = readTag(parser, name);
                Matcher matcher = imgPattern.matcher(content);
                if (matcher.find()) headerUri = matcher.group(1);
                content = imgWithWhitespacePattern.matcher(content).replaceFirst("");
                content = stylePattern.matcher(content).replaceAll("");
            }else if(entryLinkTags.contains(name)){
                if(link == null) {
                    if (parser.getAttributeValue(null, "rel")!=null && parser.getAttributeValue(null, "rel").equals("alternate"))
                        link = parser.getAttributeValue(null, "href");
                    if (link != null)
                        parser.next();
                    else
                        link = readTag(parser, name);
                    if(link.equals("")) link = null;
                }else{
                    parser.next();
                }
            }else if(entryAuthorTags.contains(name)){
                author = readAuthors(parser);
            }else if(entryPublishedTags.contains(name)){
                ParsePosition parsePosition = new ParsePosition(0);
                int i = 0;
                String dateString = readTag(parser, name);
                Date date = null;
                while(date == null && i < dateFormats.length){
                    date = dateFormats[i].parse(dateString, parsePosition);
                    i++;
                }
                if(date != null) published = String.valueOf(date.getTime());
                if(oldestArticleDate == -1 || date.getTime() < oldestArticleDate)
                    oldestArticleDate = date.getTime();
            }else{
                skip(parser);
            }
        }
        if(!existingLinks.contains(link)) {
            ContentValues articlesValues = new ContentValues();
            articlesValues.put(DbManager.ArticlesTable.COLUMN_NAME_TITLE, title);
            articlesValues.put(DbManager.ArticlesTable.COLUMN_NAME_AUTHOR, author);
            articlesValues.put(DbManager.ArticlesTable.COLUMN_NAME_CONTENT, content);
            articlesValues.put(DbManager.ArticlesTable.COLUMN_NAME_PUBLISHED, published);
            articlesValues.put(DbManager.ArticlesTable.COLUMN_NAME_URL, link);
            newArticles.add(articlesValues);
            if (headerUri != null) {
                ContentValues headerValues = new ContentValues();
                headerValues.put(DbManager.ImagesTable.COLUMN_NAME_URL, headerUri);
                headerValues.put(DbManager.ImagesTable.COLUMN_NAME_TYPE, Image.TYPE_HEADER);
                newImages.add(headerValues);
            } else newImages.add(null);
            newStoriesTitles.add(title);
        }else{
            hasOverlappingArticles = true;
        }
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
            }else if(authorNameTags.contains(parser.getName())){
                result+=readTag(parser, parser.getName());
            }else skip(parser);
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

    private void initializeTagDictionaries() {
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
        authorNameTags = createHashSet("name");
    }

    private HashSet<String> createHashSet(String... strings){
        HashSet<String> hashSet = new HashSet<>();
        Collections.addAll(hashSet, strings);
        return hashSet;
    }
}