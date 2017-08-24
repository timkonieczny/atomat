package com.timkonieczny.rss;

import java.util.Collection;
import java.util.HashSet;

class SourcesList extends DbList<Source> {

    private HashSet<String> rssUrls;

    SourcesList(){
        super();
        rssUrls = new HashSet<>();
    }

    @Override
    public boolean add(Source s) {
        rssUrls.add(s.rssUrl);
        return super.add(s);
    }

    @Override
    public boolean addAll(Collection<? extends Source> c) {
        for (Object o : c) rssUrls.add(((Source)o).rssUrl);
        return super.addAll(c);
    }

    boolean containsRssUrl(String rssUrl){
        return rssUrls.contains(rssUrl);
    }

    @Override
    void removeByDbId(Long dbId){
        rssUrls.remove(this.getByDbId(dbId).rssUrl);
        super.removeByDbId(dbId);
    }
}