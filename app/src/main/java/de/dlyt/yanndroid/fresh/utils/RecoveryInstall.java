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

package de.dlyt.yanndroid.fresh.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.dlyt.yanndroid.fresh.database.TnsOtaDownload;
import de.dlyt.yanndroid.fresh.Constants;
import de.dlyt.yanndroid.fresh.database.TnsOta;

public class RecoveryInstall implements Constants {

    private static final String SCRIPT_FILE = "/cache/recovery/openrecoveryscript";
    private static final String NEW_LINE = "\n";
    public final String TAG = this.getClass().getSimpleName();
    @SuppressLint("StaticFieldLeak")
    private final StringBuilder mScript = new StringBuilder();
    private final String mFilename;
    private String mScriptOutput;

    @SuppressLint("SdCardPath")
    public RecoveryInstall(Context context, Boolean isAddon, String addonName) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        mFilename = TnsOta.getFilename(context) + ".zip";

        executor.execute(() -> {
            if (isAddon) {
                mScript.append("install \"/sdcard/Android/data/")
                        .append(context.getPackageName())
                        .append(File.separator)
                        .append("files")
                        .append(File.separator)
                        .append(OTA_DIR_ADDONS)
                        .append(File.separator)
                        .append(addonName)
                        .append("\"")
                        .append(NEW_LINE);
            } else {
                TnsOtaDownload.setIsDeviceUpdating(context, true);

                mScript.append("install /sdcard/Android/data/")
                        .append(context.getPackageName())
                        .append(File.separator)
                        .append("files")
                        .append(File.separator)
                        .append(OTA_DIR_ROM)
                        .append(File.separator)
                        .append(mFilename)
                        .append(NEW_LINE);
            }

            mScriptOutput = mScript.toString();

            // Try create a dir in the cache folder
            // Without root
            String check = Tools.shell("mkdir -p /cache/recovery/; echo $?", false);

            // If not 0, then permission was denied
            if (!check.equals("0")) {
                // Run as root
                Tools.shell("su -c mkdir -p /cache/recovery/; echo $?", true);
                Tools.shell("su -c echo \"" + mScriptOutput + "\" > " + SCRIPT_FILE + "\n", true);
            } else {
                // Permission was enabled, run without root
                Tools.shell("echo \"" + mScriptOutput + "\" > " + SCRIPT_FILE + "\n", false);
            }

            handler.postDelayed(() -> {
                Tools.rebootUpdate(context);
            }, 2000);
        });
    }

    // For OTA Updates - since they don't have an addon name
    public RecoveryInstall(Context context, Boolean isAddon) {
        mFilename = TnsOta.getFilename(context) + ".zip";
        new RecoveryInstall(context, isAddon, null);
    }
}