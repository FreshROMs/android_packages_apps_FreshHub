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

package de.dlyt.yanndroid.freshapp.utils;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.text.format.DateFormat;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationCompat.Builder;
import androidx.core.app.TaskStackBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import de.dlyt.yanndroid.freshapp.R;
import de.dlyt.yanndroid.freshapp.activities.AvailableActivity;
import de.dlyt.yanndroid.freshapp.activities.MainActivity;
import de.dlyt.yanndroid.freshapp.receivers.AppReceiver;

public class Utils implements Constants {

    public final static String TAG = "Utils";

    private static final int KILOBYTE = 1024;
    private static int KB = KILOBYTE;
    private static int MB = KB * KB;
    private static int GB = MB * KB;

    private static DecimalFormat decimalFormat = new DecimalFormat("##0.#");

    static {
        decimalFormat.setMaximumIntegerDigits(3);
        decimalFormat.setMaximumFractionDigits(1);
    }

    public static Boolean doesPropExist(String propName) {
        boolean valid = false;

        try {
            Process process = Runtime.getRuntime().exec("getprop");
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains("[" + propName + "]")) {
                    valid = true;
                }
            }
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return valid;
    }

    public static String getProp(String propName) {
        Process p;
        String result = "";
        try {
            p = new ProcessBuilder("/system/bin/getprop", propName).redirectErrorStream(true)
                    .start();
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                result = line;
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String formatDataFromBytes(long size) {

        String symbol;
        KB = KILOBYTE;
        symbol = "B";
        if (size < KB) {
            return decimalFormat.format(size) + symbol;
        } else if (size < MB) {
            return decimalFormat.format(size / (float) KB) + 'k' + symbol;
        } else if (size < GB) {
            return decimalFormat.format(size / (float) MB) + 'M' + symbol;
        }
        return decimalFormat.format(size / (float) GB) + 'G' + symbol;
    }

    public static void deleteFile(File file) {
        Tools.shell("rm -f " + file.getAbsolutePath(), false);
    }

    public static void setHasFileDownloaded(Context context) {
        File file = RomUpdate.getFullFile(context);
        int filesize = RomUpdate.getFileSize(context);
        boolean downloadIsRunning = Preferences.getIsDownloadOnGoing(context);

        boolean status = false;
        if (DEBUGGING) {
            Log.d(TAG, "Local file " + file.getAbsolutePath());
            Log.d(TAG, "Local filesize " + file.length());
            Log.d(TAG, "Remote filesize " + filesize);
        }
        if (file.length() != 0 && file.length() == filesize && !downloadIsRunning) {
            status = true;
        }
        Preferences.setDownloadFinished(context, status);
    }

    public static void setBackgroundCheck(Context context, boolean set) {
        scheduleNotification(context, !set);
    }

    public static void scheduleNotification(Context context, boolean cancel) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AppReceiver.class);
        intent.setAction(START_UPDATE_CHECK);
        int intentId = 1673;
        int intentFlag = PendingIntent.FLAG_UPDATE_CURRENT;

        if (cancel) {
            if (alarmManager != null) {
                if (DEBUGGING) Log.d(TAG, "Cancelling alarm");
                alarmManager.cancel(PendingIntent.getBroadcast(context, intentId, intent, intentFlag));
            }
        } else {
            int requestedInterval;

            if (DEBUG_NOTIFICATIONS) {
                requestedInterval = 30000;
            } else {
                requestedInterval = Preferences.getBackgroundFrequency(context);
            }

            if (DEBUGGING) Log.d(TAG, "Setting alarm for " + requestedInterval + " seconds");
            Calendar calendar = Calendar.getInstance();
            long time = calendar.getTimeInMillis() + requestedInterval * 1000;
            alarmManager.set(AlarmManager.RTC_WAKEUP, time, PendingIntent.getBroadcast(context, intentId, intent, intentFlag));
        }
    }


    public static boolean isConnected(Context context) {
        boolean isConnected = false;
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            if (activeNetwork != null) {
                isConnected = activeNetwork.isConnectedOrConnecting();
            }
        }
        return isConnected;
    }

    public static boolean isMobileNetwork(Context context) {
        boolean isMobileNetwork = false;
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            if (activeNetwork != null) {
                isMobileNetwork = activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE;
            }
        }
        return isMobileNetwork;
    }

    private static boolean versionBiggerThan(String current, String manifest) {
        // returns true if current > manifest, false otherwise
        if (current.length() > manifest.length()) {
            for (int i = 0; i < current.length() - manifest.length(); i++) {
                manifest += "0";
            }
        } else if (manifest.length() > current.length()) {
            for (int i = 0; i < manifest.length() - current.length(); i++) {
                current += "0";
            }
        }

        if (DEBUGGING) Log.d(TAG, "Current: " + current + " Manifest: " + manifest);

        return Integer.parseInt(current) < Integer.parseInt(manifest);
    }

    public static boolean isUpdateIgnored(Context context) {
        String manifestVer = Integer.toString(RomUpdate.getVersionNumber(context));
        return Preferences.getIgnoredRelease(context).matches(manifestVer);
    }

    public static String getDeviceCodename() {
        String codename = Build.DEVICE;
        return codename;
    }

    public static String getDeviceProduct() {
        String product = Build.PRODUCT;
        return product;
    }

    public static String renderAndroidSpl(String level) {
        if (!"".equals(level)) {
            try {
                SimpleDateFormat template = new SimpleDateFormat("yyyy-MM-dd");
                Date patchDate = template.parse(level);
                String format = DateFormat.getBestDateTimePattern(Locale.getDefault(), "dMMMMyyyy");
                level = DateFormat.format(format, patchDate).toString();
            } catch (ParseException e) {
                // broken parse; fall through and use the raw string
            }
            return level;
        } else {
            return null;
        }
    }

    public static void setUpdateAvailability(Context context) {
        // Grab the data from the device and manifest
        int otaVersion = RomUpdate.getVersionNumber(context);
        String currentVer = Utils.getProp("ro.fresh.ota.version");
        String manifestVer = Integer.toString(otaVersion);

        boolean available;
        available = !Preferences.getIgnoredRelease(context).matches(manifestVer) && (DEBUG_NOTIFICATIONS || versionBiggerThan(currentVer, manifestVer));

        RomUpdate.setUpdateAvailable(context, available);
        if (DEBUGGING)
            Log.d(TAG, "Update Availability is " + available);
    }

    public static void setupNotification(Context context, String filename) {
        if (DEBUGGING) Log.d(TAG, "Showing notification");

        CharSequence name = context.getString(R.string.update);
        String description = context.getString(R.string.fresh_updates);
        String CHANNEL_ID = context.getString(R.string.fresh_updates);
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);

        NotificationManager mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        mNotifyManager.createNotificationChannel(channel);

        Builder mBuilder = new NotificationCompat.Builder(context);
        Intent resultIntent = new Intent(context, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent skipIntent = new Intent(context, AppReceiver.class);
        skipIntent.setAction(IGNORE_RELEASE);
        Intent downloadIntent = new Intent(context, AvailableActivity.class);
        PendingIntent skipPendingIntent = PendingIntent.getBroadcast(context, 0, skipIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent downloadPendingIntent = PendingIntent.getActivity(context, 0, downloadIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentTitle(context.getString(R.string.update_available))
                .setContentText(filename)
                .setSmallIcon(R.drawable.ic_notif)
                .setContentIntent(resultPendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_LIGHTS)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSound(Uri.parse(Preferences.getNotificationSound(context)))
                .addAction(R.drawable.ic_action_download, context.getString(R.string.download),
                        downloadPendingIntent)
                .addAction(R.drawable.ic_close, context.getString(R.string.ignore),
                        skipPendingIntent);

        if (Preferences.getNotificationVibrate(context)) {
            mBuilder.setDefaults(NotificationCompat.DEFAULT_VIBRATE);
        }

        mNotifyManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}