package com.timkonieczny.rss;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

class DbList<T extends DbRow> extends ArrayList<T> {

    private HashSet<Long> dbIds;

    DbList(){
        super();
        dbIds = new HashSet<>();
    }

    @Override
    public boolean add(T t) {
        if(t.dbId != DbRow.DEFAULT_DB_ID) dbIds.add(t.dbId);
        return super.add(t);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        for (T t : c) if(t.dbId != DbRow.DEFAULT_DB_ID) dbIds.add(t.dbId);
        return super.addAll(c);
    }

    T getByDbId(long dbId){
        for(int i = 0; i < this.size(); i++){
            T t = this.get(i);
            if(t.dbId == dbId) return t;
        }
        return null;
    }

    public T remove(int i){
        dbIds.remove(this.get(i).dbId);
        return super.remove(i);
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

    boolean containsDbId(long dbId){
        return dbIds.contains(dbId);
    }
}
