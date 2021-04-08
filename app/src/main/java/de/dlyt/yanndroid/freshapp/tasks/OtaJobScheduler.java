package de.dlyt.yanndroid.freshapp.tasks;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;

import de.dlyt.yanndroid.freshapp.utils.Constants;
import de.dlyt.yanndroid.freshapp.utils.RomUpdate;
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
        new LoadUpdateManifest(this, false).execute();
        new Thread(new Runnable() {
            final Context context = OtaJobScheduler.this;

            @Override
            public void run() {
                boolean updateAvailable = RomUpdate.getUpdateAvailability(context);
                boolean updateIgnored = Utils.isUpdateIgnored(context);
                String relversion = RomUpdate.getReleaseVersion(context);
                String relvariant = RomUpdate.getReleaseVariant(context);

                if (updateAvailable && !updateIgnored) {
                    Utils.setupNotification(context, relversion, relvariant);
                }

                jobFinished(params, false);
            }
        }).start();
    }
}