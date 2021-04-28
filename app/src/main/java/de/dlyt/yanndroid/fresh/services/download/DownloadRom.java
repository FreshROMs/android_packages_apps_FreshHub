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

import java.io.File;

import de.dlyt.yanndroid.fresh.Constants;
import de.dlyt.yanndroid.fresh.R;
import de.dlyt.yanndroid.fresh.database.TnsOta;
import de.dlyt.yanndroid.fresh.database.TnsOtaDownload;

public class DownloadRom implements Constants {

    public final static String TAG = "DownloadRomUpdate";

    public void startDownload(Context context) {
        String url = TnsOta.getDirectUrl(context);
        String fileName = TnsOta.getFilename(context) + ".zip";
        String otaName = context.getString(R.string.system_name) + " " + TnsOta.getReleaseVersion(context) + " "
                + TnsOta.getReleaseVariant(context) + " (" + TnsOta.getVersionNumber(context) + ")";
        File file = TnsOta.getFullFile(context);

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));

        request.setTitle(otaName);

        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);

        // Because of Scoped Storage, we can only download into public directories.
        // Directory is '/storage/emulated/0/Download/Fresh'
        request.setDestinationInExternalFilesDir(context, OTA_DIR_ROM, fileName);

        // Delete any existing files
        de.dlyt.yanndroid.fresh.utils.File.deleteFile(file);

        // Enqueue the download
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context
                .DOWNLOAD_SERVICE);
        long mDownloadID = downloadManager.enqueue(request);

        // Store the download ID
        TnsOtaDownload.setDownloadID(context, mDownloadID);

        // Set a setting indicating the download is now running
        TnsOtaDownload.setIsDownloadRunning(context, true);

        // Start updating the progress
        new DownloadRomProgress(context, downloadManager);

        // MD5 checker has not been run, nor passed
        TnsOtaDownload.setMD5Passed(context, false);
        TnsOtaDownload.setHasMD5Run(context, false);
    }

    public void cancelDownload(Context context) {
        // Grab the download ID from settings
        long mDownloadID = TnsOtaDownload.getDownloadID(context);

        // Remove the download
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context
                .DOWNLOAD_SERVICE);
        downloadManager.remove(mDownloadID);

        // Indicate that the download is no longer running
        TnsOtaDownload.setIsDownloadRunning(context, false);
    }
}