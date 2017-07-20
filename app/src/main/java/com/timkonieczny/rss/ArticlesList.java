package com.timkonieczny.rss;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

class ArticlesList extends ArrayList<Article>{

    private HashSet<Long> dbIds;

    ArticlesList(){
        super();
        dbIds = new HashSet<>();
    }

    @Override
    public boolean add(Article article) {
        dbIds.add(article.dbId);
        return super.add(article);
    }

    @Override
    public boolean addAll(Collection<? extends Article> c) {
        for (Object o : c){
            dbIds.add(((Article)o).dbId);
        }
        return super.addAll(c);
    }

    boolean containsDbId(long dbId){
        return dbIds.contains(dbId);
    }

    Article getByDbId(long dbId){
        for(int i = 0; i < this.size(); i++){
            Article article = this.get(i);
            if(article.dbId == dbId) return article;
        }
        return null;
    }

    void removeByDbId(Long dbId){
        for(int i = 0; i < this.size(); i++){
            if(this.get(i).dbId == dbId){
                this.remove(i);
                dbIds.remove(dbId);
                break;
            }
        }
    }
}
