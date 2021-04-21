package de.dlyt.yanndroid.fresh.hub;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.SeslProgressBar;

import com.google.android.material.appbar.AppBarLayout;

import de.dlyt.yanndroid.fresh.database.TnsOtaDownload;
import de.dlyt.yanndroid.fresh.utils.File;
import de.dlyt.yanndroid.fresh.utils.Notifications;
import de.dlyt.yanndroid.fresh.R;
import de.dlyt.yanndroid.fresh.services.download.DownloadRom;
import de.dlyt.yanndroid.fresh.services.download.DownloadRomProgress;
import de.dlyt.yanndroid.fresh.utils.RecoveryInstall;
import de.dlyt.yanndroid.fresh.services.OtaHashCheckService;
import de.dlyt.yanndroid.fresh.Constants;
import de.dlyt.yanndroid.fresh.database.TnsOta;
import de.dlyt.yanndroid.fresh.utils.Tools;
import in.uncod.android.bypass.Bypass;

@SuppressLint("StaticFieldLeak")
public class AvailableActivity extends Activity implements Constants, View.OnClickListener {

    public final static String TAG = "AvailableActivity";
    public static SeslProgressBar mProgressBar;
    public static TextView mProgressCounterText;
    public static TextView mDownloadSpeedTextView;
    public static TextView mMainUpdateHeader;
    public static TextView mPreOtaText;
    public static Handler UIHandler;
    private static Button mDeleteButton;
    private static Button mInstallButton;
    private static Button mDownloadButton;
    private static Button mCancelButton;
    private Context mContext;
    private Builder mDeleteDialog;
    private Builder mRebootDialog;
    private Builder mRebootManualDialog;
    private static Dialog mLoadingDialog;
    private DownloadRom mDownloadRom;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(DOWNLOAD_ROM_COMPLETE)) {
                if (!TnsOtaDownload.getDownloadFinished(context)) {
                    mDownloadRom.cancelDownload(context);
                }

                TnsOtaDownload.setIsDownloadRunning(context, false);
                Notifications.cancelUpdateNotification(context);

                setupProgress(context);
                setupProgress(context);
                setupMenuToolbar(context);
                setupUpdateNameInfo(context);
            }
        }
    };

    public static void setupProgress(Context context) {
        if (DEBUGGING)
            Log.d(TAG, "Setting up Progress Bars");
        boolean downloadFinished = TnsOtaDownload.getDownloadFinished(context);
        if (downloadFinished) {
            if (DEBUGGING)
                Log.d(TAG, "Download finished. Setting up Progress Bars accordingly.");
            String ready = context.getResources().getString(R.string.available_ready_to_install);

            if (mProgressCounterText != null) {
                mProgressCounterText.setText(ready);
            }
            if (mProgressBar != null) {
                mProgressBar.setProgress(100);
            }
        }
    }

    public static void updateProgress(Context context, int progress, int downloaded, int total) {
        Long startTime = TnsOta.getStartTime(context);
        Long currentTime = System.currentTimeMillis();

        mProgressBar.setProgress(progress);

        int downloadSpeed = (downloaded / (int) (currentTime - startTime));
        String localizedSpeed = File.formatDataFromBytes(downloadSpeed * 1000);
        long remainingTime = downloadSpeed != 0 ? ((total - downloaded) / downloadSpeed) / 1000 : 0;

        String timeLeft = String.format("%02d:%02d:%02d", remainingTime / 3600,
                (remainingTime % 3600) / 60, (remainingTime % 60));

        mProgressCounterText.setText(context.getString(R.string.available_time_remaining, timeLeft));
        mDownloadSpeedTextView.setText(context.getString(R.string.available_download_speed, localizedSpeed));
    }

    public static void setupMenuToolbar(Context context) {
        boolean downloadFinished = TnsOtaDownload.getDownloadFinished(context);
        boolean downloadIsRunning = TnsOtaDownload.getIsDownloadOnGoing(context);

        mDeleteButton.setEnabled(false);
        mDeleteButton.setVisibility(View.GONE);
        setupUpdateNameInfo(context);

        if (!downloadFinished) { // Download hasn't finished
            if (downloadIsRunning) {
                // Download is still running
                mDownloadButton.setVisibility(View.GONE);
                mCancelButton.setVisibility(View.VISIBLE);
                mInstallButton.setVisibility(View.VISIBLE);
                mInstallButton.setEnabled(false);
            } else {
                // Download is not running and hasn't finished
                mDownloadButton.setVisibility(View.VISIBLE);
                mCancelButton.setVisibility(View.VISIBLE);
                mInstallButton.setVisibility(View.GONE);
                mCancelButton.setEnabled(false);
            }
        } else { // Download has finished
            String md5 = TnsOta.getMd5(context);
            boolean md5HasRun = TnsOtaDownload.getHasMD5Run(context);
            boolean md5Passed = TnsOtaDownload.getMD5Passed(context);

            if (!md5.equals("null")) {
                // Is MD5 being used?
                mInstallButton.setVisibility(View.VISIBLE);
                mDeleteButton.setVisibility(View.VISIBLE);
                mDownloadButton.setVisibility(View.GONE);
                mCancelButton.setVisibility(View.GONE);
                mDeleteButton.setEnabled(true);

                if (md5HasRun && md5Passed) {
                    mInstallButton.setEnabled(true);
                } else if (md5HasRun) {
                    mInstallButton.setEnabled(true);
                } else {
                    mInstallButton.setEnabled(false);
                    mLoadingDialog.show();
                    new OtaHashCheckService(context, mLoadingDialog);
                }
            } else {
                mInstallButton.setVisibility(View.VISIBLE);
                mDeleteButton.setVisibility(View.VISIBLE);
                mInstallButton.setEnabled(true);
                mDeleteButton.setEnabled(true);
            }
        }
    }

    public static void runOnUI(Runnable runnable) {
        UIHandler.post(runnable);
    }

    @SuppressWarnings("deprecation")
    private void updateOtaInformation() {
        String space = " ";
        String separator_open = " (";
        String separator_close = ") ";

        //ROM version
        TextView otaVersion = (TextView) findViewById(R.id.tv_update_rom_version);
        String otaVersionTitle = getApplicationContext().getResources().getString(R.string
                .main_ota_version) + " ";
        String otaVersionName = TnsOta.getReleaseVersion(mContext) + " ";
        String otaBranchString = TnsOta.getReleaseVariant(mContext);
        String otaTypeString = TnsOta.getReleaseType(mContext);
        String otaVersionBranch = otaBranchString.substring(0, 1).toUpperCase() + otaBranchString.substring(1).toLowerCase();
        otaVersion.setText(Html.fromHtml(otaVersionTitle + otaVersionName + otaVersionBranch + separator_open +
                otaTypeString + separator_close));

        //ROM size
        TextView otaSize = (TextView) findViewById(R.id.tv_update_rom_size);
        String otaSizeTitle = getApplicationContext().getResources().getString(R.string
                .main_ota_size) + " ";
        long otaFileSize = TnsOta.getFileSize(mContext);
        String otaSizeActual = File.formatDataFromBytes(otaFileSize);
        otaSize.setText(Html.fromHtml(otaSizeTitle + otaSizeActual));

        //ROM security patch
        TextView otaSplVersion = (TextView) findViewById(R.id.tv_ota_android_spl);
        String otaSplTitle = getApplicationContext().getResources().getString(R.string
                .main_rom_spl) + " ";
        String otaSplActual = TnsOta.renderAndroidSpl(TnsOta.getSpl(mContext));
        otaSplVersion.setText(Html.fromHtml(otaSplTitle + otaSplActual));
    }

    @Override
    public void onStart() {
        super.onStart();
        UIHandler = new Handler(Looper.getMainLooper());
        this.registerReceiver(mReceiver, new IntentFilter(DOWNLOAD_ROM_COMPLETE));

        String downloadSpeed = "0B";
        long remainingTime = 0;

        String timeLeft = String.format("%02d:%02d:%02d", remainingTime / 3600,
                (remainingTime % 3600) / 60, (remainingTime % 60));

        mProgressCounterText.setText(getString(R.string.available_time_remaining, timeLeft));
        mDownloadSpeedTextView.setText(getString(R.string.available_download_speed, downloadSpeed));
        mProgressBar.setProgress(0);
        Notifications.cancelUpdateNotification(mContext);

        if (TnsOtaDownload.getIsDownloadOnGoing(mContext)) {
            // If the activity has already been run, and the download started
            // Then start updating the progress bar again
            if (DEBUGGING)
                Log.d(TAG, "Starting progress updater");
            DownloadManager downloadManager = (DownloadManager) mContext.getSystemService(Context
                    .DOWNLOAD_SERVICE);
            new DownloadRomProgress(mContext, downloadManager);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        this.unregisterReceiver(mReceiver);
    }

    public void setSubtitle(String subtitle) {
        TextView expanded_subtitle = findViewById(R.id.expanded_subtitle);
        expanded_subtitle.setText(subtitle);
    }

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mContext = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ota_available);

        initToolbar();
        settilte(getString(R.string.system_settings_plugin_title));
        setSubtitle(getString(R.string.system_name) + " "
                + TnsOta.getReleaseVersion(mContext) + " "
                + TnsOta.getReleaseVariant(mContext));

        mDownloadRom = new DownloadRom();

        mPreOtaText = (TextView) findViewById(R.id.tv_available_text_pre_ota);
        mProgressBar = (SeslProgressBar) findViewById(R.id.bar_available_progress_bar);
        mProgressCounterText = (TextView) findViewById(R.id.tv_available_progress_counter);
        mDownloadSpeedTextView = (TextView) findViewById(R.id.tv_available_progress_speed);
        mDeleteButton = (Button) findViewById(R.id.menu_available_delete);
        mInstallButton = (Button) findViewById(R.id.menu_available_install);
        mDownloadButton = (Button) findViewById(R.id.menu_available_download);
        mCancelButton = (Button) findViewById(R.id.menu_available_cancel);
        mMainUpdateHeader = (TextView) findViewById(R.id.tv_available_update_name);

        mDeleteButton.setOnClickListener(this);
        mInstallButton.setOnClickListener(this);
        mDownloadButton.setOnClickListener(this);
        mCancelButton.setOnClickListener(this);

        final View loadingLayout = getLayoutInflater().inflate(R.layout.dialog_full_loading, null);
        mLoadingDialog = new Dialog(mContext, R.style.LargeProgressDialog);
        mLoadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mLoadingDialog.setCancelable(false);
        mLoadingDialog.setContentView(loadingLayout);

        setupDialogs();
        setupProgress(mContext);
        setupChangeLog();
        updateOtaInformation();
        setupMenuToolbar(mContext);
        setupUpdateNameInfo(mContext);
    }

    public void initToolbar() {
        /** Def */
        androidx.appcompat.widget.Toolbar toolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
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
        AppBar.setExpanded(false);

        /**Back*/
        ImageView navigationIcon = findViewById(R.id.navigationIcon);
        View navigationIcon_Badge = findViewById(R.id.navigationIcon_new_badge);
        navigationIcon_Badge.setVisibility(View.GONE);
        navigationIcon.setImageResource(R.drawable.ic_back);
        navigationIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.menu_available_delete:
                mDeleteDialog.show();
                break;
            case R.id.menu_available_install:
                if (!Tools.isRootAvailable()) {
                    mRebootManualDialog.show();
                } else {
                    mLoadingDialog.show();
                    new RecoveryInstall(mContext, false);
                }
                break;
            case R.id.menu_available_download:
                mCancelButton.setEnabled(true);
                mProgressBar.setProgress(0);
                download();
                break;
            case R.id.menu_available_cancel:
                mDownloadRom.cancelDownload(mContext);
                Intent send = new Intent(DOWNLOAD_ROM_COMPLETE);
                mContext.sendBroadcast(send);
                break;
        }
    }

    private void setupDialogs() {
        mDeleteDialog = new Builder(mContext, R.style.AlertDialogStyle);
        mDeleteDialog.setTitle(R.string.are_you_sure)
                .setMessage(R.string.available_delete_confirm_message)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    // Proceed to delete the file, and reset most variables
                    // and layouts

                    File.deleteFile(TnsOta.getFullFile(mContext)); // Delete the file
                    TnsOtaDownload.setHasMD5Run(mContext, false); // MD5 check hasn't been run
                    TnsOtaDownload.setDownloadFinished(mContext, false);
                    setupProgress(mContext); // Progress goes back to 0
                    setupMenuToolbar(mContext); // Reset options menu
                    setupUpdateNameInfo(mContext); // Update name info
                }).setNegativeButton(R.string.cancel, null);

        mRebootDialog = new Builder(mContext, R.style.AlertDialogStyle);
        mRebootDialog.setTitle(R.string.are_you_sure)
                .setMessage(R.string.available_reboot_confirm)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    new RecoveryInstall(mContext, false);
                }).setNegativeButton(R.string.cancel, null);

        mRebootManualDialog = new Builder(mContext, R.style.AlertDialogStyle);
        mRebootManualDialog.setTitle(R.string.available_reboot_manual_title)
                .setMessage(R.string.available_reboot_manual_message)
                .setPositiveButton(R.string.cancel, null);
    }

    private void setupChangeLog() {
        TextView changelogView = (TextView) findViewById(R.id.tv_available_changelog_content);
        Bypass byPass = new Bypass(this);
        String changeLogStr = TnsOta.getChangelog(mContext);
        CharSequence string = byPass.markdownToSpannable(changeLogStr);
        changelogView.setText(string);
        changelogView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private static void setupUpdateNameInfo(Context context) {
        boolean downloadFinished = TnsOtaDownload.getDownloadFinished(context);
        boolean downloadIsRunning = TnsOtaDownload.getIsDownloadOnGoing(context);

        if (!downloadFinished) {
            if (downloadIsRunning) {
                mMainUpdateHeader.setText(R.string.available_update_downloading);
                mPreOtaText.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.VISIBLE);
                mProgressCounterText.setVisibility(View.VISIBLE);
                mDownloadSpeedTextView.setVisibility(View.VISIBLE);
            } else {
                mMainUpdateHeader.setText(R.string.available_update_install_info);
                mPreOtaText.setText(R.string.available_update_install_info_desc);
                mPreOtaText.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.GONE);
                mProgressCounterText.setVisibility(View.GONE);
                mDownloadSpeedTextView.setVisibility(View.GONE);
            }
        } else { // Download has finished
            String md5 = TnsOta.getMd5(context);
            boolean md5HasRun = TnsOtaDownload.getHasMD5Run(context);
            boolean md5Passed = TnsOtaDownload.getMD5Passed(context);

            mProgressBar.setVisibility(View.GONE);
            mProgressCounterText.setVisibility(View.GONE);
            mDownloadSpeedTextView.setVisibility(View.GONE);

            mPreOtaText.setText(R.string.available_preinstall_notice);
            mPreOtaText.setVisibility(View.VISIBLE);
            mMainUpdateHeader.setText(context.getResources().getString(R.string.available_update_install_ready,
                    TnsOta.getReleaseVersion(context), TnsOta.getReleaseVariant(context)));

            if (!md5.equals("null")) {
                if (md5HasRun && md5Passed) {
                    mPreOtaText.setText(R.string.available_preinstall_notice);
                } else if (md5HasRun) {
                    mPreOtaText.setText(R.string.available_preinstall_notice_nohash);
                }

            } else {
                mProgressBar.setVisibility(View.GONE);
                mProgressCounterText.setVisibility(View.GONE);
                mDownloadSpeedTextView.setVisibility(View.GONE);
            }
        }
    }

    private void download() {
        String httpUrl = TnsOta.getHttpUrl(mContext);
        String directUrl = TnsOta.getDirectUrl(mContext);
        String error = getResources().getString(R.string.available_url_error);

        // We're good, open links or start downloads
        if (directUrl.equals("null") && !httpUrl.equals("null")) {
            if (DEBUGGING)
                Log.d(TAG, "HTTP link opening");
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(httpUrl));
            startActivity(intent);
        } else if (directUrl.equals("null") && httpUrl.equals("null")) {
            if (DEBUGGING)
                Log.e(TAG, "No links found");
            Toast.makeText(mContext, error, Toast.LENGTH_LONG).show();
        } else {
            if (DEBUGGING)
                Log.d(TAG, "Downloading via DownloadManager");
            mDownloadRom.startDownload(mContext);
            TnsOta.setStartTime(mContext, System.currentTimeMillis());

            setupMenuToolbar(mContext); // Reset options menu
            setupUpdateNameInfo(mContext);
        }
    }

}
