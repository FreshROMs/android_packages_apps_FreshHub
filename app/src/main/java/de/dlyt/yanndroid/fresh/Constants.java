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

package de.dlyt.yanndroid.fresh;

public interface Constants {
    // Developer
    boolean DEBUGGING = false;
    boolean DEBUG_NOTIFICATIONS = false;

    // Storage
    String OTA_DIR_ADDONS = "Addons";
    String OTA_DIR_ROM = "Updates";

    String LAST_CHECKED = "updater_last_update_check";
    String IS_DOWNLOAD_FINISHED = "is_download_finished";
    String MD5_PASSED = "md5_passed";
    String MD5_RUN = "md5_run";
    String DOWNLOAD_RUNNING = "download_running";
    String DOWNLOAD_ID = "download_id";
    String UPDATER_BACK_SERVICE = "background_service";
    String IS_DEVICE_UPDATING = "is_device_updating";
    String UPDATER_AUTO_DOWNLOAD_SERVICE = "background_download";
    String UPDATER_BACK_FREQ = "background_frequency";
    String UPDATER_BACK_FREQ_OPTION = "background_frequency_option";
    String FIRST_RUN = "first_run";
    String APP_ICON_ENABLED = "app_icon_enabled";
    String IS_USING_SERVICE_MIRROR = "service_mirror";

    // Broadcast intents
    String WALLPAPER_CHANGED = "android.intent.action.WALLPAPER_CHANGED";
    String MANIFEST_LOADED = "com.ota.update.MANIFEST_LOADED";
    String DOWNLOAD_ROM_COMPLETE = "io.tensevntysevn.TnsOta.DOWNLOAD_ROM_COMPLETE";
    String DOWNLOAD_ADDON_DONE = "io.tensevntysevn.TnsOta.DOWNLOAD_ADDON_DONE";
    String MANIFEST_CHECK_BACKGROUND = "com.ota.update.MANIFEST_CHECK_BACKGROUND";
    String START_UPDATE_CHECK = "com.ota.update.START_UPDATE_CHECK";

    //Notification
    int NOTIFICATION_ONGOING_ID = 10771;
    int NOTIFICATION_POST_UPDATE_ID = 10772;
    int NOTIFICATION_ID = 10773;
}