/*
 * Copyright (C) 2017 Nicholas Chum (nicholaschum) and Matt Booth (Kryten2k35).
 *
 * Licensed under the Attribution-NonCommercial-ShareAlike 4.0 International
 * (the "License") you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://creativecommons.org/licenses/by-nc-sa/4.0/legalcode
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.dlyt.yanndroid.freshapp.receivers;

import android.app.DownloadManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationCompat.Builder;

import java.util.Iterator;
import java.util.Set;

import de.dlyt.yanndroid.freshapp.R;
import de.dlyt.yanndroid.freshapp.activities.AddonActivity;
import de.dlyt.yanndroid.freshapp.activities.AvailableActivity;
import de.dlyt.yanndroid.freshapp.tasks.LoadUpdateManifest;
import de.dlyt.yanndroid.freshapp.utils.Constants;
import de.dlyt.yanndroid.freshapp.utils.OtaUpdates;
import de.dlyt.yanndroid.freshapp.utils.Preferences;
import de.dlyt.yanndroid.freshapp.utils.RomUpdate;
import de.dlyt.yanndroid.freshapp.utils.Utils;

public class AppReceiver extends BroadcastReceiver implements Constants {

    public final String TAG = this.getClass().getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Bundle extras = intent.getExtras();
        long mRomDownloadID = Preferences.getDownloadID(context);

        if (action.equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
            long id = extras.getLong(DownloadManager.EXTRA_DOWNLOAD_ID);
            boolean isAddonDownload = false;
            int keyForAddonDownload = 0;

            Set<Integer> set = OtaUpdates.getAddonDownloadKeySet();
            Iterator<Integer> iterator = set.iterator();

            while (iterator.hasNext() && !isAddonDownload) {
                int nextValue = iterator.next();
                if (id == OtaUpdates.getAddonDownload(nextValue)) {
                    isAddonDownload = true;
                    keyForAddonDownload = nextValue;
                    if (DEBUGGING) {
                        Log.d(TAG, "Checking ID " + nextValue);
                    }
                }
            }

            if (isAddonDownload) {
                DownloadManager downloadManager = (DownloadManager) context.getSystemService
                        (Context.DOWNLOAD_SERVICE);
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(id);
                Cursor cursor = downloadManager.query(query);

                // it shouldn't be empty, but just in case
                if (!cursor.moveToFirst()) {
                    if (DEBUGGING)
                        Log.e(TAG, "Addon Download Empty row");
                    return;
                }

                int statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                if (DownloadManager.STATUS_SUCCESSFUL != cursor.getInt(statusIndex)) {
                    if (DEBUGGING)
                        Log.w(TAG, "Download Failed");
                    Log.d(TAG, "Removing Addon download with id " + keyForAddonDownload);
                    OtaUpdates.removeAddonDownload(keyForAddonDownload);
                    AddonActivity.AddonsArrayAdapter.updateProgress(keyForAddonDownload, 0, true);
                    AddonActivity.AddonsArrayAdapter.updateButtons(keyForAddonDownload, false);
                    return;
                } else {
                    if (DEBUGGING)
                        Log.v(TAG, "Download Succeeded");
                    Log.d(TAG, "Removing Addon download with id " + keyForAddonDownload);
                    OtaUpdates.removeAddonDownload(keyForAddonDownload);
                    AddonActivity.AddonsArrayAdapter.updateButtons(keyForAddonDownload, true);
                    return;
                }
            } else {
                if (DEBUGGING)
                    Log.v(TAG, "Receiving " + mRomDownloadID);

                if (id != mRomDownloadID) {
                    if (DEBUGGING)
                        Log.v(TAG, "Ignoring unrelated non-ROM download " + id);
                    return;
                }

                Intent send = new Intent(DOWNLOAD_ROM_COMPLETE);
                context.sendBroadcast(send);

                DownloadManager downloadManager = (DownloadManager) context.getSystemService
                        (Context.DOWNLOAD_SERVICE);
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(id);
                Cursor cursor = downloadManager.query(query);

                // it shouldn't be empty, but just in case
                if (!cursor.moveToFirst()) {
                    if (DEBUGGING)
                        Log.e(TAG, "Rom download Empty row");
                    return;
                }

                int statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);

                if (DownloadManager.STATUS_SUCCESSFUL != cursor.getInt(statusIndex)) {
                    if (DEBUGGING)
                        Log.w(TAG, "Download Failed");
                    Preferences.setDownloadFinished(context, false);
                    return;
                } else {
                    if (DEBUGGING)
                        Log.v(TAG, "Download Succeeded");
                    Preferences.setDownloadFinished(context, true);
                    return;
                }
            }
        }

        if (action.equals(DownloadManager.ACTION_NOTIFICATION_CLICKED)) {

            long[] ids = extras.getLongArray(DownloadManager.EXTRA_NOTIFICATION_CLICK_DOWNLOAD_IDS);

            assert ids != null;
            for (long id : ids) {
                if (id != mRomDownloadID) {
                    if (DEBUGGING)
                        Log.v(TAG, "mDownloadID is " + mRomDownloadID + " and ID is " + id);
                    return;
                } else {
                    Intent i = new Intent(context, AvailableActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(i);
                }
            }
        }

        if (action.equals(MANIFEST_CHECK_BACKGROUND)) {
            if (DEBUGGING)
                Log.d(TAG, "Receiving background check confirmation");

            boolean updateAvailable = RomUpdate.getUpdateAvailability(context);
            boolean updateIgnored = Utils.isUpdateIgnored(context);
            String relversion = RomUpdate.getReleaseVersion(context);
            String relvariant = RomUpdate.getReleaseVariant(context);

            if (updateAvailable && !updateIgnored) {
                Utils.setupNotification(context, relversion, relvariant);
            }
        }

        if (action.equals(START_UPDATE_CHECK)) {
            if (DEBUGGING)
                Log.d(TAG, "Update check started");
            new LoadUpdateManifest(context, false).execute();
        }

        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            if (DEBUGGING) {
                Log.d(TAG, "Boot received");
            }
            boolean backgroundCheck = Preferences.getBackgroundService(context);
            if (backgroundCheck) {
                if (DEBUGGING)
                    Log.d(TAG, "Starting background check alarm");
                Utils.setupJobScheduler(context, !Preferences.getBackgroundService(context));
            }
        }

        if (action.equals(IGNORE_RELEASE)) {
            if (DEBUGGING) {
                Log.d(TAG, "Ignore release");
            }
            Preferences.setIgnoredRelease(context, Integer.toString(RomUpdate.getVersionNumber
                    (context)));
            final NotificationManager mNotifyManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            Builder mBuilder = new NotificationCompat.Builder(context);
            mBuilder.setContentTitle(context.getString(R.string.main_release_ignored))
                    .setSmallIcon(R.drawable.ic_notif)
                    .setContentIntent(PendingIntent.getActivity(context, 0, new Intent(), 0));
            mNotifyManager.notify(NOTIFICATION_ID, mBuilder.build());

            Handler h = new Handler();
            long delayInMilliseconds = 1500;
            h.postDelayed(() -> mNotifyManager.cancel(NOTIFICATION_ID), delayInMilliseconds);
        }
    }
}