package com.timkonieczny.rss;

import android.text.Html;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class SourceUpdater {

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd'T'HH:mm:ssX", Locale.US);
    private Pattern imgWithWhitespace, img;
    private Source source;
    private boolean updateSource;

    List<Article> parse(InputStream in, Source source, boolean updateSource) throws XmlPullParserException, IOException {
        try {
            this.updateSource = updateSource;
            this.source = source;

            imgWithWhitespace = Pattern.compile("\\A<img(.*?)/>\\s*");  // <img ... /> at beginning of input, including trailing whitespaces
            img = Pattern.compile("<img(?:.*?)src=\"(.*?)\"(?:.*?)/>"); // src attribute of <img ... />

            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readFeed(parser);
        } finally {
            in.close();
        }
    }

    private List<Article> readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
        ArrayList<Article> articles = new ArrayList<>();

        parser.require(XmlPullParser.START_TAG, null, "feed");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("entry")) {
                articles.add(readEntry(parser));
            } else if(updateSource) {
                switch (name){
                    case "title":
                        source.title = readTag(parser, "title");
                        break;
                    case "icon":
                        source.icon = readTag(parser, "icon");
                        source.updateIconImage();
                        break;
                    case "updated":
                        try {
                            source.updated = dateFormat.parse(readTag(parser, "updated"));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "id":
                        source.id = readTag(parser, "id");
                        break;
                    case "link":
                        source.link = new URL(parser.getAttributeValue(null, "href"));
                        parser.next();
                        break;
                }
            }else{
                skip(parser);
            }
        }
        for(int i = 0; i < articles.size(); i++){
            Article article = articles.get(i);
            article.uniqueId = article.source.id + "_" + article.id;
        }
        return articles;
    }

    // Parses the contents of an entry. If it encounters a title, summary, or link tag, hands them
    // off
    // to their respective &quot;read&quot; methods for processing. Otherwise, skips the tag.
    private Article readEntry(XmlPullParser parser) throws XmlPullParserException, IOException {

        Article article = new Article();
        article.source = source;

        parser.require(XmlPullParser.START_TAG, null, "entry");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            switch (name) {
                case "title":
                    article.title = readTag(parser, "title");
//                title = readTitle(parser);
                    break;
                case "content":
                    article.content = readTag(parser, "content");
                    Matcher matcher = img.matcher(article.content);
                    if (matcher.find()) {
                        article.headerImage = matcher.group(1);
                    }
                    article.content = Html.fromHtml(imgWithWhitespace.matcher(article.content).replaceFirst(""), Html.FROM_HTML_MODE_COMPACT);
                    break;
                case "link":
                    article.link = readLink(parser);
                    break;
                case "id":
                    article.id = readTag(parser, "id");
                    break;
                case "author":
                    article.author = readAuthors(parser);
                    break;
                case "published":
                    try {
                        article.published = dateFormat.parse(readTag(parser, "published"));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    break;
                case "updated":
                    try {
                        article.published = dateFormat.parse(readTag(parser, "updated"));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    skip(parser);
                    break;
            }
        }
        return article;
    }

    // Processes link tags in the feed.
    private URL readLink(XmlPullParser parser) throws IOException, XmlPullParserException {
        String link = "";
        parser.require(XmlPullParser.START_TAG, null, "link");
        String tag = parser.getName();
        String relType = parser.getAttributeValue(null, "rel");
        if (tag.equals("link")) {
            if (relType.equals("alternate")) {
                link = parser.getAttributeValue(null, "href");
                parser.nextTag();
            }
        }
        parser.require(XmlPullParser.END_TAG, null, "link");
        return new URL(link);
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
        parser.require(XmlPullParser.START_TAG, null, "author");
        while(parser.next() != XmlPullParser.END_TAG){
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            result+=readTag(parser, "name");
        }
        parser.require(XmlPullParser.END_TAG, null, "author");
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
}