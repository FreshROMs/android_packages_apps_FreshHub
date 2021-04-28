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
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.util.Log;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.dlyt.yanndroid.fresh.Constants;
import de.dlyt.yanndroid.fresh.database.TnsOtaDownload;
import de.dlyt.yanndroid.fresh.hub.AvailableActivity;
import de.dlyt.yanndroid.fresh.hub.MainActivity;
import de.dlyt.yanndroid.fresh.utils.Notifications;

public class DownloadRomProgress implements Constants {

    private static final long UPDATE_DELAY = 500;
    private static long mStartTime;
    public final String TAG = this.getClass().getSimpleName();

    public DownloadRomProgress(Context context, DownloadManager downloadManager) {
        mStartTime = System.currentTimeMillis();
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            while (TnsOtaDownload.getIsDownloadOnGoing(context)) {
                long mDownloadID = TnsOtaDownload.getDownloadID(context);
                DownloadManager.Query q = new DownloadManager.Query().setFilterById(mDownloadID);
                Cursor cursor = downloadManager.query(q);
                cursor.moveToFirst();
                try {
                    final int bytesDownloaded = cursor.getInt(cursor
                            .getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                    final int bytesInTotal = cursor.getInt(cursor
                            .getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));

                    switch (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))) {
                        case DownloadManager.STATUS_SUCCESSFUL: {
                            TnsOtaDownload.setIsDownloadRunning(context, false);
                            TnsOtaDownload.setDownloadFinished(context, true);

                            Notifications.sendPreUpdate(context);

                            Intent send = new Intent(DOWNLOAD_ROM_COMPLETE);
                            context.sendBroadcast(send);
                            break;
                        }
                        case DownloadManager.STATUS_FAILED:
                        case DownloadManager.STATUS_PAUSED: {
                            TnsOtaDownload.setIsDownloadRunning(context, false);
                            TnsOtaDownload.setDownloadFinished(context, false);

                            Intent send = new Intent(DOWNLOAD_ROM_COMPLETE);
                            context.sendBroadcast(send);
                            break;
                        }
                    }

                    long currentTime = System.currentTimeMillis();

                    if ((currentTime - mStartTime > UPDATE_DELAY)) {
                        final int progressPercent = (int) ((bytesDownloaded * 100L) / bytesInTotal);

                        AvailableActivity.runOnUI(new Runnable() {
                            @Override
                            public void run() {
                                AvailableActivity.updateProgress(context, progressPercent, bytesDownloaded, bytesInTotal);
                            }
                        });

                        MainActivity.runOnUI(new Runnable() {
                            @Override
                            public void run() {
                                MainActivity.updateProgress(progressPercent);
                            }
                        });

                        mStartTime = currentTime;
                    }
                } catch (CursorIndexOutOfBoundsException e) {
                    TnsOtaDownload.setIsDownloadRunning(context, false);
                } catch (ArithmeticException e) {
                    TnsOtaDownload.setIsDownloadRunning(context, false);
                    Log.e(TAG, Arrays.toString(e.getStackTrace()));
                }
                cursor.close();
            }
        });
    }
}