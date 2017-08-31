package com.timkonieczny.rss;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.MenuItem;
import android.widget.PopupMenu;

class Source extends DbRow implements ImageListener, PopupMenu.OnMenuItemClickListener{

    String title, rssUrl;
    private String link;
    Image icon;

    Context context;

    SourceChangedListener sourceChangedListener;

    Source(Context context, String rssUrl, String title, String link, String iconUrl, String iconFileName, long dbId){
        this.context = context;
        this.rssUrl = rssUrl;
        this.title = title;
        this.link = link;
        this.icon = new Image(Image.TYPE_ICON);
        this.icon.url = iconUrl;
        this.icon.fileName = iconFileName;
        this.dbId = dbId;
    }

    Drawable getIconDrawable(SourceChangedListener sourceChangedListener){
        this.sourceChangedListener = sourceChangedListener;
        return icon.getDrawable(context, this, title, Image.TYPE_ICON, dbId);
    }

    private void destroy(){
        for(int i = 0; i < MainActivity.articles.size(); i++)
            if(MainActivity.articles.get(i).source == this){
                MainActivity.articles.get(i).destroy();
                MainActivity.articles.remove(i);
                i--;
            }

        icon.destroy(context);
        DbManager dbManager = new DbManager(context);
        dbManager.deleteSource(this);
        dbManager.close();
        MainActivity.sources.removeByDbId(dbId);
        if(sourceChangedListener!=null) sourceChangedListener.onSourceChanged(this);
    }

    @Override
    public String toString(){
        return "Title:\t\t\t"+title+
            "\nIcon:\t\t\t\t"+ icon.url+
            "\nLink:\t\t\t\t"+ link+
            "\nIcon Drawable:\t"+(icon.drawable != null)+
            "\nIcon File Name:\t"+ icon.fileName +
            "\nRSS URL:\t\t\t"+rssUrl;
    }

    @Override
    public void onImageLoaded(int index) {
        sourceChangedListener.onSourceChanged(this);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()){
            case R.id.delete_source_menu_item:
                destroy();
                return true;
            default:
                return false;
        }
    }
}