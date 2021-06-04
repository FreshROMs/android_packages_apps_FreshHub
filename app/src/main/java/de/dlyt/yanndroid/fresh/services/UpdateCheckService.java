package de.dlyt.yanndroid.fresh.services;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Handler;
import android.os.Looper;

import de.dlyt.yanndroid.fresh.Constants;
import de.dlyt.yanndroid.fresh.database.TnsOta;
import de.dlyt.yanndroid.fresh.database.TnsOtaDownload;
import de.dlyt.yanndroid.fresh.hub.utils.Preferences;
import de.dlyt.yanndroid.fresh.services.download.DownloadRom;
import de.dlyt.yanndroid.fresh.utils.Notifications;

public class UpdateCheckService extends JobService implements Constants {


    @Override
    public boolean onStopJob(JobParameters params) {
        Handler handler = new Handler(Looper.getMainLooper());

        new Thread(new Runnable() {
            final Context context = UpdateCheckService.this;
            @Override
            public void run() {
                handler.postDelayed(() -> {
                    Notifications.cancelOngoingCheckNotification(context);
                }, 5000);
            }
        }).start();
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

                ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());

                String relVersion = TnsOta.getReleaseVersion(context);
                String relVariant = TnsOta.getReleaseVariant(context);

                handler.postDelayed(() -> {
                    Long currentTimeMillis = System.currentTimeMillis();
                    TnsOtaDownload.setUpdateLastChecked(context, currentTimeMillis);
                    Notifications.cancelOngoingCheckNotification(context);

                    if (capabilities != null) {
                        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                            if (updateAvailable && updateAutomatic) {
                                DownloadRom downloadRom = new DownloadRom();
                                downloadRom.startDownload(context);
                            } else if (updateAvailable) {
                                Notifications.sendUpdateNotification(context, relVersion, relVariant);
                            }
                        } else {
                            if (updateAvailable) {
                                Notifications.sendUpdateNotification(context, relVersion, relVariant);
                            }
                        }
                        ;
                    }
                }, 5000);

                jobFinished(params, false);
            }
        }).start();
    }
}