package com.timkonieczny.rss;

import java.net.URL;
import java.util.Date;

public class Entry {

    public Date published, updated;
    public String title, content, id, author, uniqueId;
    public URL link, headerImage;
    public Source source;

    public String toString(){
        return title + " by " + author;
    }
}
