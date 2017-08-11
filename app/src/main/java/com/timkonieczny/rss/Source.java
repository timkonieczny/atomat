package com.timkonieczny.rss;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.PersistableBundle;
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

        rescheduleBackgroundUpdate();
    }

    Drawable getIconDrawable(SourceChangedListener sourceChangedListener){
        this.sourceChangedListener = sourceChangedListener;
        return icon.getDrawable(context, this, title, Image.TYPE_ICON);
    }

    private void destroy(){
        for(int i = 0; i < MainActivity.articles.size(); i++)
            if(MainActivity.articles.get(i).source == this){
                MainActivity.articles.get(i).destroy();
                MainActivity.articles.remove(i);
                i--;
            }

        icon.destroy(context);
        MainActivity.dbManager.deleteSource(this);
        if(sourceChangedListener!=null) sourceChangedListener.onSourceChanged(this);
    }

    void rescheduleBackgroundUpdate(){      // TODO: test if job is run after reboot
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if(jobScheduler.getPendingJob((int)dbId) != null)
            jobScheduler.cancel((int)dbId);

        JobInfo.Builder builder = new JobInfo.Builder((int)dbId, new ComponentName(context, UpdateService.class));
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
        builder.setPeriodic(5000);      // TODO: set update frequency to reasonable time / set in preferences
        builder.setPersisted(true);
        PersistableBundle bundle = new PersistableBundle(1);
        bundle.putLong("dbId", dbId);
        builder.setExtras(bundle);
        jobScheduler.schedule(builder.build());
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
        // save icon file name in db
        MainActivity.dbManager.updateValue(DbManager.SourcesTable.TABLE_NAME,
                DbManager.SourcesTable.COLUMN_NAME_ICON_PATH, icon.fileName,
                DbManager.SourcesTable.COLUMN_NAME_URL, rssUrl);
        sourceChangedListener.onSourceChanged(this);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        destroy();
        MainActivity.sources.removeByDbId(dbId);
        return true;
    }
}