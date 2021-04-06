package de.dlyt.yanndroid.freshapp.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SeslProgressBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.card.MaterialCardView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.dlyt.yanndroid.freshapp.R;
import de.dlyt.yanndroid.freshapp.tasks.LoadUpdateManifest;
import de.dlyt.yanndroid.freshapp.utils.Constants;
import de.dlyt.yanndroid.freshapp.utils.Preferences;
import de.dlyt.yanndroid.freshapp.utils.RomUpdate;
import de.dlyt.yanndroid.freshapp.utils.Tools;
import de.dlyt.yanndroid.freshapp.utils.Utils;

public class MainActivity extends AppCompatActivity implements Constants,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private static final int CHANGE_THEME_REQUEST_CODE = 2;
    private static final boolean ENABLE_COMPATIBILITY_CHECK = true;
    public static boolean hasRoot;
    @SuppressLint("StaticFieldLeak")
    private static SeslProgressBar mProgressBar;
    @SuppressLint("StaticFieldLeak")
    private static Context mContext;
    private final String TAG = this.getClass().getSimpleName();
    String omc_url = "https://tiny.cc/FRSH-OMC";
    String feedback_url = "https://tiny.cc/FRSH-Feedback";
    WebView webView;
    ProgressBar web_progressbar;
    private Builder mCompatibilityDialog;
    private Builder mDonateDialog;
    private Builder mPlayStoreDialog;
    private Builder mRebootDialog;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(MANIFEST_LOADED)) {
                updateAllLayouts();

                if (RomUpdate.getUpdateAvailability(mContext) && !Utils.isUpdateIgnored(mContext)) {
                    Utils.setupNotification(mContext, RomUpdate.getReleaseVersion(mContext), RomUpdate.getReleaseVariant(mContext));
                }
            }
        }
    };

    public static void updateProgress(int progress) {
        if (mProgressBar != null) {
            mProgressBar.setProgress(progress);
        }
    }

    private boolean updateAllLayouts() {
        try {
            updateDonateLinkLayout();
            updateAddonsLayout();
            updateRomInformation();
            updateRomUpdateLayouts();
            updateWebsiteLayout();
            return true;
        } catch (Exception e) {
            // Suppress warning
        }
        return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mContext = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ota_main);

        webView = findViewById(R.id.webview);
        web_progressbar = findViewById(R.id.web_progressbar);

        initToolbar();
        initDrawer();
        settilte(getString(R.string.update));
        initWebView();

        Utils.setupNotificationChannel(mContext);
        Utils.setupJobScheduler(mContext, !Preferences.getBackgroundService(mContext));

        boolean firstRun = Preferences.getFirstRun(mContext);
        if (firstRun) {
            Preferences.setFirstRun(mContext, false);
        }

        // Create download directories if needed
        File installAfterFlashDir = new File(SD_CARD
                + File.separator
                + OTA_DOWNLOAD_DIR
                + File.separator
                + INSTALL_AFTER_FLASH_DIR);
        boolean created = installAfterFlashDir.mkdirs();
        if (!created) Log.e(TAG, "Could not create installAfterFlash directory...");

        createDialogs();

        // Check the correct build prop values are installed
        // Also executes the manifest/update check

        if (!Utils.isConnected(mContext)) {
            Builder notConnectedDialog = new Builder(mContext, R.style.AlertDialogStyle);
            notConnectedDialog.setTitle(R.string.main_not_connected_title)
                    .setMessage(R.string.main_not_connected_message)
                    .setCancelable(false)
                    .setPositiveButton(R.string.ok, (dialog, which) -> ((Activity) mContext)
                            .finish())
                    .show();
        } else {
            if (ENABLE_COMPATIBILITY_CHECK) new CompatibilityTask(mContext).execute();
        }

        // Has the download already completed?
        Utils.setHasFileDownloaded(mContext);

        // Update the layouts
        updateDonateLinkLayout();
        updateAddonsLayout();
        updateRomInformation();
        updateRomUpdateLayouts();
        updateWebsiteLayout();
        refreshDrawer();

        // But check permissions first - download will be started in the callback
        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(),
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
        new checkRoot().execute("");
    }


    public void initDrawer() {
        View content = findViewById(R.id.main_content);
        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        View drawer = findViewById(R.id.drawer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);


        ViewGroup.LayoutParams layoutParams = drawer.getLayoutParams();
        layoutParams.width = (int) ((double) this.getResources().getDisplayMetrics().widthPixels / 1.19);
        drawerLayout.setScrimColor(ContextCompat.getColor(getBaseContext(), R.color.drawer_dim_color));
        drawerLayout.setDrawerElevation(0);

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.opend, R.string.closed) {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                float slideX = drawerView.getWidth() * slideOffset;
                content.setTranslationX(slideX);
            }
        };
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(drawer, true);
            }
        });


        /**Items*/
        View drawer_settings = findViewById(R.id.drawer_settings);
        drawer_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSettings(null);
                drawerLayout.closeDrawer(drawer, true);
            }
        });


        View feedback_content = findViewById(R.id.feedback_content);
        View ota_content = findViewById(R.id.ota_content);


        View drawer_update = findViewById(R.id.drawer_update);
        drawer_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settilte(getString(R.string.update));
                ota_content.setVisibility(View.VISIBLE);
                feedback_content.setVisibility(View.GONE);
                drawerLayout.closeDrawer(drawer, true);
            }
        });


        View drawer_feedback = findViewById(R.id.drawer_feedback);
        drawer_feedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ota_content.setVisibility(View.GONE);
                feedback_content.setVisibility(View.VISIBLE);
                web_progressbar.setVisibility(View.VISIBLE);
                webView.loadUrl(feedback_url);
                settilte(getString(R.string.feedback));
                drawerLayout.closeDrawer(drawer, true);
            }
        });

        View drawer_omc_request = findViewById(R.id.drawer_omc_request);
        drawer_omc_request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ota_content.setVisibility(View.GONE);
                feedback_content.setVisibility(View.VISIBLE);
                web_progressbar.setVisibility(View.VISIBLE);
                webView.loadUrl(omc_url);
                settilte(getString(R.string.omc_request));
                drawerLayout.closeDrawer(drawer, true);
            }
        });

        View drawer_fresh_group = findViewById(R.id.drawer_fresh_group);
        drawer_fresh_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/FreshROMs")));
            }
        });

        View drawer_community = findViewById(R.id.drawer_community);
        drawer_community.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/FreshROMsCommunity")));
            }
        });

        View drawer_discord_group = findViewById(R.id.drawer_discord_group);
        drawer_discord_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(RomUpdate.getDiscord(mContext))));
            }
        });


        View drawer_reboot = findViewById(R.id.drawer_reboot);
        drawer_reboot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRebootDialog.show();
            }
        });




        View drawer_test = findViewById(R.id.drawer_resolution);
        drawer_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent().setClass(getApplicationContext(), Screen_Resolution.class));
            }
        });

    }

    public void refreshDrawer(){
        View drawer_discord_group = findViewById(R.id.drawer_discord_group);
        String discord_url = RomUpdate.getDiscord(mContext);
        if (discord_url.trim().equals("null")) {
            drawer_discord_group.setVisibility(View.GONE);
        }
    }


    public void initWebView() {
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                web_progressbar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                web_progressbar.setVisibility(View.GONE);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                web_progressbar.setVisibility(View.GONE);
            }
        });
        webView.loadUrl(feedback_url);
    }


    public void initToolbar() {
        /** Def */
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        AppBarLayout AppBar = findViewById(R.id.app_bar);

        TextView expanded_title = findViewById(R.id.expanded_title);
        TextView expanded_subtitle = findViewById(R.id.expanded_subtitle);
        TextView collapsed_title = findViewById(R.id.collapsed_title);

        /** 1/3 of the Screen */
        ViewGroup.LayoutParams layoutParams = AppBar.getLayoutParams();
        layoutParams.height = (int) ((double) this.getResources().getDisplayMetrics().heightPixels / 2.6);


        /** Collapsing */
        AppBar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                float percentage = (AppBar.getY() / AppBar.getTotalScrollRange());
                expanded_title.setAlpha(1 - (percentage * 2 * -1));
                expanded_subtitle.setAlpha(1 - (percentage * 2 * -1));
                collapsed_title.setAlpha(percentage * -1);
            }
        });


    }


    public void settilte(String title) {
        TextView expanded_title = findViewById(R.id.expanded_title);
        TextView collapsed_title = findViewById(R.id.collapsed_title);
        expanded_title.setText(title);
        collapsed_title.setText(title);
    }


    @Override
    public void onStart() {
        super.onStart();
        this.registerReceiver(mReceiver, new IntentFilter(MANIFEST_LOADED));

    }

    @Override
    public void onStop() {
        super.onStop();
        this.unregisterReceiver(mReceiver);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length <= 0 || grantResults[0]
                        != PackageManager.PERMISSION_GRANTED) {
                    new Builder(this, R.style.AlertDialogStyle)
                            .setTitle(R.string.permission_not_granted_dialog_title)
                            .setMessage(R.string.permission_not_granted_dialog_message)
                            .setPositiveButton(R.string.dialog_ok, (dialog, which) ->
                                    MainActivity.this.finish()).show();
                    return;
                }
                break;
            }
        }
    }

    private void createDialogs() {
        //reboot dialog
        mRebootDialog = new Builder(mContext, R.style.AlertDialogStyle);
        mRebootDialog.setTitle(R.string.are_you_sure)
                .setMessage(R.string.available_reboot_confirm)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    Tools.recovery(mContext);
                }).setNegativeButton(R.string.cancel, null);


        // Compatibility Dialog
        mCompatibilityDialog = new Builder(mContext, R.style.AlertDialogStyle);
        mCompatibilityDialog.setCancelable(false);
        mCompatibilityDialog.setTitle(R.string.main_not_compatible_title);
        mCompatibilityDialog.setMessage(R.string.main_not_compatible_message);
        mCompatibilityDialog.setPositiveButton(R.string.ok, (dialog, which) -> MainActivity.this.finish());

        mCompatibilityDialog.setNegativeButton("debug", (dialog, which) -> dialog.cancel()); //todo: must be disabled at release

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
                        url = RomUpdate.getDonateLink(mContext);
                    } else {
                        url = RomUpdate.getBitCoinLink(mContext);
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
    private void updateRomUpdateLayouts() {
        View updateAvailable;
        View updateNotAvailable;
        updateAvailable = findViewById(R.id.layout_main_update_available);
        updateNotAvailable = findViewById(R.id.layout_main_no_update_available);
        updateAvailable.setVisibility(View.GONE);
        updateNotAvailable.setVisibility(View.GONE);

        TextView updateAvailableSummary = (TextView) findViewById(R.id.main_tv_update_available_summary);
        TextView updateNotAvailableSummary = (TextView) findViewById(R.id.main_tv_no_update_available_summary);

        mProgressBar = (SeslProgressBar) findViewById(R.id.bar_main_progress_bar);
        mProgressBar.setVisibility(View.GONE);

        // Update is available
        if (RomUpdate.getUpdateAvailability(mContext) || (!RomUpdate.getUpdateAvailability(mContext)) && Utils.isUpdateIgnored(mContext)) {
            updateAvailable.setVisibility(View.VISIBLE);
            TextView updateAvailableTitle = (TextView) findViewById(R.id.main_tv_update_available_title);

            if (Preferences.getDownloadFinished(mContext)) { //  Update already finished?
                updateAvailableTitle.setText(getResources().getString(R.string
                        .main_update_finished));
                String htmlColorOpen;
                htmlColorOpen = "<font color='" + getResources().getColor(R.color.item_color) + "'>";
                String htmlColorClose = "</font>";
                String updateSummary = RomUpdate.getFilename(mContext)
                        + "<br />"
                        + htmlColorOpen
                        + getResources().getString(R.string.main_download_completed_details)
                        + htmlColorClose;
                updateAvailableSummary.setText(Html.fromHtml(updateSummary));
            } else if (Preferences.getIsDownloadOnGoing(mContext)) {
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
                String htmlColorOpen;
                htmlColorOpen = "<font color='" + getResources().getColor(R.color.item_color) + "'>";
                String htmlColorClose = "</font>";
                String updateSummary = RomUpdate.getFilename(mContext)
                        + "<br />"
                        + htmlColorOpen
                        + getResources().getString(R.string.main_tap_to_download)
                        + htmlColorClose;
                updateAvailableSummary.setText(Html.fromHtml(updateSummary));
            }
        } else {
            updateNotAvailable.setVisibility(View.VISIBLE);

            boolean is24 = DateFormat.is24HourFormat(mContext);
            Date now = new Date();
            Locale locale = Locale.getDefault();
            String time;

            if (is24) {
                time = new SimpleDateFormat("MMMM d - HH:mm", locale).format(now);
            } else {
                time = new SimpleDateFormat("MMMM d - hh:mm a", locale).format(now);
            }

            Preferences.setUpdateLastChecked(this, time);
            String lastChecked = getString(R.string.main_last_checked);
            updateNotAvailableSummary.setText(String.format("%s%s", lastChecked, time));
        }
    }

    private void updateAddonsLayout() {
        MaterialCardView addonsLink = (MaterialCardView) findViewById(R.id.layout_main_addons);
        addonsLink.setVisibility(View.GONE);

        if (RomUpdate.getAddonsCount(mContext) > 0) {
            addonsLink.setVisibility(View.VISIBLE);
        }
    }

    private void updateDonateLinkLayout() {
        MaterialCardView donateLink = (MaterialCardView) findViewById(R.id.layout_main_dev_donate_link);
        donateLink.setVisibility(View.GONE);

        if (!(RomUpdate.getDonateLink(mContext).trim().equals("null"))
                || !(RomUpdate.getBitCoinLink(mContext).trim().equals("null"))) {
            donateLink.setVisibility(View.VISIBLE);
        }
    }

    private void updateWebsiteLayout() {
        MaterialCardView webLink = (MaterialCardView) findViewById(R.id.layout_main_dev_website);
        webLink.setVisibility(View.GONE);

        if (!RomUpdate.getWebsite(mContext).trim().equals("null")) {
            webLink.setVisibility(View.VISIBLE);
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
        String romVersionName = Utils.getProp(getResources().getString(R.string.ota_swupdate_prop_release)) + " ";
        String romBranchString = Utils.getProp(getResources().getString(R.string.ota_swupdate_prop_branch));
        String romVersionString = Utils.getProp(getResources().getString(R.string.ota_swupdate_prop_version));
        if (!romBranchString.isEmpty()) {
            String romVersionBranch = romBranchString.substring(0, 1).toUpperCase() + romBranchString.substring(1).toLowerCase();
            romVersion.setText(Html.fromHtml(htmlColorOpen + romVersionTitle + romVersionName + romVersionBranch + separator_open + romVersionString + separator_close + htmlColorClose));
        }

        //ROM date
        TextView romDate = (TextView) findViewById(R.id.tv_main_rom_date);
        String romDateTitle = getApplicationContext().getResources().getString(R.string
                .main_rom_build_date) + " ";
        String romDateActual = Utils.getProp(getResources().getString(R.string.ota_swupdate_prop_date));
        romDate.setText(Html.fromHtml(romDateTitle + htmlColorOpen + romDateActual +
                htmlColorClose));

        //ROM security patch
        TextView splVersion = (TextView) findViewById(R.id.tv_main_android_spl);
        String romSplTitle = getApplicationContext().getResources().getString(R.string
                .main_rom_spl) + " ";
        String romSplActual = Utils.renderAndroidSpl(Utils.getProp("ro.build.version.security_patch"));
        splVersion.setText(Html.fromHtml(romSplTitle + htmlColorOpen + romSplActual +
                htmlColorClose));
    }

    public void openCheckForUpdates(View v) {
        new LoadUpdateManifest(mContext, true).execute();
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

        boolean payPalLinkAvailable = RomUpdate.getDonateLink(mContext).trim().equals("null");
        boolean bitCoinLinkAvailable = RomUpdate.getBitCoinLink(mContext).trim().equals("null");
        if (!payPalLinkAvailable && !bitCoinLinkAvailable) {
            mDonateDialog.show();
        } else if (!payPalLinkAvailable) {
            String url = RomUpdate.getDonateLink(mContext);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        } else if (!bitCoinLinkAvailable) {
            String url = RomUpdate.getBitCoinLink(mContext);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        }
    }

    public void openWebsitePage(View v) {
        String url = RomUpdate.getWebsite(mContext);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    public void openSettings(View v) {
        Intent intent = new Intent(mContext, SettingsActivity.class);
        startActivityForResult(intent, CHANGE_THEME_REQUEST_CODE);
    }


    @SuppressLint("MissingSuperCall")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CHANGE_THEME_REQUEST_CODE) {
            this.recreate();
        }
    }

    static class checkRoot extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... sUrl) {
            hasRoot = Tools.isRootAvailable();
            return null;
        }
    }

    private class CompatibilityTask extends AsyncTask<Void, Boolean, Boolean> implements Constants {

        public final String TAG = this.getClass().getSimpleName();

        private Context mContext;
        private String mPropName;

        CompatibilityTask(Context context) {
            mContext = context;
            mPropName = mContext.getResources().getString(R.string.ota_swupdate_prop_api_url);
        }

        @Override
        protected Boolean doInBackground(Void... v) {
            return Utils.doesPropExist(mPropName);
        }

        @Override
        protected void onPostExecute(Boolean result) {

            if (result) {
                if (DEBUGGING)
                    Log.d(TAG, "Prop found");
                new LoadUpdateManifest(mContext, true).execute();
            } else {
                if (DEBUGGING)
                    Log.d(TAG, "Prop not found");
                try {
                    mCompatibilityDialog.show();
                } catch (WindowManager.BadTokenException ex) {
                    Log.e(TAG, ex.getMessage());
                }
            }
            super.onPostExecute(result);
        }
    }
}
