package de.dlyt.yanndroid.freshapp.tasks;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;

import de.dlyt.yanndroid.freshapp.receivers.AppReceiver;
import de.dlyt.yanndroid.freshapp.utils.Constants;
import de.dlyt.yanndroid.freshapp.utils.Preferences;
import de.dlyt.yanndroid.freshapp.utils.Utils;

public class OtaJobScheduler extends JobService implements Constants {
    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        doCheckOta(params);
        return true;
    }

    private void doCheckOta(final JobParameters params) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getApplicationContext(), AppReceiver.class);
                Utils.setupJobScheduler(getApplicationContext(), !Preferences.getBackgroundService(getApplicationContext()));
                intent.setAction(START_UPDATE_CHECK);
                jobFinished(params, false);
            }
        }).start();
    }
}