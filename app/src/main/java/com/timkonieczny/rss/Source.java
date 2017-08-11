package com.timkonieczny.rss;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.PersistableBundle;
import android.view.MenuItem;
import android.widget.PopupMenu;

class Source extends DbRow implements ImageListener, PopupMenu.OnMenuItemClickListener{

    String title, rssUrl;
    private String link;
    Image icon;
    long updateFrequency;

    ChooseUpdateFrequencyDialog chooseUpdateFrequencyDialog;

    Context context;

    SourceChangedListener sourceChangedListener;

    Source(Context context, String rssUrl, String title, String link, String iconUrl, String iconFileName, long dbId, long updateFrequency){
        this.context = context;
        this.rssUrl = rssUrl;
        this.title = title;
        this.link = link;
        this.icon = new Image(Image.TYPE_ICON);
        this.icon.url = iconUrl;
        this.icon.fileName = iconFileName;
        this.dbId = dbId;
        this.updateFrequency = updateFrequency;
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
        MainActivity.sources.removeByDbId(dbId);
        if(sourceChangedListener!=null) sourceChangedListener.onSourceChanged(this);
    }

    void changeBackgroundUpdateFrequency(){
        (new AsyncTask<Long, Void, Void>(){
            @Override
            protected Void doInBackground(Long... longs) {
                MainActivity.dbManager.updateValue(
                        DbManager.SourcesTable.TABLE_NAME,
                        DbManager.SourcesTable.COLUMN_NAME_UPDATE_FREQUENCY,
                        String.valueOf(longs[0]),
                        DbManager.SourcesTable._ID,
                        String.valueOf(dbId));
                return null;
            }

        }).execute(updateFrequency);
        rescheduleBackgroundUpdate();
    }

    void rescheduleBackgroundUpdate(){      // TODO: test if job is run after reboot
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if(jobScheduler.getPendingJob((int)dbId) != null)
            jobScheduler.cancel((int)dbId);

        JobInfo.Builder builder = new JobInfo.Builder((int)dbId, new ComponentName(context, UpdateService.class));
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
        builder.setPeriodic(updateFrequency);
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
        switch (item.getItemId()){
            case R.id.delete_source_menu_item:
                destroy();
                return true;
            case R.id.update_frequency_source_menu_item:
                chooseUpdateFrequencyDialog.show();
                return true;
            default:
                return false;
        }
    }
}