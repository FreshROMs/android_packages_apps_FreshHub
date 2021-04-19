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

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class AddonDownloadDB extends Application {
    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences("AddonDownloadDB", Context.MODE_PRIVATE);
    }

    public static void putAddonDownload(Context context, int key, long value) {
        String stringId = Integer.toString(key);
        checkForNullKey(stringId);
        putAddonDownloading(context, key);
        getPrefs(context).edit().putLong("downloadId_"+stringId, value).apply();
    }

    public static void putAddonDownloading(Context context, int key) {
        String stringId = Integer.toString(key);
        checkForNullKey(stringId);
        getPrefs(context).edit().putBoolean("downloading_"+stringId, true).apply();
    }

    public static long getAddonDownload(Context context, int key) {
        String stringId = Integer.toString(key);
        return getPrefs(context).getLong("downloadId_"+stringId, 0);
    }

    public static Boolean getAddonDownloading(Context context, int key) {
        String stringId = Integer.toString(key);
        return getPrefs(context).getBoolean("downloading_"+stringId, false);
    }

    public static void removeAddonDownload(Context context, int key) {
        String stringId = Integer.toString(key);
        removeAddonDownloading(context, key);
        getPrefs(context).edit().remove("downloadId_"+stringId).apply();
    }

    public static void removeAddonDownloading(Context context, int key) {
        String stringId = Integer.toString(key);
        getPrefs(context).edit().remove("downloading_"+stringId).apply();
    }

    private static void checkForNullKey(String key){
        if (key == null){
            throw new NullPointerException();
        }
    }
}