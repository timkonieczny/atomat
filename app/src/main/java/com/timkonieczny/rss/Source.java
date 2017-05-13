package com.timkonieczny.rss;

import java.util.ArrayList;
import java.util.Date;

public class Source {

    public String title, iconURL, websiteURL, idURL;
    public Date updated;
    public ArrayList<Entry> entries;

    public Source(){
        entries = new ArrayList<>();
    }

}
