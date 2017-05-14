package com.timkonieczny.rss;

import java.util.Date;

public class Entry {

    public Date published,
    updated;
    public String title,
    content,
    link,
    id,
    author;

    public String toString(){
        return title + " by " + author;
    }
}
