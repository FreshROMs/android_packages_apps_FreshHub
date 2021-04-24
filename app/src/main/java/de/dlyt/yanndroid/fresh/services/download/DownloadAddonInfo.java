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

package de.dlyt.yanndroid.fresh.services.download;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.File;

import de.dlyt.yanndroid.fresh.Constants;
import de.dlyt.yanndroid.fresh.database.TnsAddonDownload;

public class DownloadAddonInfo implements Constants {

    public final static String TAG = "DownloadAddon";

    public void startDownload(Context context, String url, String fileName, int id, int versionNumber, int oldVersion) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));

        request.setTitle(fileName);

        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        String newFileName = fileName + "_" + versionNumber + ".zip";
        String oldFileName = fileName + "_" + oldVersion + ".zip";

        File file = new File(context.getExternalFilesDir(OTA_DIR_ADDONS), oldFileName);

        if (file.exists()) {
            boolean deleted = file.delete();
            if (!deleted) Log.e(TAG, "Unable to delete file...");
        }

        // Because of Scoped Storage, we can only download into public directories.
        request.setDestinationInExternalFilesDir(context, OTA_DIR_ADDONS, newFileName);

        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context
                .DOWNLOAD_SERVICE);
        long mDownloadID = downloadManager.enqueue(request);
        TnsAddonDownload.putAddonDownload(context, id, mDownloadID);
        new DownloadAddonInfoProgress(context, downloadManager, id, mDownloadID);
        if (DEBUGGING) {
            Log.d(TAG, "Starting download with manager ID " + mDownloadID + " and item id of " + id);
        }
    }

    public void cancelDownload(Context context, int id) {
        long mDownloadID = TnsAddonDownload.getAddonDownload(context, id);
        if (DEBUGGING) {
            Log.d(TAG,
                    "Stopping download with manager ID " + mDownloadID + " and item id of " + id);
        }
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context
                .DOWNLOAD_SERVICE);
        downloadManager.remove(mDownloadID);
        TnsAddonDownload.removeAddonDownload(context, id, TnsAddonDownload.getAddonDownload(context, id));
    }
}
