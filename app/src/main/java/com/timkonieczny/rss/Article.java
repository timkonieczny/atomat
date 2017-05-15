package com.timkonieczny.rss;

import java.net.URL;
import java.util.Date;

class Article {

    Date published, updated;
    String title, content, id, author, uniqueId;
    URL link, headerImage;
    Source source;
}
