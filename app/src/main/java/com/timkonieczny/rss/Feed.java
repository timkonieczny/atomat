package com.timkonieczny.rss;

import android.icu.text.SimpleDateFormat;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Feed extends AsyncTask<URL, Void, ArrayList<Entry>> {

    private static final String ns = null;

//    private Source source;
    private ArrayList<Entry> entries;
    private HashSet<String> existingIds;

    private SimpleDateFormat dateFormat;

    public FeedListener feedListener;

    private Pattern imgWithWhitespace;
    private Pattern img;
    private Matcher matcher;


    public Feed(FeedListener feedListener, ArrayList<Entry> existingEntries){
        dateFormat = new SimpleDateFormat("yyyy-mm-dd'T'HH:mm:ssX");
        imgWithWhitespace = Pattern.compile("\\s*<img(.*?)/>\\s*");
        img = Pattern.compile("<img(?:.*?)src=\"(.*?)\"(?:.*?)/>");

        this.feedListener = feedListener;
        existingIds = getExistingEntryIds(existingEntries);
    }

    @Override
    protected ArrayList<Entry> doInBackground(URL... params) {

        entries = new ArrayList<>();

        for(URL url : params){
            try {
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.setReadTimeout(10000);
                connection.setConnectTimeout(15000);

                // Starts the query
                connection.connect();
                InputStream stream = connection.getInputStream();

                parse(stream);

            } catch (IOException | XmlPullParserException e) {
                e.printStackTrace();
            }
        }

        Entry entry;
        for(int i = 0; i < entries.size(); i++){
            entry = entries.get(i);
            entry.uniqueId = entry.source.id + "_" + entry.id;
            if(existingIds.contains(entry.uniqueId)){
                entries.remove(i);
                i--;
            }
        }

        return entries;
    }


    private void parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            parser.require(XmlPullParser.START_TAG, ns, "feed");

            //parseNextTag(parser);
            readSource(parser);
            //return readFeed(parser);
        } finally {
            in.close();
        }
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

    private void readSource(XmlPullParser parser) throws IOException, XmlPullParserException {
        Source source = new Source();
        while(parseNextTag(parser) != XmlPullParser.END_TAG){
            if(parser.getEventType() == XmlPullParser.START_TAG){
                switch (parser.getName()){
                    case "entry":
                        readEntry(parser, source);
                        break;
                    case "title":
                        if(parseNextTag(parser) == XmlPullParser.TEXT)
                            source.title = parser.getText().trim();
                        parseNextTag(parser);
                        break;
                    case "icon":
                        if(parseNextTag(parser) == XmlPullParser.TEXT)
                            source.icon = parser.getText().trim();
                        parseNextTag(parser);
                        break;
                    case "updated":
                        if(parseNextTag(parser) == XmlPullParser.TEXT)
                            try {
                                source.updated = dateFormat.parse(parser.getText().trim());
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        parseNextTag(parser);
                        break;
                    case "id":
                        if(parseNextTag(parser) == XmlPullParser.TEXT)
                            source.id = parser.getText().trim();
                        parseNextTag(parser);
                        break;
                    case "link":
                        source.link = parser.getAttributeValue(null, "href");
                        parseNextTag(parser);
                        break;
                }
            }else if(parser.getEventType() == XmlPullParser.TEXT){
                parseNextTag(parser);
            }
        }
    }

    private void readEntry(XmlPullParser parser, Source source) throws IOException, XmlPullParserException {
        Entry entry = new Entry();
        entry.source = source;
        while(parseNextTag(parser) != XmlPullParser.END_TAG){
            if(parser.getEventType() == XmlPullParser.START_TAG){
                switch (parser.getName()){
                    case "published":
                        if(parseNextTag(parser) == XmlPullParser.TEXT)
                            try {
                                entry.published = dateFormat.parse(parser.getText().trim());
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        parseNextTag(parser);
                        break;
                    case "title":
                        if(parseNextTag(parser) == XmlPullParser.TEXT)
                            entry.title = parser.getText().trim();
                        parseNextTag(parser);
                        break;
                    case "updated":
                        if(parseNextTag(parser) == XmlPullParser.TEXT)
                            try {
                                entry.updated = dateFormat.parse(parser.getText().trim());
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        parseNextTag(parser);
                        break;
                    case "id":
                        if(parseNextTag(parser) == XmlPullParser.TEXT)
                            entry.id = parser.getText().trim();
                        parseNextTag(parser);
                        break;
                    case "link":
                        entry.link = parser.getAttributeValue(null, "href");
                        parseNextTag(parser);
                        break;
                    case "content":
                        if(parseNextTag(parser) == XmlPullParser.TEXT) {
                            entry.content = parser.getText();
                            matcher = img.matcher(entry.content);
                            if (matcher.find()){
                                for(int i = 0; i <= matcher.groupCount(); i++) {
                                    entry.headerImage = matcher.group(i);
                                }
                            }
                            entry.content = imgWithWhitespace.matcher(entry.content).replaceFirst("");
                        }
                        parseNextTag(parser);
                        break;
                    case "author":
                        if(parseNextTag(parser) == XmlPullParser.START_TAG && parser.getName().equals("name")){
                            if(parseNextTag(parser) == XmlPullParser.TEXT)        // TODO: multi-author support
                                entry.author = parser.getText().trim();
                            parseNextTag(parser);
                        }
                        parseNextTag(parser);
                        break;
                }
            }else if(parser.getEventType() == XmlPullParser.TEXT){
                parseNextTag(parser);
            }
        }
        entries.add(entry);
    }

    private int parseNextTag(XmlPullParser parser) throws IOException, XmlPullParserException {
        if(parser.next() == XmlPullParser.TEXT && parser.isWhitespace())
            return parseNextTag(parser);
        else
            return parser.getEventType();
    }

    @Override
    protected void onPostExecute(ArrayList<Entry> entries) {
        super.onPostExecute(entries);
        feedListener.onSourcesUpdated(entries);
    }

    private HashSet<String> getExistingEntryIds(ArrayList<Entry> existingEntries){
        HashSet<String> ids = new HashSet<String>();
        for(int i = 0; i < existingEntries.size(); i++){
            ids.add(existingEntries.get(i).uniqueId);
        }
        return ids;
    }

}