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
import android.os.RecoverySystem;

import java.io.File;
import java.io.IOException;
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
    private File mFilename;
    private String mScriptOutput;

    @SuppressLint("SdCardPath")
    public RecoveryInstall(Context context, Boolean isAddon, String addonName) {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            if (isAddon) {
                mFilename =  new File(context.getExternalFilesDir(OTA_DIR_ADDONS), addonName);
            } else {
                TnsOtaDownload.setIsDeviceUpdating(context, true);
                mFilename = TnsOta.getFullFile(context);
            }

            try {
                RecoverySystem.installPackage(context, mFilename);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    // For OTA Updates - since they don't have an addon name
    public RecoveryInstall(Context context, Boolean isAddon) {
        mFilename = TnsOta.getFullFile(context);
        new RecoveryInstall(context, isAddon, null);
    }
}