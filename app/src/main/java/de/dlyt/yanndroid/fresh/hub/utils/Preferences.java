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

package de.dlyt.yanndroid.fresh.hub.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.util.Log;

import de.dlyt.yanndroid.fresh.Constants;
import de.dlyt.yanndroid.fresh.hub.SplashActivity;

public class Preferences implements Constants {

    public static final String TAG = "Preferences";
    public static String PREF_NAME = "hub_settings";

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static boolean getBackgroundService(Context context) {
        if (DEBUGGING)
            Log.d(TAG, "Background Service set to " + getPrefs(context).getBoolean
                    (UPDATER_BACK_SERVICE, true));
        return getPrefs(context).getBoolean(UPDATER_BACK_SERVICE, true);
    }

    public static boolean getAppIconState(Context context) {
        int appIconEnabled = Settings.System.getInt(context.getContentResolver(),
                APP_ICON_ENABLED, 1);

        if (DEBUGGING)
            Log.d(TAG, "Background Service set to " + (appIconEnabled == 1));

        return appIconEnabled == 1;
    }

    public static boolean getBackgroundDownload(Context context) {
        if (DEBUGGING)
            Log.d(TAG, "Background Download set to " + getPrefs(context).getBoolean
                    (UPDATER_AUTO_DOWNLOAD_SERVICE, true));
        return getPrefs(context).getBoolean(UPDATER_AUTO_DOWNLOAD_SERVICE, false);
    }

    public static boolean getUsingMirrorService(Context context) {
        return getPrefs(context).getBoolean(IS_USING_SERVICE_MIRROR, false);
    }

    public static int getBackgroundFrequency(Context context) {
        return Integer.parseInt(getPrefs(context).getString(UPDATER_BACK_FREQ, "43200"));
    }

    public static int getBackgroundFrequencyOption(Context context) {
        return getPrefs(context).getInt(UPDATER_BACK_FREQ_OPTION, 3);
    }

    public static Boolean getFirstRun(Context context) {
        return getPrefs(context).getBoolean(FIRST_RUN, true);
    }

    public static void setBackgroundService(Context context, Boolean toggle) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putBoolean(UPDATER_BACK_SERVICE, toggle);
        editor.apply();
    }

    public static void setAppIconState(Context context, Boolean toggle) {
        Settings.System.putInt(context.getContentResolver(), APP_ICON_ENABLED, toggle ? 1 : 0);
    }

    public static void setBackgroundDownload(Context context, Boolean toggle) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putBoolean(UPDATER_AUTO_DOWNLOAD_SERVICE, toggle);
        editor.apply();
    }

    public static void setUsingMirrorService(Context context, Boolean toggle) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putBoolean(IS_USING_SERVICE_MIRROR, toggle);
        editor.apply();
    }

    public static void setBackgroundFrequency(Context context, String time) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putString(UPDATER_BACK_FREQ, time);
        editor.apply();
    }

    public static void setBackgroundFrequencyOption(Context context, Integer selection) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putInt(UPDATER_BACK_FREQ_OPTION, selection);
        editor.apply();
    }

    public static int getDataConnectionIconInt(Context context) {
        return Settings.System.getInt(context.getContentResolver(), "zest_data_icon_int", 0);
    }

    public static void setDataConnectionIconInt(Context context, Integer selection) {
        Settings.System.putInt(context.getContentResolver(), "zest_data_icon_int", selection);
    }

    public static int getWlanConnectionIconInt(Context context) {
        return Settings.System.getInt(context.getContentResolver(), "zest_wlan_icon_int", 0);
    }

    public static void setWlanConnectionIconInt(Context context, Integer selection) {
        Settings.System.putInt(context.getContentResolver(), "zest_wlan_icon_int", selection);
    }
    
    public static void setFirstRun(Context context, boolean value) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putBoolean(FIRST_RUN, value);
        editor.apply();
    }

    public static void toggleAppIcon(Context context, boolean enabled) {
        PackageManager packageManager = context.getPackageManager();
        ComponentName launcherActivity = new ComponentName(context, SplashActivity.class);
        packageManager.setComponentEnabledSetting(launcherActivity,
                enabled ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }
}