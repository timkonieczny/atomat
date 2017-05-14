package com.timkonieczny.rss;

import java.util.ArrayList;
import java.util.Date;

public class Source {

    public String title, icon, link, id;
    public Date updated;
    public ArrayList<Entry> entries;

    public Source(){
        entries = new ArrayList<>();
    }

    public String toString(){
        String string =
                "\nTitle: "+title+
                        "\nIcon: "+ icon +
                        "\nUpdated: "+updated.toString()+
                        "\nID: "+ id +
                        "\nLink: "+ link +"\n\n\n";

        for(int i = 0; i < entries.size(); i++){
            string +=
                    "\nPublished: "+entries.get(i).published +
                            "\nTitle: "+entries.get(i).title+
                            "\nUpdated: "+entries.get(i).updated +
                            "\nID: "+entries.get(i).id +
                            "\nLink: "+entries.get(i).link +
                            "\nContent: "+entries.get(i).content+
                            "\nAuthor: "+entries.get(i).author+"\n\n";
        }

        return string;
    }

}
