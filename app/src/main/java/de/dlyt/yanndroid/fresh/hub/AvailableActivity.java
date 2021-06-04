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
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import de.dlyt.yanndroid.fresh.Constants;
import de.dlyt.yanndroid.fresh.R;
import de.dlyt.yanndroid.fresh.database.TnsOta;
import de.dlyt.yanndroid.fresh.database.TnsOtaDownload;
import de.dlyt.yanndroid.fresh.services.OtaHashCheckService;
import de.dlyt.yanndroid.fresh.services.download.DownloadRom;
import de.dlyt.yanndroid.fresh.services.download.DownloadRomProgress;
import de.dlyt.yanndroid.fresh.utils.File;
import de.dlyt.yanndroid.fresh.utils.Notifications;
import de.dlyt.yanndroid.fresh.utils.RecoveryInstall;
import de.dlyt.yanndroid.samsung.ProgressBar;
import de.dlyt.yanndroid.samsung.layout.ToolbarLayout;
import in.uncod.android.bypass.Bypass;

@SuppressLint("StaticFieldLeak")
public class AvailableActivity extends Activity implements Constants, View.OnClickListener {

    public final static String TAG = "AvailableActivity";
    public static ProgressBar mProgressBar;
    public static TextView mProgressCounterText;
    public static TextView mDownloadSpeedTextView;
    public static TextView mMainUpdateHeader;
    public static TextView mPreOtaText;
    public static Handler UIHandler;
    private static Button mDeleteButton;
    private static Button mInstallButton;
    private static Button mDownloadButton;
    private static Button mCancelButton;
    private static Dialog mLoadingDialog;
    private Context mContext;
    private Builder mDeleteDialog;
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

        String timeLeft = String.format("%02d:%02d:%02d", remainingTime / 3600, (remainingTime % 3600) / 60, (remainingTime % 60));

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

        setupDialogs();
        setupProgress(mContext);
        setupChangeLog();
        updateOtaInformation();
        setupMenuToolbar(mContext);
        setupUpdateNameInfo(mContext);

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

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mContext = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ota_available);

        ToolbarLayout toolbarLayout = findViewById(R.id.toolbar_layout);
        toolbarLayout.setExpanded(false, false);
        toolbarLayout.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        toolbarLayout.setSubtitle(getString(R.string.system_name) + " "
                + TnsOta.getReleaseVersion(mContext) + " "
                + TnsOta.getReleaseVariant(mContext));


        mDownloadRom = new DownloadRom();

        mPreOtaText = findViewById(R.id.tv_available_text_pre_ota);
        mProgressBar = findViewById(R.id.bar_available_progress_bar);
        mProgressCounterText = findViewById(R.id.tv_available_progress_counter);
        mDownloadSpeedTextView = findViewById(R.id.tv_available_progress_speed);
        mDeleteButton = findViewById(R.id.menu_available_delete);
        mInstallButton = findViewById(R.id.menu_available_install);
        mDownloadButton = findViewById(R.id.menu_available_download);
        mCancelButton = findViewById(R.id.menu_available_cancel);
        mMainUpdateHeader = findViewById(R.id.tv_available_update_name);

        mDeleteButton.setOnClickListener(this);
        mInstallButton.setOnClickListener(this);
        mDownloadButton.setOnClickListener(this);
        mCancelButton.setOnClickListener(this);

        final View loadingLayout = getLayoutInflater().inflate(R.layout.dialog_full_loading, null);
        mLoadingDialog = new Dialog(mContext, R.style.LargeProgressDialog);
        mLoadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mLoadingDialog.setCancelable(false);
        mLoadingDialog.setContentView(loadingLayout);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.menu_available_delete:
                mDeleteDialog.show();
                break;
            case R.id.menu_available_install:
                new RecoveryInstall(mContext, false);
                break;
            case R.id.menu_available_download:
                mCancelButton.setEnabled(true);
                mProgressBar.setProgress(0);
                download();
                break;
            case R.id.menu_available_cancel:
                mDownloadRom.cancelDownload(mContext);
                mProgressBar.setProgress(0);
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
    }

    private void setupChangeLog() {
        TextView changelogView = (TextView) findViewById(R.id.tv_available_changelog_content);
        Bypass byPass = new Bypass(this);
        String changeLogStr = TnsOta.getChangelog(mContext);
        CharSequence string = byPass.markdownToSpannable(changeLogStr);
        changelogView.setText(string);
        changelogView.setMovementMethod(LinkMovementMethod.getInstance());
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
