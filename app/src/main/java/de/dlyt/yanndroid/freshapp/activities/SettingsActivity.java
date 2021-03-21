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

package de.dlyt.yanndroid.freshapp.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.preference.SwitchPreference;
import android.text.TextUtils;
import android.util.SparseBooleanArray;

import de.dlyt.yanndroid.freshapp.R;
import de.dlyt.yanndroid.freshapp.utils.Constants;
import de.dlyt.yanndroid.freshapp.utils.Preferences;
import de.dlyt.yanndroid.freshapp.utils.Utils;

@SuppressLint("SdCardPath")
@SuppressWarnings("deprecation")
public class SettingsActivity extends PreferenceActivity implements OnPreferenceClickListener,
        OnPreferenceChangeListener, OnSharedPreferenceChangeListener, Constants {

    private static final String NOTIFICATIONS_IGNORED_RELEASE = "notifications_ignored_release";
    public final String TAG = this.getClass().getSimpleName();
    private Context mContext;
    private Preference mInstallPrefs;
    private Preference mAboutActivity;
    private RingtonePreference mRingtonePreference;

    private SparseBooleanArray mInstallPrefsItems = new SparseBooleanArray();

    private SwitchPreference mIgnoredRelease;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mContext = this;
        setTheme(Preferences.getSettingsTheme(mContext));
        super.onCreate(savedInstanceState);

        getPreferenceManager().setSharedPreferencesName(Preferences.PREF_NAME);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        addPreferencesFromResource(R.xml.preferences);

        mInstallPrefs = findPreference(INSTALL_PREFS);
        mInstallPrefs.setOnPreferenceClickListener(this);

        mAboutActivity = findPreference(ABOUT_ACTIVITY_PREF);
        mAboutActivity.setOnPreferenceClickListener(this);

        mRingtonePreference = (RingtonePreference) findPreference(NOTIFICATIONS_SOUND);

        String defValue = android.provider.Settings.System.DEFAULT_NOTIFICATION_URI.toString();
        String soundValue = getPreferenceManager().getSharedPreferences().getString
                (NOTIFICATIONS_SOUND, defValue);
        setRingtoneSummary(soundValue);

        mIgnoredRelease = (SwitchPreference) findPreference(NOTIFICATIONS_IGNORED_RELEASE);
        mIgnoredRelease.setOnPreferenceChangeListener(this);
        String ignoredRelease = Preferences.getIgnoredRelease(mContext);
        boolean isIgnored = ignoredRelease.equalsIgnoreCase("0");
        if (!isIgnored) {
            mIgnoredRelease.setSummary(
                    getString(R.string.notification_ignoring_release) + " " + ignoredRelease);
            mIgnoredRelease.setChecked(true);
            mIgnoredRelease.setEnabled(true);
            mIgnoredRelease.setSelectable(true);
        } else {
            setNotIgnore(false);
        }

        if (!MainActivity.hasRoot) {
            SwitchPreference ors = (SwitchPreference) findPreference("updater_twrp_ors");
            ors.setEnabled(false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener
                (this);
        mRingtonePreference.setOnPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference pref = findPreference(key);
        if (pref instanceof ListPreference) {
            ListPreference listPref = (ListPreference) pref;
            pref.setSummary(listPref.getEntry());

            if (key.equals(CURRENT_THEME)) {
                Preferences.setTheme(mContext, listPref.getValue());
                this.recreate();
            } else if (key.equals(UPDATER_BACK_FREQ)) {
                Utils.setBackgroundCheck(mContext, Preferences.getBackgroundService(mContext));
            }
        } else if (pref instanceof SwitchPreference) {
            if (key.equals(UPDATER_BACK_SERVICE)) {
                Utils.setBackgroundCheck(mContext, Preferences.getBackgroundService(mContext));
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference == mInstallPrefs) {
            showInstallPrefs();
        } else if (preference == mAboutActivity) {
            Builder builder = new Builder(this);
            final CharSequence[] items =
                    getApplicationContext().getResources().getStringArray(R.array.credits);
            builder.setTitle(R.string.about_credits_title);
            builder.setItems(items, (dialog, item) -> {
                switch (item) {
                    case 0:
                        try {
                            String sourceURL = getString(R.string.about_credits_matt_link);
                            Intent i = new Intent(Intent.ACTION_VIEW);
                            i.setData(Uri.parse(sourceURL));
                            startActivity(i);
                        } catch (ActivityNotFoundException activityNotFoundException) {
                            //
                        }
                        break;
                    case 1:
                        try {
                            String sourceURL = getString(R.string.about_credits_nicholas_link);
                            Intent i = new Intent(Intent.ACTION_VIEW);
                            i.setData(Uri.parse(sourceURL));
                            startActivity(i);
                        } catch (ActivityNotFoundException activityNotFoundException) {
                            //
                        }
                        break;
                }
                dialog.dismiss();
            });
            builder.create().show();
        }
        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mRingtonePreference) {
            setRingtoneSummary((String) newValue);
            return true;
        }
        return false;
    }

    private void setNotIgnore(boolean set) {
        if (set) Preferences.setIgnoredRelease(mContext, "0");
        mIgnoredRelease.setSummary(
                getResources().getString(R.string.notification_not_ignoring_release));
        mIgnoredRelease.setChecked(false);
        mIgnoredRelease.setEnabled(false);
        mIgnoredRelease.setSelectable(false);
    }

    private void showInstallPrefs() {
        boolean wipeData, wipeCache, wipeDalvik, deleteAfterInstall;

        wipeData = Preferences.getWipeData(mContext);
        wipeCache = Preferences.getWipeCache(mContext);
        wipeDalvik = Preferences.getWipeDalvik(mContext);
        deleteAfterInstall = Preferences.getDeleteAfterInstall(mContext);

        // Default value array for the multi-choice class.
        boolean[] defaultValues = {wipeData, wipeCache, wipeDalvik, deleteAfterInstall};

        // Also fill in InstallPrefItems with the default values
        // So that, if the user changes nothing, it doesn't reset all to false.
        mInstallPrefsItems.put(0, wipeData);
        mInstallPrefsItems.put(1, wipeCache);
        mInstallPrefsItems.put(2, wipeDalvik);
        mInstallPrefsItems.put(3, deleteAfterInstall);

        Builder mInstallPrefsDialog = new Builder(mContext);
        mInstallPrefsDialog.setTitle(R.string.twrp_ors_install_prefs);
        mInstallPrefsDialog.setMultiChoiceItems(R.array.ors_install_entries, defaultValues,
                (dialog, which, isChecked) -> mInstallPrefsItems.put(which, isChecked));
        mInstallPrefsDialog.setPositiveButton(R.string.ok, (dialog, id) -> {
            Preferences.setWipeData(mContext, mInstallPrefsItems.get(0));
            Preferences.setWipeCache(mContext, mInstallPrefsItems.get(1));
            Preferences.setWipeDalvik(mContext, mInstallPrefsItems.get(2));
            Preferences.setDeleteAfterInstall(mContext, mInstallPrefsItems.get(3));
        });
        mInstallPrefsDialog.show();
    }

    private void setRingtoneSummary(String soundValue) {
        Uri soundUri = TextUtils.isEmpty(soundValue) ? null : Uri.parse(soundValue);
        Ringtone tone = soundUri != null ? RingtoneManager.getRingtone(this, soundUri) : null;
        mRingtonePreference.setSummary(tone != null ? tone.getTitle(this) : getResources()
                .getString(R.string.silent_ringtone));
    }
}