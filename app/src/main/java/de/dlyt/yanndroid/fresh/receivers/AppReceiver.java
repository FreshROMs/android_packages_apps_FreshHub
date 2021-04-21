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

package de.dlyt.yanndroid.fresh.receivers;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

import java.io.File;

import de.dlyt.yanndroid.fresh.database.TnsAddonDownload;
import de.dlyt.yanndroid.fresh.database.TnsOtaDownload;
import de.dlyt.yanndroid.fresh.utils.JobScheduler;
import de.dlyt.yanndroid.fresh.utils.Notifications;
import de.dlyt.yanndroid.fresh.hub.AvailableActivity;
import de.dlyt.yanndroid.fresh.services.TnsOtaApiService;
import de.dlyt.yanndroid.fresh.Constants;
import de.dlyt.yanndroid.fresh.hub.utils.Preferences;
import de.dlyt.yanndroid.fresh.database.TnsOta;

public class AppReceiver extends BroadcastReceiver implements Constants {

    public final String TAG = this.getClass().getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Bundle extras = intent.getExtras();
        long mRomDownloadID = TnsOtaDownload.getDownloadID(context);

        if (action.equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
            long id = extras.getLong(DownloadManager.EXTRA_DOWNLOAD_ID);
            boolean isAddonDownload = !(TnsAddonDownload.getAddonDownloadId(context, id) == 0);

            if (isAddonDownload) {
                int addonId = TnsAddonDownload.getAddonDownloadId(context, id);
                TnsAddonDownload.removeAddonDownloading(context, addonId);
                TnsAddonDownload.removeAddonDownloadId(context, addonId);
                TnsAddonDownload.removeAddonDownload(context, addonId, id);

                Intent send = new Intent(DOWNLOAD_ADDON_DONE);
                context.sendBroadcast(send);

                return;
            } else {
                if (DEBUGGING)
                    Log.v(TAG, "Receiving " + mRomDownloadID);

                if (id != mRomDownloadID) {
                    if (DEBUGGING)
                        Log.v(TAG, "Ignoring unrelated non-ROM download " + id);
                    return;
                }

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

                if (!(cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) ==
                        DownloadManager.STATUS_SUCCESSFUL)) {
                    if (DEBUGGING)
                        Log.w(TAG, "Download Failed");
                    TnsOtaDownload.setDownloadFinished(context, false);
                } else {
                    if (DEBUGGING)
                        Log.v(TAG, "Download Succeeded");
                    TnsOtaDownload.setDownloadFinished(context, true);
                }

                return;
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

            boolean updateAvailable = TnsOta.getUpdateAvailability(context);
            String relversion = TnsOta.getReleaseVersion(context);
            String relvariant = TnsOta.getReleaseVariant(context);

            if (updateAvailable) {
                Notifications.sendUpdateNotification(context, relversion, relvariant);
            }
        }

        if (action.equals(START_UPDATE_CHECK)) {
            if (DEBUGGING)
                Log.d(TAG, "Update check started");
            new TnsOtaApiService(context, false);
        }

        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            if (DEBUGGING) {
                Log.d(TAG, "Boot received");
            }
            boolean backgroundCheck = Preferences.getBackgroundService(context);
            boolean isDeviceUpdating = TnsOtaDownload.getIsDeviceUpdating(context);
            String isUninstallingAddon = TnsAddonDownload.getIsUninstallingAddon(context);

            if (isUninstallingAddon != null) {
                File file = new java.io.File(context.getExternalFilesDir(OTA_DIR_ADDONS), isUninstallingAddon+".zip");
                if (file.exists()) {
                    boolean deleted = file.delete();
                    if (!deleted) Log.e(TAG, "Unable to delete file...");
                }

                TnsAddonDownload.setIsUninstallingAddon(context, null);
            }

            TnsAddonDownload.clearDownloadDb(context);

            if (isDeviceUpdating) {
                TnsOta.setUpdateAvailability(context);
                boolean isUpdateSuccessful = !(TnsOta.getUpdateAvailability(context));
                String updateVersion = TnsOta.getReleaseVersion(context);
                Notifications.sendPostUpdateNotification(context, updateVersion, isUpdateSuccessful);

                TnsOtaDownload.setIsDeviceUpdating(context, false);

                File updateFile = TnsOta.getFullFile(context);

                if (updateFile.exists()) {
                    boolean deleted = updateFile.delete();
                    if (!deleted) Log.e(TAG, "Could not delete update file...");
                }

                if (backgroundCheck && !isDeviceUpdating) {
                    if (DEBUGGING)
                        Log.d(TAG, "Starting background check alarm");
                    new TnsOtaApiService(context, false);

                    boolean updateAvailable = TnsOta.getUpdateAvailability(context);
                    String relversion = TnsOta.getReleaseVersion(context);
                    String relvariant = TnsOta.getReleaseVariant(context);

                    if (updateAvailable) {
                        Notifications.sendUpdateNotification(context, relversion, relvariant);
                    }

                    JobScheduler.setupJobScheduler(context, !Preferences.getBackgroundService(context));
                }

            }
        }
    }
}