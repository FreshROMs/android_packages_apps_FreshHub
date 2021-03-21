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

package de.dlyt.yanndroid.freshapp.download;

import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.os.AsyncTask;
import android.util.Log;

import java.util.Arrays;

import de.dlyt.yanndroid.freshapp.activities.AvailableActivity;
import de.dlyt.yanndroid.freshapp.activities.MainActivity;
import de.dlyt.yanndroid.freshapp.utils.Constants;
import de.dlyt.yanndroid.freshapp.utils.Preferences;

public class DownloadRomProgress extends AsyncTask<Long, Integer, Void> implements Constants {

    public final String TAG = this.getClass().getSimpleName();

    private Context mContext;
    private DownloadManager mDownloadManager;

    public DownloadRomProgress(Context context, DownloadManager downloadManager) {
        mContext = context;
        mDownloadManager = downloadManager;
    }

    @Override
    protected Void doInBackground(Long... params) {
        int previousValue = 0;
        while (Preferences.getIsDownloadOnGoing(mContext)) {
            long mDownloadID = Preferences.getDownloadID(mContext);
            DownloadManager.Query q = new DownloadManager.Query();
            q.setFilterById(mDownloadID);

            Cursor cursor = mDownloadManager.query(q);
            cursor.moveToFirst();
            try {
                final int bytesDownloaded = cursor.getInt(cursor
                        .getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                final int bytesInTotal = cursor.getInt(cursor
                        .getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));

                if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) ==
                        DownloadManager.STATUS_SUCCESSFUL) {
                    Preferences.setIsDownloadRunning(mContext, false);
                }

                final int progressPercent = (int) ((bytesDownloaded * 100L) / bytesInTotal);

                if (progressPercent != previousValue) {
                    // Only publish every 1%, to reduce the amount of work being done.
                    publishProgress(progressPercent, bytesDownloaded, bytesInTotal);
                    previousValue = progressPercent;
                }
            } catch (CursorIndexOutOfBoundsException e) {
                Preferences.setIsDownloadRunning(mContext, false);
            } catch (ArithmeticException e) {
                Preferences.setIsDownloadRunning(mContext, false);
                Log.e(TAG, Arrays.toString(e.getStackTrace()));
            }
            cursor.close();
        }
        return null;
    }

    protected void onProgressUpdate(Integer... progress) {
        if (DEBUGGING)
            Log.d(TAG, "Updating Progress - " + progress[0] + "%");
        if (Preferences.getIsDownloadOnGoing(mContext)) {
            AvailableActivity.updateProgress(progress[0], progress[1], progress[2]);
            MainActivity.updateProgress(progress[0]);
        }
    }
}