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
import de.dlyt.yanndroid.fresh.hub.AddonActivity;

public class DownloadAddon implements Constants {

    public final static String TAG = "DownloadAddon";

    public void startDownload(Context context, String url, String fileName, int id, int versionNumber) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));

        request.setTitle(fileName);

        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        String newFileName = fileName + "_" + versionNumber + ".zip";

        File folder = new File(context.getExternalFilesDir(OTA_DIR_ADDONS).getAbsolutePath());
        try {
            File[] addonFiles = folder.listFiles();

            for (File addonFile : addonFiles != null ? addonFiles : new File[0]) {
                if (addonFile.getAbsolutePath().startsWith(fileName) && addonFile.getAbsolutePath().endsWith(".zip"))
                    if (!addonFile.delete()) Log.e(TAG, "Unable to delete file...");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Because of Scoped Storage, we can only download into public directories.
        request.setDestinationInExternalFilesDir(context, OTA_DIR_ADDONS, newFileName);

        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context
                .DOWNLOAD_SERVICE);
        long mDownloadID = downloadManager.enqueue(request);
        TnsAddonDownload.putAddonDownload(context, id, mDownloadID);
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
        AddonActivity.AddonsArrayAdapter.updateProgress(id, 0, true, 0, false);
        TnsAddonDownload.removeAddonDownload(context, id, TnsAddonDownload.getAddonDownload(context, id));
    }
}
