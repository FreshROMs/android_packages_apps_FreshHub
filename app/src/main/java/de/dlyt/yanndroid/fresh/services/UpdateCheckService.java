package de.dlyt.yanndroid.fresh.services;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import de.dlyt.yanndroid.fresh.Constants;
import de.dlyt.yanndroid.fresh.database.TnsOta;
import de.dlyt.yanndroid.fresh.hub.AddonActivity;
import de.dlyt.yanndroid.fresh.hub.utils.Preferences;
import de.dlyt.yanndroid.fresh.services.download.DownloadRom;
import de.dlyt.yanndroid.fresh.utils.Notifications;

public class UpdateCheckService extends JobService implements Constants {

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
        Handler handler = new Handler(Looper.getMainLooper());

        Notifications.sendOngoingCheckNotification(this);
        new TnsOtaApiService(this, false);
        new Thread(new Runnable() {
            final Context context = UpdateCheckService.this;

            @Override
            public void run() {
                boolean updateAvailable = TnsOta.getUpdateAvailability(context);
                boolean updateAutomatic = Preferences.getBackgroundDownload(context);
                String relversion = TnsOta.getReleaseVersion(context);
                String relvariant = TnsOta.getReleaseVariant(context);

                handler.postDelayed(() -> {
                    Notifications.cancelOngoingCheckNotification(context);

                    if (updateAvailable && updateAutomatic) {
                        new DownloadRom();
                    } else if (updateAvailable) {
                        Notifications.sendUpdateNotification(context, relversion, relvariant);
                    }
                }, 3000);

                jobFinished(params, false);
            }
        }).start();
    }
}