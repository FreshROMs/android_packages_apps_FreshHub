package de.dlyt.yanndroid.fresh.database;

import android.content.Context;
import android.content.SharedPreferences;

import de.dlyt.yanndroid.fresh.Constants;

public class TnsOtaDownload implements Constants {

    public static final String TAG = "TnsOtaDownload";
    public static String PREF_NAME = "tns_ota_download_db";

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static boolean getDownloadFinished(Context context) {
        return getPrefs(context).getBoolean(IS_DOWNLOAD_FINISHED, false);
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

    public static long getDownloadID(Context context) {
        return getPrefs(context).getLong(DOWNLOAD_ID, 0L);
    }

    public static boolean getIsDeviceUpdating(Context context) {
        return getPrefs(context).getBoolean(IS_DEVICE_UPDATING, false);
    }

    public static long getUpdateLastChecked(Context context) {
        return getPrefs(context).getLong(LAST_CHECKED, System.currentTimeMillis());
    }

    public static void setDownloadFinished(Context context, boolean value) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putBoolean(IS_DOWNLOAD_FINISHED, value);
        editor.apply();
    }

    public static void setIsDeviceUpdating(Context context, boolean value) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putBoolean(IS_DEVICE_UPDATING, value);
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

    public static void setUpdateLastChecked(Context context, Long time) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putLong(LAST_CHECKED, time);
        editor.apply();
    }
}
