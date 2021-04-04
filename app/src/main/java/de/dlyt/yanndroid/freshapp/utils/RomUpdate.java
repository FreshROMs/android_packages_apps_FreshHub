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

import java.io.File;

public class RomUpdate implements Constants {

    private static final String PREF_NAME = "ROMUpdate";
    private static String VERSION_NAME = "rom_version_name";
    private static String VERSION_NUMBER = "rom_version_number";
    private static String DIRECT_URL = "rom_direct_url";
    private static String HTTP_URL = "rom_http_url";
    private static String MD5 = "rom_md5";
    private static String WEBSITE = "rom_website";
    private static String DEVELOPER = "rom_developer";
    private static String CHANGELOG = "rom_changelog";
    private static String DONATE_LINK = "rom_donate_link";
    private static String BTC_LINK = "rom_bitcoin_link";
    private static String FILESIZE = "rom_filesize";
    private static String AVAILABILITY = "update_availability";
    private static String SPONSORED_ROMHUT = "rom_sponsored_romhut";
    private static String ADDONS_COUNT = "rom_addons_count";
    private static String ADDONS_URL = "rom_addons_url";
    private static String DEF_VALUE = "null";
    public final String TAG = this.getClass().getSimpleName();

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    private static String getVersionName(Context context) {
        return getPrefs(context).getString(VERSION_NAME, DEF_VALUE);
    }

    public static int getVersionNumber(Context context) {
        return getPrefs(context).getInt(VERSION_NUMBER, 0);
    }

    public static String getDirectUrl(Context context) {
        return getPrefs(context).getString(DIRECT_URL, DEF_VALUE);
    }

    public static String getHttpUrl(Context context) {
        return getPrefs(context).getString(HTTP_URL, DEF_VALUE);
    }

    public static String getMd5(Context context) {
        return getPrefs(context).getString(MD5, DEF_VALUE);
    }

    public static String getChangelog(Context context) {
        return getPrefs(context).getString(CHANGELOG, DEF_VALUE);
    }

    public static String getWebsite(Context context) {
        return getPrefs(context).getString(WEBSITE, DEF_VALUE);
    }

    public static String getDeveloper(Context context) {
        return getPrefs(context).getString(DEVELOPER, DEF_VALUE);
    }

    public static String getDonateLink(Context context) {
        return getPrefs(context).getString(DONATE_LINK, DEF_VALUE);
    }

    public static String getBitCoinLink(Context context) {
        return getPrefs(context).getString(BTC_LINK, DEF_VALUE);
    }

    public static int getFileSize(Context context) {
        return getPrefs(context).getInt(FILESIZE, 0);
    }

    public static int getAddonsCount(Context context) {
        return getPrefs(context).getInt(ADDONS_COUNT, 0);
    }

    public static String getAddonsUrl(Context context) {
        return getPrefs(context).getString(ADDONS_URL, DEF_VALUE);
    }

    public static String getRomHut(Context context) {
        return getPrefs(context).getString(SPONSORED_ROMHUT, DEF_VALUE);
    }

    public static boolean getUpdateAvailability(Context context) {
        return getPrefs(context).getBoolean(AVAILABILITY, false);
    }

    public static void setRomName(Context context, String name) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        String NAME = "rom_name";
        editor.putString(NAME, name);
        editor.apply();
    }

    public static void setVersionName(Context context, String version) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putString(VERSION_NAME, version);
        editor.apply();
    }

    public static void setVersionNumber(Context context, int version) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putInt(VERSION_NUMBER, version);
        editor.apply();
    }

    public static void setDirectUrl(Context context, String url) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putString(DIRECT_URL, url);
        editor.apply();
    }

    public static void setHttpUrl(Context context, String url) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putString(HTTP_URL, url);
        editor.apply();
    }

    public static void setMd5(Context context, String md5) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putString(MD5, md5);
        editor.apply();
    }

    public static void setChangelog(Context context, String change) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        String CHANGELOG = "rom_changelog";
        editor.putString(CHANGELOG, change);
        editor.apply();
    }

    public static void setAndroidVersion(Context context, String android) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        String ANDROID = "rom_android_ver";
        editor.putString(ANDROID, android);
        editor.apply();
    }

    public static void setWebsite(Context context, String website) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putString(WEBSITE, website);
        editor.apply();
    }

    public static void setDeveloper(Context context, String developer) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putString(DEVELOPER, developer);
        editor.apply();
    }

    public static void setDonateLink(Context context, String donateLink) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putString(DONATE_LINK, donateLink);
        editor.apply();
    }

    public static void setBitCoinLink(Context context, String donateLink) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putString(BTC_LINK, donateLink);
        editor.apply();
    }

    public static void setFileSize(Context context, int size) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putInt(FILESIZE, size);
        editor.apply();
    }

    public static void setRomHut(Context context, String romhut_text) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putString(SPONSORED_ROMHUT, romhut_text);
        editor.apply();
    }

    public static void setAddonsCount(Context context, int addons_count) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putInt(ADDONS_COUNT, addons_count);
        editor.apply();
    }

    public static void setAddonsUrl(Context context, String addons_url) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putString(ADDONS_URL, addons_url);
        editor.apply();
    }

    public static void setUpdateAvailable(Context context, boolean availability) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putBoolean(AVAILABILITY, availability);
        editor.apply();
    }

    public static String getFilename(Context context) {

        String result = getVersionName(context);

        return result.replace(" ", "");
    }

    public static File getFullFile(Context context) {
        return new File(SD_CARD
                + File.separator
                + OTA_DOWNLOAD_DIR
                + File.separator
                + RomUpdate.getFilename(context)
                + ".zip");
    }
}
