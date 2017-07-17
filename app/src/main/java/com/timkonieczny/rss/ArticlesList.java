package com.timkonieczny.rss;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

class ArticlesList extends ArrayList<Article>{

    private HashSet<String> links;

    ArticlesList(){
        super();
        links = new HashSet<>();
    }

    @Override
    public boolean add(Article article) {
        links.add(article.link);
        return super.add(article);
    }

    @Override
    public boolean addAll(Collection<? extends Article> c) {
        for (Object o : c) links.add(((Article)o).link);
        return super.addAll(c);
    }

    boolean containsLink(String link){
        return links.contains(link);
    }
}
