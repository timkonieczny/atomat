package com.timkonieczny.rss;

import android.app.job.JobParameters;
import android.app.job.JobService;

public class UpdateService extends JobService {

    // TODO: UpdateService is run on scheduling. Delay?
    // TODO: Delay UpdateService after manual refresh?
    @Override
    public boolean onStartJob(final JobParameters jobParameters) {
        new BackgroundFeedTask(new DbManager(getApplicationContext()), getApplicationContext()){
            @Override
            protected void onPostExecute(Void aVoid){
                jobFinished(jobParameters, false);
            }
        }.execute();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }
}