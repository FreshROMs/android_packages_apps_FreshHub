package de.dlyt.yanndroid.fresh.settings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.dlyt.yanndroid.fresh.R;
import de.dlyt.yanndroid.fresh.database.TnsOta;
import de.dlyt.yanndroid.fresh.hub.AboutActivity;
import de.dlyt.yanndroid.fresh.hub.MainActivity;
import de.dlyt.yanndroid.fresh.hub.utils.Preferences;
import de.dlyt.yanndroid.fresh.renoir.RenoirService;
import de.dlyt.yanndroid.fresh.settings.sub.HDREffectSettingsActivity;
import de.dlyt.yanndroid.fresh.settings.sub.RenoirSettingsActivity;
import de.dlyt.yanndroid.fresh.settings.sub.ScreenResolutionActivity;
import de.dlyt.yanndroid.fresh.utils.Notifications;
import de.dlyt.yanndroid.fresh.utils.Tools;
import de.dlyt.yanndroid.samsung.layout.ToolbarLayout;
import io.tensevntysevn.fresh.ExperienceUtils;
import io.tensevntysevn.fresh.OverlayService;

public class FreshSettingsActivity extends AppCompatActivity {

    public static Context mContext;
    private static ExecutorService mExecutor;
    private static Integer mDataIconSelected;
    private static Integer mWlanIconSelected;
    private static boolean mBackground = true;

    private static SwitchMaterial mRenoirSwitch;
    private static SwitchMaterial mHDReffectSwitch;
    private static Spinner mDataIconSpinner;
    private static Spinner mWlanIconSpinner;

    String[] mResolutionValues;
    TextView mResolutionSummary;
    TextView mOtaSummary;
    TextView mRenoirSummary;
    LinearLayout mRenoirLayout;
    LinearLayout mResolutionLayout;
    LinearLayout mZestLayout;
    TextView mDataIconSummary;
    String[] mDataIconEntries;
    String[] mDataIconPackages;
    String[] mDataIconPackagesDeX;
    TextView mWlanIconSummary;
    String[] mWlanIconEntries;
    String[] mWlanIconPackages;
    String[] mWlanIconPackagesDeX;
    ArrayAdapter<String> mDataIconAdapter;
    ArrayAdapter<String> mWlanIconAdapter;

    int setResolution;
    boolean setRenoir = false;
    boolean setHDReffect = false;
    boolean disableRenoir = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mExecutor = Executors.newCachedThreadPool();
        mContext = this;
        mBackground = true;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zest_settings_main);

        ToolbarLayout toolbarLayout = findViewById(R.id.toolbar_layout);
        toolbarLayout.setExpanded(false, false);
        setSupportActionBar(toolbarLayout.getToolbar());
        toolbarLayout.setNavigationOnClickListener(v -> onBackPressed());

        mRenoirSwitch = findViewById(R.id.zest_switch_renoir);
        mHDReffectSwitch = findViewById(R.id.zest_switch_video_enhancer);
        mDataIconSpinner = findViewById(R.id.data_4g_icon_spinner);
        mWlanIconSpinner = findViewById(R.id.wlan_signal_icon_spinner);
        mResolutionValues = getResources().getStringArray(R.array.screen_resolution_options_summary);
        mResolutionSummary = findViewById(R.id.zest_screen_resolution_subtitle);
        mResolutionLayout = findViewById(R.id.zest_screen_resolution);
        mOtaSummary = findViewById(R.id.zest_update_badge);
        mRenoirSummary = findViewById(R.id.zest_renoir_subtitle);
        mRenoirLayout = findViewById(R.id.zest_layout_renoir);
        mDataIconSummary = findViewById(R.id.data_4g_icon_selected);
        mDataIconEntries = getResources().getStringArray(R.array.data_connection_icon_entries);
        mDataIconPackages = getResources().getStringArray(R.array.data_connection_icon_packages);
        mDataIconPackagesDeX = getResources().getStringArray(R.array.data_connection_icon_packages_dex);
        mWlanIconSummary = findViewById(R.id.wlan_signal_icon_selected);
        mWlanIconEntries = getResources().getStringArray(R.array.wlan_signal_icon_entries);
        mWlanIconPackages = getResources().getStringArray(R.array.wlan_signal_icon_packages);
        mWlanIconPackagesDeX = getResources().getStringArray(R.array.wlan_signal_icon_packages_dex);
        mDataIconAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mDataIconEntries);
        mWlanIconAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mWlanIconEntries);
        mZestLayout = findViewById(R.id.zest_main_hki);

        setResolution = ScreenResolutionActivity.getResolutionInt(mContext);
        setRenoir = RenoirService.getRenoirEnabled(mContext);
        setHDReffect = ExperienceUtils.isVideoEnhancerEnabled(mContext);
        disableRenoir = ExperienceUtils.isGalaxyThemeApplied(mContext);

        TextView mFreshVersionText = findViewById(R.id.zest_rom_version_subtitle);

        TextView mFreshHubVersionText = findViewById(R.id.zest_hub_version_subtitle);
        mResolutionSummary.setText(mResolutionValues[setResolution]);

        String romVersionBranch = "";
        String romBranchString = ExperienceUtils.getProp(getResources().getString(R.string.ota_swupdate_prop_branch));

        if (!romBranchString.isEmpty()) {
            romVersionBranch = romBranchString.substring(0, 1).toUpperCase() + romBranchString.substring(1).toLowerCase();
        }

        String romVersion = ExperienceUtils.getProp(getResources().getString(R.string.ota_swupdate_prop_release)) + " "
                + romVersionBranch
                + " (" + ExperienceUtils.getProp(getResources().getString(R.string.ota_swupdate_prop_version)) + ")";

        String buildDate = ExperienceUtils.getProp(getResources().getString(R.string.ota_swupdate_prop_date));

        if (!(ExperienceUtils.getProp(getResources().getString(R.string.ota_swupdate_prop_date_actual))).equals("")) {
            buildDate = ExperienceUtils.getProp(getResources().getString(R.string.ota_swupdate_prop_date_actual));
        }

        String dualLineVersion = romVersion + "\n" + buildDate;
        mFreshVersionText.setText(dualLineVersion);

        try {
            PackageInfo packageInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            String versionName = packageInfo.versionName;
            @SuppressWarnings("deprecation") int versionCode = packageInfo.versionCode;
            String hubVersion = versionName + " (" + versionCode + ")";
            mFreshHubVersionText.setText(hubVersion);
        } catch (PackageManager.NameNotFoundException e) {
            mFreshHubVersionText.setVisibility(View.GONE);
        }

        Notifications.setupCustomizationNotifChannel(mContext);
        easterEgg();
    }

    @Override
    protected void onResume() {
        updatePreferences();
        super.onResume();
    }

    @Override
    protected void onPause() {
        mBackground = true;
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.settings_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.zest_settings_shortcut) {
            ComponentName cn = new ComponentName("com.android.settings.intelligence", "com.android.settings.intelligence.search.SearchActivity");
            Intent intent = new Intent();
            intent.setComponent(cn);
            mContext.startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        mBackground = true;
        updatePreferences();
        super.onConfigurationChanged(newConfig);
    }

    public static boolean isFreshBuildEligibleForZest(Context context) {
        PackageManager packageManager = context.getPackageManager();
        return packageManager.hasSystemFeature("io.tensevntysevn.fresh.hikari");
    }

    public void openZestCMT(View v) {
        startActivity(new Intent().setClass(getApplicationContext(), RenoirSettingsActivity.class));
    }

    public void openZestScreenResolution(View v) {
        startActivity(new Intent().setClass(getApplicationContext(), ScreenResolutionActivity.class));
    }

    public void openZestVideoBrightness(View v) {
        startActivity(new Intent().setClass(getApplicationContext(), HDREffectSettingsActivity.class));
    }

    public void openZestShortcutSecAdvanced(View v) {
        ComponentName cn = new ComponentName("com.android.settings", "com.android.settings.Settings$UsefulFeatureMainActivity");
        Intent intent = new Intent();
        intent.setComponent(cn);
        mContext.startActivity(intent);
    }

    public void openZestShortcutSecWallpaper(View v) {
        ComponentName cn = new ComponentName("com.samsung.android.app.dressroom", "com.samsung.android.app.dressroom.presentation.settings.WallpaperSettingActivity");
        Intent intent = new Intent();
        intent.setComponent(cn);
        mContext.startActivity(intent);
    }

    public void openZestShortcutSecThemes(View v) {
        ComponentName cn = new ComponentName("com.samsung.android.themestore", "com.samsung.android.themestore.activity.MainActivity");
        Intent intent = new Intent();
        intent.setComponent(cn);
        mContext.startActivity(intent);
    }

    public void openZestHubAboutPage(View v) {
        startActivity(new Intent().setClass(getApplicationContext(), AboutActivity.class));
    }

    public void openZestSwUpdate(View v) {
        startActivity(new Intent().setClass(getApplicationContext(), MainActivity.class));
    }

    public void openZestDataSpinner(View v) {
        Spinner options_spinner = findViewById(R.id.data_4g_icon_spinner);
        options_spinner.performClick();
    }

    public void openZestWlanSpinner(View v) {
        Spinner options_spinner = findViewById(R.id.wlan_signal_icon_spinner);
        options_spinner.performClick();
    }

    public static void setLayoutEnabled(View view, boolean enable) {
        view.setEnabled(enable);
        view.setClickable(enable);
        view.setFocusable(enable);
        view.setAlpha(enable ? 1f : 0.7f);
    }

    private void updatePreferences() {
        Handler handler = new Handler(Looper.getMainLooper());

        final boolean updateAvailable = TnsOta.getUpdateAvailability(mContext);
        setResolution = ScreenResolutionActivity.getResolutionInt(mContext);
        setHDReffect = ExperienceUtils.isVideoEnhancerEnabled(mContext);

        ExperienceUtils.getRealScreenWidth(mContext, Tools.getActivity(mContext));

        if (!isFreshBuildEligibleForZest(mContext)) {
            mZestLayout.setVisibility(View.GONE);
        } else {
            if (RenoirService.isFreshBuildEligibleForRenoir(mContext)) {
                setRenoir = RenoirService.getRenoirEnabled(mContext);
                disableRenoir = ExperienceUtils.isGalaxyThemeApplied(mContext);

                mRenoirSwitch.setChecked(setRenoir);
            } else {
                mRenoirLayout.setVisibility(View.GONE);
            }

            mDataIconAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mDataIconSpinner.setAdapter(mDataIconAdapter);
            mDataIconAdapter.notifyDataSetChanged();

            mWlanIconAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mWlanIconSpinner.setAdapter(mWlanIconAdapter);
            mWlanIconAdapter.notifyDataSetChanged();

            mDataIconSelected = Preferences.getDataConnectionIconInt(mContext);
            mWlanIconSelected = Preferences.getWlanConnectionIconInt(mContext);

            mDataIconSpinner.setSelection(mDataIconSelected);
            mDataIconSummary.setText(mDataIconEntries[mDataIconSelected]);

            mWlanIconSpinner.setSelection(mWlanIconSelected);
            mWlanIconSummary.setText(mWlanIconEntries[mWlanIconSelected]);

            if (RenoirService.isFreshBuildEligibleForRenoir(mContext)) {
                setLayoutEnabled(mRenoirLayout, !disableRenoir);
                if (disableRenoir) {
                    mRenoirSummary.setText(R.string.renoir_settings_desc_unavailable);
                    mRenoirSwitch.setEnabled(false);
                }

                if (ExperienceUtils.isDesktopMode(mContext)) {
                    setLayoutEnabled(mRenoirLayout, false);
                    setLayoutEnabled(mResolutionLayout, false);
                    mRenoirSwitch.setEnabled(false);
                    findViewById(R.id.zest_relative_link_wallpaper).setVisibility(View.GONE);
                    findViewById(R.id.zest_relative_link_themes).setVisibility(View.GONE);
                }

                mRenoirSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (!(isChecked == setRenoir) && !mBackground) {
                        mRenoirSwitch.setEnabled(false);
                        setLayoutEnabled(mRenoirLayout, false);
                        RenoirService.setRenoirEnabled(this, isChecked);

                        handler.postDelayed(() -> {
                            setLayoutEnabled(mRenoirLayout, true);
                            mRenoirSwitch.setEnabled(true);
                        }, 1500);
                    }
                });
            }

            mDataIconSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int selection, long id) {
                    String oldPackage = mDataIconPackages[mDataIconSelected];
                    String oldPackageDeX = mDataIconPackagesDeX[mDataIconSelected];

                    String newPackage = mDataIconPackages[selection];
                    String newPackageDeX = mDataIconPackagesDeX[selection];

                    mDataIconSelected = selection;
                    mDataIconSummary.setText(mDataIconEntries[selection]);
                    Preferences.setDataConnectionIconInt(mContext, selection);

                    if (!mBackground && !oldPackage.equals(newPackage)) {
                        mExecutor.execute(() -> {
                            if (!newPackage.equals(mWlanIconPackages[0])) {
                                OverlayService.setOverlayState(newPackage, true);
                                OverlayService.setOverlayState(newPackageDeX, true);
                            }

                            if (!oldPackage.equals(mWlanIconPackages[0])) {
                                OverlayService.setOverlayState(oldPackage, false);
                                OverlayService.setOverlayState(oldPackageDeX, false);
                            }
                        });
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });

            mWlanIconSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int selection, long id) {
                    String oldPackage = mWlanIconPackages[mWlanIconSelected];
                    String oldPackageDeX = mWlanIconPackagesDeX[mWlanIconSelected];

                    String newPackage = mWlanIconPackages[selection];
                    String newPackageDeX = mWlanIconPackagesDeX[selection];

                    mWlanIconSelected = selection;
                    mWlanIconSummary.setText(mWlanIconEntries[selection]);
                    Preferences.setWlanConnectionIconInt(mContext, selection);

                    if (!mBackground && !oldPackage.equals(newPackage)) {
                        mExecutor.execute(() -> {
                            if (!newPackage.equals(mWlanIconPackages[0])) {
                                OverlayService.setOverlayState(newPackage, true);
                                OverlayService.setOverlayState(newPackageDeX, true);
                            }

                            if (!oldPackage.equals(mWlanIconPackages[0])) {
                                OverlayService.setOverlayState(oldPackage, false);
                                OverlayService.setOverlayState(oldPackageDeX, false);
                            }
                        });
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }

        mResolutionSummary.setText(mResolutionValues[setResolution]);
        mHDReffectSwitch.setChecked(setHDReffect);

        mOtaSummary.setVisibility(updateAvailable ? View.VISIBLE : View.GONE);

        mHDReffectSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!(isChecked == setHDReffect) && !mBackground)
                ExperienceUtils.setVideoEnhancerEnabled(this, isChecked);
        });

        mBackground = false;
    }

    public void easterEgg() {
        View mVersionLabel = findViewById(R.id.zest_rom_version);
        long[] mHits = new long[3];

        mVersionLabel.setOnClickListener(v -> {
            //noinspection SuspiciousSystemArraycopy
            System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
            mHits[mHits.length - 1] = SystemClock.uptimeMillis();
            if (mHits[0] >= (SystemClock.uptimeMillis() - 500)) {
                String url = "https://www.youtube.com/watch?v=aYsgsSo1aow";
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
        });

    }
}