package com.timkonieczny.rss;

import android.app.job.JobParameters;
import android.app.job.JobService;

public class UpdateService extends JobService {
    @Override
    public boolean onStartJob(final JobParameters jobParameters) {
        new BackgroundFeedTask(){
            @Override
            protected void onPostExecute(Void aVoid){   // TODO: rather create single JobService for each source
                jobFinished(jobParameters, false);      // TODO: check JobService success. Don't just pass false
            }
        };
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }
}