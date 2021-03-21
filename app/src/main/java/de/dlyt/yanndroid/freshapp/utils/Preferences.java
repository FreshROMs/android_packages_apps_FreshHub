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

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import de.dlyt.yanndroid.freshapp.R;

public class Preferences implements Constants {

    public static final String TAG = "Preferences";

    public static String PREF_NAME = "OTAUpdateSettings";

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static boolean getDownloadFinished(Context context) {
        return getPrefs(context).getBoolean(IS_DOWNLOAD_FINISHED, false);
    }

    public static boolean getDeleteAfterInstall(Context context) {
        return getPrefs(context).getBoolean(DELETE_AFTER_INSTALL, false);
    }

    public static boolean getWipeData(Context context) {
        return getPrefs(context).getBoolean(WIPE_DATA, false);
    }

    public static boolean getWipeCache(Context context) {
        return getPrefs(context).getBoolean(WIPE_CACHE, true);
    }

    public static boolean getWipeDalvik(Context context) {
        return getPrefs(context).getBoolean(WIPE_DALVIK, true);
    }

    public static boolean getMD5Passed(Context context) {
        return getPrefs(context).getBoolean(MD5_PASSED, false);
    }

    public static boolean getHasMD5Run(Context context) {
        return getPrefs(context).getBoolean(MD5_RUN, false);
    }

    public static boolean getIsDownloadOnGoing(Context context) {
        return getPrefs(context).getBoolean(DOWNLOAD_RUNNING, false);
    }

    public static String getNetworkType(Context context) {
        return getPrefs(context).getString(NETWORK_TYPE, WIFI_ONLY);
    }

    public static long getDownloadID(Context context) {
        return getPrefs(context).getLong(DOWNLOAD_ID, 0L);
    }

    static String getNotificationSound(Context context) {
        String defValue = android.provider.Settings.System.DEFAULT_NOTIFICATION_URI.toString();
        return getPrefs(context).getString(NOTIFICATIONS_SOUND, defValue);
    }

    static Boolean getNotificationVibrate(Context context) {
        return getPrefs(context).getBoolean(NOTIFICATIONS_VIBRATE, true);
    }

    public static boolean getBackgroundService(Context context) {
        if (DEBUGGING)
            Log.d(TAG, "Background Service set to " + getPrefs(context).getBoolean
                    (UPDATER_BACK_SERVICE, true));
        return getPrefs(context).getBoolean(UPDATER_BACK_SERVICE, true);
    }

    static int getBackgroundFrequency(Context context) {
        return Integer.parseInt(getPrefs(context).getString(UPDATER_BACK_FREQ, "43200"));
    }

    public static boolean getORSEnabled(Context context) {
        Log.d(TAG, "ORS Enabled Preference " + getPrefs(context).getBoolean(UPDATER_ENABLE_ORS,
                false));
        return getPrefs(context).getBoolean(UPDATER_ENABLE_ORS, false);
    }

    public static int getCurrentTheme(Context context) {
        return Integer.parseInt(getPrefs(context).getString(CURRENT_THEME, THEME_LIGHT));
    }


    public static String getIgnoredRelease(Context context) {
        return getPrefs(context).getString(IGNORE_RELEASE_VERSION, "0");
    }

    public static Boolean getFirstRun(Context context) {
        return getPrefs(context).getBoolean(FIRST_RUN, true);
    }

    public static void setUpdateLastChecked(Context context, String time) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putString(LAST_CHECKED, time);
        editor.apply();
    }

    public static void setDownloadFinished(Context context, boolean value) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putBoolean(IS_DOWNLOAD_FINISHED, value);
        editor.apply();
    }

    public static void setTheme(Context context, String value) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putString(CURRENT_THEME, value);
        editor.apply();
    }

    public static void setDeleteAfterInstall(Context context, boolean value) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putBoolean(DELETE_AFTER_INSTALL, value);
        editor.apply();
    }

    public static void setWipeData(Context context, boolean value) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putBoolean(WIPE_DATA, value);
        editor.apply();
    }

    public static void setWipeCache(Context context, boolean value) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putBoolean(WIPE_CACHE, value);
        editor.apply();
    }

    public static void setWipeDalvik(Context context, boolean value) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putBoolean(WIPE_DALVIK, value);
        editor.apply();
    }

    public static void setMD5Passed(Context context, boolean value) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putBoolean(MD5_PASSED, value);
        editor.apply();
    }

    public static void setHasMD5Run(Context context, boolean value) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putBoolean(MD5_RUN, value);
        editor.apply();
    }

    public static void setIsDownloadRunning(Context context, boolean value) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putBoolean(DOWNLOAD_RUNNING, value);
        editor.apply();
    }

    public static void setDownloadID(Context context, long value) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putLong(DOWNLOAD_ID, value);
        editor.apply();
    }

    public static void setIgnoredRelease(Context context, String value) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putString(IGNORE_RELEASE_VERSION, value);
        editor.apply();
    }

    public static void setFirstRun(Context context, boolean value) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putBoolean(FIRST_RUN, value);
        editor.apply();
    }
}