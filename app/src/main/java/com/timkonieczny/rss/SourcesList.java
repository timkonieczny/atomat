package com.timkonieczny.rss;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

class SourcesList extends ArrayList<Source> {

    private HashSet<String> rssUrls;
    private HashSet<Long> dbIds;

    SourcesList(){
        super();
        rssUrls = new HashSet<>();
        dbIds = new HashSet<>();
    }

    @Override
    public boolean add(Source s) {
        rssUrls.add(s.rssUrl);
        if(s.dbId != -1) dbIds.add(s.dbId);
        return super.add(s);
    }

    @Override
    public boolean addAll(Collection<? extends Source> c) {
        for (Object o : c){
            rssUrls.add(((Source)o).rssUrl);
            if(((Source)o).dbId != -1) addDbId(((Source)o).dbId);
        }
        return super.addAll(c);
    }

    boolean containsRssUrl(String rssUrl){
        return rssUrls.contains(rssUrl);
    }

    boolean containsDbId(long dbId){
        return dbIds.contains(dbId);
    }

    Source getByDbId(long dbId){
        for(int i = 0; i < this.size(); i++){
            Source source = this.get(i);
            if(source.dbId == dbId) return source;
        }
        return null;
    }

    void addDbId(long dbId){
        dbIds.add(dbId);
    }
}