package com.timkonieczny.rss;

import android.app.job.JobParameters;
import android.app.job.JobService;

public class UpdateService extends JobService {
    @Override
    public boolean onStartJob(final JobParameters jobParameters) {
        new BackgroundFeedTask(){
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