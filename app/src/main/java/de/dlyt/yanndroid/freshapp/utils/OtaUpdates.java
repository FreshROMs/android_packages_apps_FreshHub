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

import androidx.collection.ArrayMap;

import java.util.Set;

public class OtaUpdates extends Application {
    private static ArrayMap<Integer, Long> mAddonsDownloads = new ArrayMap<Integer, Long>();

    public static void putAddonDownload(int key, long value) {
        mAddonsDownloads.put(key, value);
    }

    public static long getAddonDownload(int key) {
        return mAddonsDownloads.get(key);
    }

    public static void removeAddonDownload(int key) {
        mAddonsDownloads.remove(key);
    }

    public static Set<Integer> getAddonDownloadKeySet() {
        return mAddonsDownloads.keySet();
    }
}