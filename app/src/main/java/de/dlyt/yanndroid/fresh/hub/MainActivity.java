package de.dlyt.yanndroid.fresh.hub;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.webkit.WebResourceErrorCompat;
import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewClientCompat;
import androidx.webkit.WebViewFeature;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.dlyt.yanndroid.fresh.Constants;
import de.dlyt.yanndroid.fresh.R;
import de.dlyt.yanndroid.fresh.database.TnsAddonDownload;
import de.dlyt.yanndroid.fresh.database.TnsOta;
import de.dlyt.yanndroid.fresh.database.TnsOtaDownload;
import de.dlyt.yanndroid.fresh.hub.utils.Preferences;
import de.dlyt.yanndroid.fresh.services.TnsOtaApiService;
import de.dlyt.yanndroid.fresh.settings.sub.NotificationSettingsActivity;
import de.dlyt.yanndroid.fresh.utils.File;
import de.dlyt.yanndroid.fresh.utils.JobScheduler;
import de.dlyt.yanndroid.fresh.utils.Notifications;
import de.dlyt.yanndroid.fresh.utils.SystemProperties;
import de.dlyt.yanndroid.fresh.utils.Tools;
import de.dlyt.yanndroid.samsung.drawer.OptionButton;
import de.dlyt.yanndroid.samsung.drawer.OptionGroup;
import de.dlyt.yanndroid.samsung.layout.DrawerLayout;

public class MainActivity extends AppCompatActivity implements Constants,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private static final int CHANGE_THEME_REQUEST_CODE = 2;
    private static final boolean ENABLE_COMPATIBILITY_CHECK = true;
    public static boolean hasRoot;
    public static Handler UIHandler;
    @SuppressLint("StaticFieldLeak")
    private static de.dlyt.yanndroid.samsung.ProgressBar mProgressBar;
    @SuppressLint("StaticFieldLeak")
    private static Context mContext;
    private final String TAG = this.getClass().getSimpleName();
    String omc_url = "https://fresh.tensevntysevn.cf/app/omc/";
    String feedback_url = "https://fresh.tensevntysevn.cf/app/feedback/";
    private WebView mWebView;
    ProgressBar web_progressbar;
    ProgressBar ota_progressbar;
    SwipeRefreshLayout swipeRefreshLayout;
    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // There are no request codes
                    Intent data = result.getData();
                }
            });
    private Builder mCompatibilityDialog;
    private Builder mDonateDialog;
    private Builder mPlayStoreDialog;
    private Builder mRebootDialog;
    private DrawerLayout drawerLayout;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(MANIFEST_LOADED)) {
                updateAllLayouts();
                Notifications.cancelOngoingCheckNotification(context);
                ota_progressbar.setVisibility(View.GONE);
                checkforAppUpdate();
                findViewById(R.id.swiperefresh).setEnabled(true);
            }
        }
    };

    public static void updateProgress(int progress) {
        if (mProgressBar != null) {
            mProgressBar.setProgress(progress);
        }
    }

    public static void setLayoutEnabled(LinearLayout view, boolean enable) {
        view.setEnabled(enable);
        view.setClickable(enable);
        view.setFocusable(enable);
        view.setAlpha(enable ? 1f : 0.7f);
    }

    public static void runOnUI(Runnable runnable) {
        UIHandler.post(runnable);
    }

    private void updateAllLayouts() {
        try {
            updateCommunityLinksLayout();
            updateAddonsLayout();
            updateRomInformation();
            updateRomUpdateLayouts(true);
        } catch (Exception e) {
            // Suppress warning
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mContext = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hub_main);

        mWebView = findViewById(R.id.webview);
        web_progressbar = findViewById(R.id.web_progressbar);
        ota_progressbar = findViewById(R.id.ota_progressbar);
        SwitchMaterial appIcon = findViewById(R.id.switch_app_icon);
        SwitchMaterial notifSwitch = findViewById(R.id.switch_notifications);

        drawerLayout = findViewById(R.id.drawer_layout);
        drawerLayout.setToolbarTitle(getString(R.string.update));
        initDrawerItems();
        initWebView();

        Notifications.setupNotificationChannel(mContext);
        Notifications.setupOngoingNotificationChannel(mContext);
        JobScheduler.setupJobScheduler(mContext, !Preferences.getBackgroundService(mContext));

        boolean firstRun = Preferences.getFirstRun(mContext);
        if (firstRun) {
            Preferences.setFirstRun(mContext, false);
        }

        // Create download directories if needed
        mContext.getExternalFilesDir(OTA_DIR_ROM);
        mContext.getExternalFilesDir(OTA_DIR_ADDONS);

        createDialogs();

        appIcon.setChecked(Preferences.getAppIconState(mContext));
        notifSwitch.setChecked(Preferences.getBackgroundService(mContext));

        // But check permissions first - download will be started in the callback
        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(),
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }

        appIcon.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Preferences.setAppIconState(mContext, isChecked);
            Preferences.toggleAppIcon(mContext, isChecked);
        });

        notifSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Preferences.setBackgroundService(mContext, isChecked);
                JobScheduler.setBackgroundCheck(mContext, Preferences.getBackgroundService(mContext));
            }
        });

        // Delete OTA on App open
        boolean isDeviceUpdating = TnsOtaDownload.getIsDeviceUpdating(mContext);
        if (isDeviceUpdating) {
            TnsOta.setUpdateAvailability(mContext);
            boolean isUpdateSuccessful = !(TnsOta.getUpdateAvailability(mContext));
            String updateVersion = TnsOta.getReleaseVersion(mContext);
            Notifications.sendPostUpdateNotification(mContext, updateVersion, isUpdateSuccessful);

            TnsOtaDownload.setIsDeviceUpdating(mContext, false);

            java.io.File updateFile = TnsOta.getFullFile(mContext);

            if (updateFile.exists()) {
                boolean deleted = updateFile.delete();
                if (!deleted) Log.e(TAG, "Could not delete update file...");
            }
        }

        String isUninstallingAddon = TnsAddonDownload.getIsUninstallingAddon(mContext);

        if (isUninstallingAddon != null) {
            java.io.File file = new java.io.File(mContext.getExternalFilesDir(OTA_DIR_ADDONS), isUninstallingAddon + ".zip");
            if (file.exists()) {
                boolean deleted = file.delete();
                if (!deleted) Log.e(TAG, "Unable to delete file...");
            }

            TnsAddonDownload.setIsUninstallingAddon(mContext, null);
        }

        egg();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
            switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
                case Configuration.UI_MODE_NIGHT_YES:
                    WebSettingsCompat.setForceDark(mWebView.getSettings(), WebSettingsCompat.FORCE_DARK_ON);
                    break;
                case Configuration.UI_MODE_NIGHT_NO:
                case Configuration.UI_MODE_NIGHT_UNDEFINED:
                    WebSettingsCompat.setForceDark(mWebView.getSettings(), WebSettingsCompat.FORCE_DARK_OFF);
                    break;
            }
        }
    }

    public void checkforAppUpdate() {

        try {
            PackageInfo packageInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            int appServerVersion = TnsOta.getAppVersion(mContext);
            int appLocalVersion = packageInfo.versionCode;
            Boolean showIcon = appLocalVersion < appServerVersion;
            drawerLayout.showIconNotification(showIcon, showIcon);
        } catch (PackageManager.NameNotFoundException ignored) {
        }
    }

    public void initDrawerItems() {

        /**Items*/
        drawerLayout.setDrawerIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAbout();
            }
        });


        View feedback_content = findViewById(R.id.feedback_content);
        View ota_content = findViewById(R.id.ota_content);

        OptionGroup optionGroup = findViewById(R.id.optionGroup);
        optionGroup.setOnOptionButtonClickListener(new OptionGroup.OnOptionButtonClickListener() {
            @Override
            public void onOptionButtonClick(OptionButton optionButton, int i, int i1) {
                switch (i) {
                    case R.id.drawer_update:
                        drawerLayout.setToolbarTitle(getString(R.string.update));
                        ota_content.setVisibility(View.VISIBLE);
                        ota_progressbar.setVisibility(View.VISIBLE);
                        if (ENABLE_COMPATIBILITY_CHECK) new CompatibilityTask(mContext);
                        updateCommunityLinksLayout();
                        updateAddonsLayout();
                        updateRomInformation();
                        updateRomUpdateLayouts(false);
                        refreshDrawer();
                        feedback_content.setVisibility(View.GONE);
                        break;
                    case R.id.drawer_feedback:
                        ota_content.setVisibility(View.GONE);
                        feedback_content.setVisibility(View.VISIBLE);
                        web_progressbar.setVisibility(View.VISIBLE);
                        mWebView.loadUrl(feedback_url);
                        drawerLayout.setToolbarSubtitle(getString(R.string.feedback));
                        break;
                    case R.id.drawer_omc_request:
                        ota_content.setVisibility(View.GONE);
                        feedback_content.setVisibility(View.VISIBLE);
                        web_progressbar.setVisibility(View.VISIBLE);
                        mWebView.loadUrl(omc_url);
                        drawerLayout.setToolbarSubtitle(getString(R.string.omc_request));
                        break;
                }
                drawerLayout.setDrawerOpen(false, true);
            }
        });

        OptionButton drawer_fresh_group = findViewById(R.id.drawer_fresh_group);
        drawer_fresh_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/FreshROMs")));
            }
        });

        OptionButton drawer_discord_group = findViewById(R.id.drawer_github_issues);
        drawer_discord_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(TnsOta.getGitIssues(mContext))));
            }
        });


        OptionButton drawer_reboot = findViewById(R.id.drawer_reboot);
        drawer_reboot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRebootDialog.show();
            }
        });
    }

    public void refreshDrawer() {
        View drawer_github_issues = findViewById(R.id.drawer_github_issues);
        String github_issues_url = TnsOta.getGitIssues(mContext);
        if (github_issues_url.trim().equals("null")) {
            drawer_github_issues.setVisibility(View.GONE);
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    public void initWebView() {
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new WebViewClientCompat() {

            @Override
            public void onPageStarted(@NonNull WebView view, @NonNull String url, @NonNull Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                web_progressbar.setVisibility(View.VISIBLE);
                findViewById(R.id.swiperefresh).setEnabled(false);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                web_progressbar.setVisibility(View.GONE);
                findViewById(R.id.swiperefresh).setEnabled(true);
            }

            @Override
            public void onReceivedError(@NonNull WebView view, @NonNull WebResourceRequest request, @NonNull WebResourceErrorCompat error) {
                super.onReceivedError(view, request, error);
                web_progressbar.setVisibility(View.GONE);
            }
        });

        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
            switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
                case Configuration.UI_MODE_NIGHT_YES:
                    WebSettingsCompat.setForceDark(mWebView.getSettings(), WebSettingsCompat.FORCE_DARK_ON);
                    break;
                case Configuration.UI_MODE_NIGHT_NO:
                case Configuration.UI_MODE_NIGHT_UNDEFINED:
                    WebSettingsCompat.setForceDark(mWebView.getSettings(), WebSettingsCompat.FORCE_DARK_OFF);
                    break;
            }
        }

        //noinspection RestrictedApi
        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK_STRATEGY)) {
            WebSettingsCompat.setForceDarkStrategy(mWebView.getSettings(), WebSettingsCompat.DARK_STRATEGY_PREFER_WEB_THEME_OVER_USER_AGENT_DARKENING);
        }

        mWebView.loadUrl(feedback_url);
    }


    @Override
    public void onStart() {
        super.onStart();
        UIHandler = new Handler(Looper.getMainLooper());
        this.registerReceiver(mReceiver, new IntentFilter(MANIFEST_LOADED));

        swipeRefreshLayout = findViewById(R.id.swiperefresh);

        // Check the correct build prop values are installed
        // Also executes the manifest/update check

        if (!Tools.isDeviceOnline(mContext)) {
            Builder notConnectedDialog = new Builder(mContext, R.style.AlertDialogStyle);
            notConnectedDialog.setTitle(R.string.main_not_connected_title)
                    .setMessage(R.string.main_not_connected_message)
                    .setCancelable(false)
                    .setPositiveButton(R.string.ok, (dialog, which) -> ((Activity) mContext)
                            .finish())
                    .show();
        } else {
            if (ENABLE_COMPATIBILITY_CHECK) new CompatibilityTask(mContext);
        }

        // Has the download already completed?
        File.setHasFileDownloaded(mContext);
        findViewById(R.id.swiperefresh).setEnabled(false);

        if (TnsOtaDownload.getIsDownloadOnGoing(mContext)) {
            updateRomUpdateLayouts(true);
            findViewById(R.id.swiperefresh).setEnabled(false);
        } else {
            updateRomUpdateLayouts(false);
            openCheckForUpdates(null);
            findViewById(R.id.swiperefresh).setEnabled(true);
        }

        // Update the layouts
        updateCommunityLinksLayout();
        updateAddonsLayout();
        updateRomInformation();
        refreshDrawer();

        swipeRefreshLayout.setOnRefreshListener(() -> {
            ota_progressbar.setVisibility(View.VISIBLE);
            web_progressbar.setVisibility(View.VISIBLE);
            if (ENABLE_COMPATIBILITY_CHECK) new CompatibilityTask(mContext);
            updateCommunityLinksLayout();
            updateAddonsLayout();
            updateRomInformation();
            updateRomUpdateLayouts(false);
            refreshDrawer();
            mWebView.reload();
            swipeRefreshLayout.setRefreshing(false);
            findViewById(R.id.swiperefresh).setEnabled(false);
        });


        SwitchMaterial notifSwitch = findViewById(R.id.switch_notifications);
        notifSwitch.setChecked(Preferences.getBackgroundService(mContext));


    }

    @Override
    public void onStop() {
        super.onStop();
        this.unregisterReceiver(mReceiver);
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length <= 0 || grantResults[0]
                    != PackageManager.PERMISSION_GRANTED) {
                new Builder(this, R.style.AlertDialogStyle)
                        .setTitle(R.string.permission_not_granted_dialog_title)
                        .setMessage(R.string.permission_not_granted_dialog_message)
                        .setPositiveButton(R.string.dialog_ok, (dialog, which) ->
                                MainActivity.this.finish()).show();
            }
        }
    }

    private void createDialogs() {
        //reboot dialog
        mRebootDialog = new Builder(mContext, R.style.AlertDialogStyle);
        mRebootDialog.setTitle(R.string.are_you_sure)
                .setMessage(R.string.available_reboot_confirm)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    Tools.rebootUpdate(mContext);
                }).setNegativeButton(R.string.cancel, null);


        // Compatibility Dialog
        mCompatibilityDialog = new Builder(mContext, R.style.AlertDialogStyle);
        mCompatibilityDialog.setCancelable(false);
        mCompatibilityDialog.setTitle(R.string.main_not_compatible_title);
        mCompatibilityDialog.setMessage(R.string.main_not_compatible_message);
        mCompatibilityDialog.setPositiveButton(R.string.ok, (dialog, which) -> MainActivity.this.finish());

        /**enable to test the app without props*/
        //mCompatibilityDialog.setNegativeButton("debug", (dialog, which) -> dialog.cancel());

        // Donate Dialog
        mDonateDialog = new Builder(this, R.style.AlertDialogStyle);
        String[] donateItems = {"PayPal", "BitCoin"};
        mDonateDialog.setTitle(getResources().getString(R.string.donate))
                .setSingleChoiceItems(donateItems, 0, null)
                .setPositiveButton(getResources().getString(R.string.ok), (dialog, which) -> {
                    String url;
                    int selectedPosition = ((AlertDialog) dialog).getListView()
                            .getCheckedItemPosition();
                    if (selectedPosition == 0) {
                        url = TnsOta.getDonateLink(mContext);
                    } else {
                        url = TnsOta.getBitCoinLink(mContext);
                    }
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));

                    try {
                        startActivity(intent);
                    } catch (ActivityNotFoundException ex) {
                        // Nothing to handle BitCoin payments. Send to Play Store
                        if (DEBUGGING)
                            Log.d(TAG, ex.getMessage());
                        mPlayStoreDialog.show();
                    }
                })
                .setNegativeButton(getResources().getString(R.string.cancel), (dialog, which) ->
                        dialog.cancel());

        mPlayStoreDialog = new Builder(mContext, R.style.AlertDialogStyle);
        mPlayStoreDialog.setCancelable(true);
        mPlayStoreDialog.setTitle(R.string.main_playstore_title);
        mPlayStoreDialog.setMessage(R.string.main_playstore_message);
        mPlayStoreDialog.setPositiveButton(R.string.ok, (dialog, which) -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            String url = "https://play.google.com/store/search?q=bitcoin%20wallet&c=apps";
            intent.setData(Uri.parse(url));
            startActivity(intent);
        });
        mPlayStoreDialog.setNegativeButton(getResources().getString(R.string.cancel), (dialog, which) -> dialog.cancel());
    }

    @SuppressWarnings("deprecation")
    private void updateRomUpdateLayouts(Boolean isLoaded) {
        View updateAvailable = findViewById(R.id.layout_main_update_available);
        View updateNotAvailable = findViewById(R.id.layout_main_no_update_available);
        View updateChecking = findViewById(R.id.layout_main_checking_update_available);
        updateAvailable.setVisibility(View.GONE);
        updateNotAvailable.setVisibility(View.GONE);
        updateChecking.setVisibility(View.GONE);

        TextView updateAvailableSummary = (TextView) findViewById(R.id.main_tv_update_available_summary);
        TextView updateNotAvailableSummary = (TextView) findViewById(R.id.main_tv_no_update_available_summary);
        TextView updateCheckingSummary = (TextView) findViewById(R.id.main_tv_checking_update_summary);

        mProgressBar = (de.dlyt.yanndroid.samsung.ProgressBar) findViewById(R.id.bar_main_progress_bar);
        mProgressBar.setVisibility(View.GONE);

        Long currentTimeMillis = System.currentTimeMillis();
        Long lastCheckedTimeMillis = TnsOtaDownload.getUpdateLastChecked(mContext);

        // Update is available
        if (isLoaded) {
            if (TnsOta.getUpdateAvailability(mContext)) {
                updateAvailable.setVisibility(View.VISIBLE);
                TextView updateAvailableTitle = (TextView) findViewById(R.id.main_tv_update_available_title);

                if (TnsOtaDownload.getDownloadFinished(mContext)) { //  Update already finished?
                    updateAvailableTitle.setText(getResources().getString(R.string
                            .main_update_finished));
                    String htmlColorOpen;
                    htmlColorOpen = "<font color='" + getResources().getColor(R.color.item_color) + "'>";
                    String htmlColorClose = "</font>";
                    String updateSummary = getResources().getString(R.string.system_name) + " " +
                            TnsOta.getReleaseVersion(mContext) + " " + TnsOta.getReleaseVariant(mContext);
                    updateAvailableSummary.setText(Html.fromHtml(updateSummary));
                } else if (TnsOtaDownload.getIsDownloadOnGoing(mContext)) {
                    updateAvailableTitle.setText(getResources().getString(R.string
                            .main_update_progress));
                    mProgressBar.setVisibility(View.VISIBLE);
                    String htmlColorOpen;
                    htmlColorOpen = "<font color='" + getResources().getColor(R.color.item_color) + "'>";
                    String htmlColorClose = "</font>";
                    String updateSummary = htmlColorOpen
                            + getResources().getString(R.string.main_tap_to_view_progress)
                            + htmlColorClose;
                    updateAvailableSummary.setText(Html.fromHtml(updateSummary));
                } else {
                    updateAvailableTitle.setText(getResources().getString(R.string
                            .main_update_available));
                    drawerLayout.setToolbarSubtitle(getResources().getString(R.string.main_update_available));
                    String htmlColorOpen;
                    htmlColorOpen = "<font color='" + getResources().getColor(R.color.item_color) + "'>";
                    String htmlColorClose = "</font>";
                    String updateSummary = getResources().getString(R.string.system_name) + " " +
                            TnsOta.getReleaseVersion(mContext) + " " + TnsOta.getReleaseVariant(mContext);

                    updateAvailableSummary.setText(Html.fromHtml(updateSummary));
                }
            } else {
                updateNotAvailable.setVisibility(View.VISIBLE);
                drawerLayout.setToolbarSubtitle(getResources().getString(R.string.main_no_update_available));

                CharSequence localizedTime = DateUtils.getRelativeTimeSpanString(lastCheckedTimeMillis, currentTimeMillis, DateUtils.DAY_IN_MILLIS);

                TnsOtaDownload.setUpdateLastChecked(this, currentTimeMillis);
                String lastChecked = getString(R.string.main_last_checked) + " ";
                updateNotAvailableSummary.setText(String.format("%s%s", lastChecked, localizedTime));
            }
        } else {
            String romBranchString = SystemProperties.getProp(getResources().getString(R.string.ota_swupdate_prop_branch));
            String romVersionBranch = !romBranchString.isEmpty() ? romBranchString.substring(0, 1).toUpperCase() + romBranchString.substring(1).toLowerCase() : " ";

            updateChecking.setVisibility(View.VISIBLE);
            drawerLayout.setToolbarSubtitle(getResources().getString(R.string.main_checking_updates));

            String updateCheckSummary = getResources().getString(R.string.system_name) + " " +
                    SystemProperties.getProp(getResources().getString(R.string.ota_swupdate_prop_release)) + " " +
                    romVersionBranch;

            updateCheckingSummary.setText(Html.fromHtml(updateCheckSummary));
        }
    }

    private void updateAddonsLayout() {
        LinearLayout addonsLink = findViewById(R.id.layout_main_addons);
        addonsLink.setVisibility(View.GONE);

        if (TnsOta.getAddonsCount(mContext) > 0) {
            addonsLink.setVisibility(View.VISIBLE);
        }
    }

    private void updateCommunityLinksLayout() {
        LinearLayout linkXdaForum = (LinearLayout) findViewById(R.id.layout_main_forum);
        LinearLayout linkDiscord = (LinearLayout) findViewById(R.id.layout_main_discord);
        LinearLayout linkTelegram = (LinearLayout) findViewById(R.id.layout_main_telegram);
        LinearLayout linkDiscussions = (LinearLayout) findViewById(R.id.layout_main_discussions);
        LinearLayout linkWebsite = (LinearLayout) findViewById(R.id.layout_main_website);
        LinearLayout linkDonate = (LinearLayout) findViewById(R.id.layout_main_donate);

        linkXdaForum.setVisibility(View.GONE);
        linkDiscord.setVisibility(View.GONE);
        linkTelegram.setVisibility(View.GONE);
        linkDiscussions.setVisibility(View.GONE);
        linkWebsite.setVisibility(View.GONE);
        linkDonate.setVisibility(View.GONE);

        if (!TnsOta.getForum(mContext).trim().equals("null")) {
            linkXdaForum.setVisibility(View.VISIBLE);
            linkXdaForum.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(TnsOta.getForum(mContext))));
                }
            });
        }

        if (!TnsOta.getDiscord(mContext).trim().equals("null")) {
            linkDiscord.setVisibility(View.VISIBLE);
            linkDiscord.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(TnsOta.getDiscord(mContext))));
                }
            });
        }

        if (!TnsOta.getGitIssues(mContext).trim().equals("null")) {
            linkTelegram.setVisibility(View.VISIBLE);
            linkTelegram.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(TnsOta.getDiscord(mContext))));
                }
            });
        }

        if (!TnsOta.getGitDiscussion(mContext).trim().equals("null")) {
            linkDiscussions.setVisibility(View.VISIBLE);
            linkDiscussions.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(TnsOta.getGitDiscussion(mContext))));
                }
            });
        }

        if (!TnsOta.getWebsite(mContext).trim().equals("null")) {
            linkWebsite.setVisibility(View.VISIBLE);
            linkWebsite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(TnsOta.getWebsite(mContext))));
                }
            });
        }

        if (!(TnsOta.getDonateLink(mContext).trim().equals("null"))
                || !(TnsOta.getBitCoinLink(mContext).trim().equals("null"))) {
            linkDonate.setVisibility(View.VISIBLE);
        }
    }

    @SuppressWarnings("deprecation")
    private void updateRomInformation() {
        String htmlColorOpen;
        htmlColorOpen = "<font color='" + getResources().getColor(R.color.item_color) + "'>";
        String htmlColorClose = "</font>";

        String space = " ";
        String separator_open = " (";
        String separator_close = ") ";

        //ROM version
        TextView romVersion = (TextView) findViewById(R.id.tv_main_rom_version);
        String romVersionTitle = getApplicationContext().getResources().getString(R.string
                .system_name) + " ";
        String romVersionName = SystemProperties.getProp(getResources().getString(R.string.ota_swupdate_prop_release)) + " ";
        String romBranchString = SystemProperties.getProp(getResources().getString(R.string.ota_swupdate_prop_branch));
        String romVersionString = SystemProperties.getProp(getResources().getString(R.string.ota_swupdate_prop_version));
        if (!romBranchString.isEmpty()) {
            String romVersionBranch = romBranchString.substring(0, 1).toUpperCase() + romBranchString.substring(1).toLowerCase();
            romVersion.setText(Html.fromHtml(htmlColorOpen + romVersionTitle + romVersionName + romVersionBranch + separator_open + romVersionString + separator_close + htmlColorClose));
        }

        //ROM date
        TextView romDate = (TextView) findViewById(R.id.tv_main_rom_date);
        String romDateTitle = getApplicationContext().getResources().getString(R.string
                .main_rom_build_date) + " ";

        String romDateActual = SystemProperties.getProp(getResources().getString(R.string.ota_swupdate_prop_date));

        if (!(SystemProperties.getProp(getResources().getString(R.string.ota_swupdate_prop_date_actual))).equals("")) {
            romDateActual = SystemProperties.getProp(getResources().getString(R.string.ota_swupdate_prop_date_actual));
        }

        romDate.setText(Html.fromHtml(romDateTitle + htmlColorOpen + romDateActual +
                htmlColorClose));

        //ROM security patch
        TextView splVersion = (TextView) findViewById(R.id.tv_main_android_spl);
        String romSplTitle = getApplicationContext().getResources().getString(R.string
                .main_rom_spl) + " ";
        String romSplActual = TnsOta.renderAndroidSpl(SystemProperties.getProp("ro.build.version.security_patch"));
        splVersion.setText(Html.fromHtml(romSplTitle + htmlColorOpen + romSplActual +
                htmlColorClose));
    }

    public void openNotiSettings(View v) {
        startActivity(new Intent().setClass(getApplicationContext(), NotificationSettingsActivity.class));
    }

    public void toggleAppIconSwitch(View v) {
        SwitchMaterial appIcon = findViewById(R.id.switch_app_icon);
        appIcon.toggle();
    }


    public void openCheckForUpdates(View v) {
        ota_progressbar.setVisibility(View.VISIBLE);
        findViewById(R.id.swiperefresh).setEnabled(false);
        new TnsOtaApiService(mContext, true);
    }

    public void openDownload(View v) {
        Intent intent = new Intent(mContext, AvailableActivity.class);
        startActivity(intent);
    }

    public void openAddons(View v) {
        Intent intent = new Intent(mContext, AddonActivity.class);
        startActivity(intent);
    }

    public void openDonationPage(View v) {

        boolean payPalLinkAvailable = TnsOta.getDonateLink(mContext).trim().equals("null");
        boolean bitCoinLinkAvailable = TnsOta.getBitCoinLink(mContext).trim().equals("null");
        if (!payPalLinkAvailable && !bitCoinLinkAvailable) {
            mDonateDialog.show();
        } else if (!payPalLinkAvailable) {
            String url = TnsOta.getDonateLink(mContext);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        } else if (!bitCoinLinkAvailable) {
            String url = TnsOta.getBitCoinLink(mContext);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        }
    }

    public void openWebsitePage(View v) {
        String url = TnsOta.getWebsite(mContext);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    public void openForum(View v) {
        String url = TnsOta.getForum(mContext);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    public void openDiscord(View v) {
        String url = TnsOta.getDiscord(mContext);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    public void openGitDiscussions(View v) {
        String url = TnsOta.getGitDiscussion(mContext);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    public void openTelegram(View v) {
        String url = "https://t.me/FreshROMsCommunity";
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    public void openAbout() {
        Intent intent = new Intent(mContext, AboutActivity.class);
        someActivityResultLauncher.launch(intent);
    }


    @SuppressLint("MissingSuperCall")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CHANGE_THEME_REQUEST_CODE) {
            this.recreate();
        }
    }

    public void egg() {
        View about_sys_card = findViewById(R.id.about_sys_card);
        long[] mHits = new long[3];

        about_sys_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
                mHits[mHits.length - 1] = SystemClock.uptimeMillis();
                if (mHits[0] >= (SystemClock.uptimeMillis() - 500)) {
                    ComponentName comp = new ComponentName("com.android.systemui", "com.android.systemui.egg.MLandActivity");

                    Intent intent = new Intent();
                    intent.setComponent(comp);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                    try {
                        startActivity(intent);
                    } catch (Exception e) {
                        Log.e(TAG, "Unable to start activity " + intent.toString());
                    }
                }
            }
        });

    }

    private class CompatibilityTask implements Constants {

        public final String TAG = this.getClass().getSimpleName();

        CompatibilityTask(Context context) {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            String propName = mContext.getResources().getString(R.string.ota_swupdate_prop_api_url);

            executor.execute(() -> {
                boolean isCompatible = SystemProperties.doesPropExist(propName);

                MainActivity.runOnUI(() -> {
                    if (isCompatible) {
                        if (DEBUGGING)
                            Log.d(TAG, "Prop found");
                        new TnsOtaApiService(context, true);
                    } else {
                        if (DEBUGGING)
                            Log.d(TAG, "Prop not found");
                        try {
                            mCompatibilityDialog.show();
                        } catch (WindowManager.BadTokenException ex) {
                            Log.e(TAG, ex.getMessage());
                        }
                    }
                });
            });
        }
    }
}