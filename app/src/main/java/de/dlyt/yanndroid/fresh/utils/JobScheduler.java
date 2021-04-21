package de.dlyt.yanndroid.fresh.utils;

import android.app.job.JobInfo;
import android.content.ComponentName;
import android.content.Context;
import android.util.Log;

import de.dlyt.yanndroid.fresh.Constants;
import de.dlyt.yanndroid.fresh.hub.utils.Preferences;
import de.dlyt.yanndroid.fresh.services.UpdateCheckService;

public class JobScheduler implements Constants {
    public static void setupJobScheduler(Context context, boolean cancel) {
        ComponentName serviceName = new ComponentName(context, UpdateCheckService.class);
        android.app.job.JobScheduler scheduler = (android.app.job.JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        int JOB_ID = 1771;

        if (cancel) {
            if (scheduler != null) {
                if (DEBUGGING) Log.d(Tools.TAG, "Cancelling job");
                scheduler.cancel(JOB_ID);
            }
        } else {
            long requestedInterval;
            long requestedFlex;

            if (DEBUG_NOTIFICATIONS) {
                requestedInterval = 900 * 1000;
                requestedFlex = 900 * 500;
            } else {
                requestedInterval = Preferences.getBackgroundFrequency(context) * 1000; // sec to ms;
                requestedFlex = Preferences.getBackgroundFrequency(context) * 500; // sec to ms;
            }

            JobInfo jobInfo = new JobInfo.Builder(JOB_ID, serviceName)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setRequiresDeviceIdle(false)
                    .setRequiresCharging(false)
                    .setPeriodic(requestedInterval, requestedFlex)
                    .build();

            int result = scheduler.schedule(jobInfo);
            if (result == android.app.job.JobScheduler.RESULT_SUCCESS) {
                Log.d(Tools.TAG, "Job scheduled successfully!");
                Log.d(Tools.TAG, "Job scheduled for " + requestedInterval + " ms!");
            }
        }
    }

    public static void setBackgroundCheck(Context context, boolean set) {
        setupJobScheduler(context, !set);
    }
}
