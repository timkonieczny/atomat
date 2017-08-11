package com.timkonieczny.rss;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.util.Log;

public class UpdateService extends JobService {
    @Override
    public boolean onStartJob(final JobParameters jobParameters) {
        Log.d("UpdateService", "onStartJob()");
        new BackgroundFeedTask(){
            @Override
            protected void onPostExecute(Void aVoid){
                jobFinished(jobParameters, false);
            }
        }.execute(jobParameters.getExtras().getLong("dbId"));
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }
}