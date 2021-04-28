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

package de.dlyt.yanndroid.fresh.database;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.format.DateFormat;
import android.util.Log;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.dlyt.yanndroid.fresh.Constants;
import de.dlyt.yanndroid.fresh.R;
import de.dlyt.yanndroid.fresh.utils.SystemProperties;
import de.dlyt.yanndroid.fresh.utils.Tools;

public class TnsOta implements Constants {

    private static final String PREF_NAME = "tns_ota_update_db";
    private static final String VERSION_NAME = "rom_version_name";
    private static final String VERSION_NUMBER = "rom_version_number";
    private static final String DIRECT_URL = "rom_direct_url";
    private static final String HTTP_URL = "rom_http_url";
    private static final String MD5 = "rom_md5";
    private static final String WEBSITE = "rom_website";
    private static final String DEVELOPER = "rom_developer";
    private static final String CHANGELOG = "rom_changelog";
    private static final String DONATE_LINK = "rom_donate_link";
    private static final String BTC_LINK = "rom_bitcoin_link";
    private static final String FILESIZE = "rom_filesize";
    private static final String AVAILABILITY = "update_availability";
    private static final String ADDONS_COUNT = "rom_addons_count";
    private static final String ADDONS_URL = "rom_addons_url";

    private static final String RELEASE_TYPE = "rom_release_type";
    private static final String ANDROID_SPL = "rom_android_spl";
    private static final String ANDROID_SESL = "rom_android_sesl";
    private static final String RELEASE_STRING = "rom_release_string";
    private static final String RELEASE_VARIANT = "rom_release_variant";
    private static final String DISCORD_URL = "rom_discord_url";
    private static final String FORUM_URL = "rom_forum_url";
    private static final String GIT_ISSUES = "rom_git_issues_url";
    private static final String GIT_DISCUSSION = "rom_git_discussion_url";

    private static final String APP_VERSION = "app_update_version";
    private static final String APP_URL = "app_update_url";

    private static final String START_TIME = "ota_start_download_time";

    private static final String DEF_VALUE = "null";
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

    public static Long getStartTime(Context context) {
        return getPrefs(context).getLong(START_TIME, 0);
    }

    public static String getChangelog(Context context) {
        return getPrefs(context).getString(CHANGELOG, DEF_VALUE);
    }

    public static String getSpl(Context context) {
        return getPrefs(context).getString(ANDROID_SPL, DEF_VALUE);
    }

    public static String getSesl(Context context) {
        return getPrefs(context).getString(ANDROID_SESL, DEF_VALUE);
    }

    public static String getForum(Context context) {
        return getPrefs(context).getString(FORUM_URL, DEF_VALUE);
    }

    public static String getDiscord(Context context) {
        return getPrefs(context).getString(DISCORD_URL, DEF_VALUE);
    }

    public static String getGitIssues(Context context) {
        return getPrefs(context).getString(GIT_ISSUES, DEF_VALUE);
    }

    public static String getGitDiscussion(Context context) {
        return getPrefs(context).getString(GIT_DISCUSSION, DEF_VALUE);
    }

    public static String getReleaseVersion(Context context) {
        return getPrefs(context).getString(RELEASE_STRING, DEF_VALUE);
    }

    public static String getReleaseVariant(Context context) {
        return getPrefs(context).getString(RELEASE_VARIANT, DEF_VALUE);
    }

    public static String getReleaseType(Context context) {
        return getPrefs(context).getString(RELEASE_TYPE, DEF_VALUE);
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

    public static long getFileSize(Context context) {
        return getPrefs(context).getLong(FILESIZE, 0);
    }

    public static int getAddonsCount(Context context) {
        return getPrefs(context).getInt(ADDONS_COUNT, 0);
    }

    public static String getAddonsUrl(Context context) {
        return getPrefs(context).getString(ADDONS_URL, DEF_VALUE);
    }

    public static Integer getAppVersion(Context context) {
        return getPrefs(context).getInt(APP_VERSION, 0);
    }

    public static String getAppUrl(Context context) {
        return getPrefs(context).getString(APP_URL, DEF_VALUE);
    }

    public static boolean getUpdateAvailability(Context context) {
        return getPrefs(context).getBoolean(AVAILABILITY, false);
    }

    public static void setStartTime(Context context, Long time) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putLong(START_TIME, time);
        editor.apply();
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

    public static void setReleaseVersion(Context context, String url) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putString(RELEASE_STRING, url);
        editor.apply();
    }

    public static void setReleaseVariant(Context context, String url) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putString(RELEASE_VARIANT, url);
        editor.apply();
    }

    public static void setReleaseType(Context context, String url) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putString(RELEASE_TYPE, url);
        editor.apply();
    }

    public static void setSpl(Context context, String url) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putString(ANDROID_SPL, url);
        editor.apply();
    }

    public static void setSesl(Context context, String url) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putString(ANDROID_SESL, url);
        editor.apply();
    }

    public static void setForum(Context context, String url) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putString(FORUM_URL, url);
        editor.apply();
    }

    public static void setDiscord(Context context, String url) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putString(DISCORD_URL, url);
        editor.apply();
    }

    public static void setGitIssues(Context context, String url) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putString(GIT_ISSUES, url);
        editor.apply();
    }

    public static void setGitDiscussion(Context context, String url) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putString(GIT_DISCUSSION, url);
        editor.apply();
    }

    public static void setMd5(Context context, String md5) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putString(MD5, md5);
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

    public static void setFileSize(Context context, long size) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putLong(FILESIZE, size);
        editor.apply();
    }

    public static void setChangelog(Context context, String change) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        String CHANGELOG = "rom_changelog";
        editor.putString(CHANGELOG, change);
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

    public static void setAppVersion(Context context, int appVersion) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putInt(APP_VERSION, appVersion);
        editor.apply();
    }

    public static void setAppUrl(Context context, String appUrl) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putString(APP_URL, appUrl);
        editor.apply();
    }

    public static String getFilename(Context context) {

        String result = getVersionName(context);

        return context.getResources().getString(R.string.system_name) + "_"
                + getReleaseVersion(context) + "-" + getVersionNumber(context) + "_"
                + getReleaseVariant(context) + "_" + getReleaseType(context);
    }

    public static File getFullFile(Context context) {
        return new File(context.getExternalFilesDir(OTA_DIR_ROM),
                TnsOta.getFilename(context) + ".zip");
    }

    private static boolean versionBiggerThan(String current, String manifest) {
        // returns true if current > manifest, false otherwise
        if (current.length() > manifest.length()) {
            StringBuilder manifestBuilder = new StringBuilder(manifest);
            for (int i = 0; i < current.length() - manifestBuilder.length(); i++) {
                manifestBuilder.append("0");
            }
            manifest = manifestBuilder.toString();
        } else if (manifest.length() > current.length()) {
            StringBuilder currentBuilder = new StringBuilder(current);
            for (int i = 0; i < manifest.length() - currentBuilder.length(); i++) {
                currentBuilder.append("0");
            }
            current = currentBuilder.toString();
        }

        if (DEBUGGING) Log.d(Tools.TAG, "Current: " + current + " Manifest: " + manifest);

        return Integer.parseInt(current) < Integer.parseInt(manifest);
    }

    public static void setUpdateAvailability(Context context) {
        // Grab the data from the device and manifest
        int otaVersion = getVersionNumber(context);
        String currentVer = SystemProperties.getProp("ro.fresh.build.version");
        String manifestVer = Integer.toString(otaVersion);

        boolean available;
        available = (DEBUG_NOTIFICATIONS || versionBiggerThan(currentVer, manifestVer));

        setUpdateAvailable(context, available);
        if (DEBUGGING)
            Log.d(Tools.TAG, "Update Availability is " + available);
    }

    @SuppressLint("SimpleDateFormat")
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
}
