package com.example.goplaneticket;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.util.Log;

public class SampleJobService extends JobService {

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d("SampleJobService", "Job futtatása...");
        jobFinished(params, false);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d("SampleJobService", "Job megszakítva.");
        return true;
    }
}
