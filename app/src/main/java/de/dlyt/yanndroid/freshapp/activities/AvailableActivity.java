package de.dlyt.yanndroid.freshapp.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.SeslProgressBar;

import com.google.android.material.appbar.AppBarLayout;

import in.uncod.android.bypass.Bypass;
import de.dlyt.yanndroid.freshapp.R;
import de.dlyt.yanndroid.freshapp.download.DownloadRom;
import de.dlyt.yanndroid.freshapp.download.DownloadRomProgress;
import de.dlyt.yanndroid.freshapp.tasks.GenerateRecoveryScript;
import de.dlyt.yanndroid.freshapp.utils.Constants;
import de.dlyt.yanndroid.freshapp.utils.Preferences;
import de.dlyt.yanndroid.freshapp.utils.RomUpdate;
import de.dlyt.yanndroid.freshapp.utils.Tools;
import de.dlyt.yanndroid.freshapp.utils.Utils;

@SuppressLint("StaticFieldLeak")
public class AvailableActivity extends Activity implements Constants, View.OnClickListener {

    public final static String TAG = "AvailableActivity";
    public static SeslProgressBar mProgressBar;
    public static TextView mProgressCounterText;
    private static Button mCheckMD5Button;
    private static Button mDeleteButton;
    private static Button mInstallButton;
    private static Button mDownloadButton;
    private static Button mCancelButton;
    private Context mContext;
    private Builder mDeleteDialog;
    private Builder mRebootDialog;
    private Builder mRebootManualDialog;
    private Builder mNetworkDialog;
    private DownloadRom mDownloadRom;

    public static void setupProgress(Context context) {
        if (DEBUGGING)
            Log.d(TAG, "Setting up Progress Bars");
        boolean downloadFinished = Preferences.getDownloadFinished(context);
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
        } else {
            if (DEBUGGING)
                Log.d(TAG, "Download not finished/started. Setting Progress Bars to default.");
            int fileSize = RomUpdate.getFileSize(context);
            String fileSizeStr = Utils.formatDataFromBytes(fileSize);
            if (mProgressCounterText != null) {
                mProgressCounterText.setText(fileSizeStr);
            }
            if (mProgressBar != null) {
                mProgressBar.setProgress(0);
            }
        }
    }

    public static void updateProgress(int progress, int downloaded, int total) {
        mProgressBar.setProgress(progress);
        mProgressCounterText.setText(
                Utils.formatDataFromBytes(downloaded) +
                        "/" +
                        Utils.formatDataFromBytes(total));
    }

    public static void setupMenuToolbar(Context context) {
        boolean downloadFinished = Preferences.getDownloadFinished(context);
        boolean downloadIsRunning = Preferences.getIsDownloadOnGoing(context);
        boolean md5HasRun = Preferences.getHasMD5Run(context);
        boolean md5Passed = Preferences.getMD5Passed(context);

        mDeleteButton.setEnabled(false);
        mCheckMD5Button.setEnabled(false);

        if (!downloadFinished) { // Download hasn't finished
            if (downloadIsRunning) {
                // Download is still running
                mDownloadButton.setVisibility(View.GONE);
                mCancelButton.setVisibility(View.VISIBLE);
                mInstallButton.setVisibility(View.GONE);
            } else {
                // Download is not running and hasn't finished
                mDownloadButton.setVisibility(View.VISIBLE);
                mCancelButton.setVisibility(View.GONE);
                mInstallButton.setVisibility(View.GONE);
            }
        } else { // Download has finished
            String md5 = RomUpdate.getMd5(context);
            if (!md5.equals("null")) {
                // Is MD5 being used?
                if (md5HasRun && md5Passed) {
                    mCheckMD5Button.setEnabled(false);
                    mCheckMD5Button.setText(R.string.available_md5_ok);
                } else if (md5HasRun) {
                    mCheckMD5Button.setEnabled(false);
                    mCheckMD5Button.setText(R.string.available_md5_failed);
                } else {
                    mCheckMD5Button.setEnabled(true);
                }
            } else {
                mCheckMD5Button.setClickable(false);
            }
            mDeleteButton.setEnabled(true);
            mDownloadButton.setVisibility(View.GONE);
            mCancelButton.setVisibility(View.GONE);
            mInstallButton.setVisibility(View.VISIBLE);
        }
    }

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mContext = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ota_available);

        initToolbar();
        settilte("Update");

        mDownloadRom = new DownloadRom();

        mProgressBar = (SeslProgressBar) findViewById(R.id.bar_available_progress_bar);
        mProgressCounterText = (TextView) findViewById(R.id.tv_available_progress_counter);
        mCheckMD5Button = (Button) findViewById(R.id.menu_available_check_md5);
        mDeleteButton = (Button) findViewById(R.id.menu_available_delete);
        mInstallButton = (Button) findViewById(R.id.menu_available_install);
        mDownloadButton = (Button) findViewById(R.id.menu_available_download);
        mCancelButton = (Button) findViewById(R.id.menu_available_cancel);
        Button mChangelogButton = (Button) findViewById(R.id.menu_available_changelog);

        mCheckMD5Button.setOnClickListener(this);
        mDeleteButton.setOnClickListener(this);
        mInstallButton.setOnClickListener(this);
        mDownloadButton.setOnClickListener(this);
        mCancelButton.setOnClickListener(this);
        mChangelogButton.setOnClickListener(this);

        setupDialogs();
        setupUpdateNameInfo();
        setupProgress(mContext);
        setupMd5Info();
        setupChangeLog();
        setupMenuToolbar(mContext);

        if (Preferences.getIsDownloadOnGoing(mContext)) {
            // If the activity has already been run, and the download started
            // Then start updating the progress bar again
            if (DEBUGGING)
                Log.d(TAG, "Starting progress updater");
            DownloadManager downloadManager = (DownloadManager) mContext.getSystemService(Context
                    .DOWNLOAD_SERVICE);
            new DownloadRomProgress(mContext, downloadManager).execute();
        }
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

        /**Back*/
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
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

            case R.id.menu_available_check_md5:
                new MD5Check(mContext).execute();
                Preferences.setHasMD5Run(mContext, true);
                break;
            case R.id.menu_available_delete:
                mDeleteDialog.show();
                break;
            case R.id.menu_available_install:
                if (!Tools.isRootAvailable()) {
                    mRebootManualDialog.show();
                } else {
                    mRebootDialog.show();
                }
                break;
            case R.id.menu_available_download:
                download();
                break;
            case R.id.menu_available_cancel:
                mDownloadRom.cancelDownload(mContext);
                setupUpdateNameInfo();
                setupProgress(mContext);
                setupMenuToolbar(mContext);
                break;
            case R.id.menu_available_changelog:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R
                        .string.changelog_url)));
                startActivity(browserIntent);
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

                    Utils.deleteFile(RomUpdate.getFullFile(mContext)); // Delete the file
                    Preferences.setHasMD5Run(mContext, false); // MD5 check hasn't been run
                    Preferences.setDownloadFinished(mContext, false);
                    setupUpdateNameInfo(); // Update name info
                    setupProgress(mContext); // Progress goes back to 0
                    setupMd5Info(); // MD5 goes back to default
                    setupMenuToolbar(mContext); // Reset options menu
                }).setNegativeButton(R.string.cancel, null);

        mRebootDialog = new Builder(mContext, R.style.AlertDialogStyle);
        mRebootDialog.setTitle(R.string.are_you_sure)
                .setMessage(R.string.available_reboot_confirm)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    if (DEBUGGING) Log.d(TAG, "ORS is " + Preferences.getORSEnabled(mContext));
                    if (Preferences.getORSEnabled(mContext)) {
                        new GenerateRecoveryScript(mContext).execute();
                    } else {
                        Tools.recovery(mContext);
                    }
                }).setNegativeButton(R.string.cancel, null);

        mNetworkDialog = new Builder(mContext, R.style.AlertDialogStyle);
        mNetworkDialog.setTitle(R.string.available_wrong_network_title)
                .setMessage(R.string.available_wrong_network_message)
                .setPositiveButton(R.string.ok, null)
                .setNeutralButton(R.string.settings, (dialog, which) -> {
                    Intent intent = new Intent(mContext, SettingsActivity.class);
                    mContext.startActivity(intent);
                });

        mRebootManualDialog = new Builder(mContext, R.style.AlertDialogStyle);
        mRebootManualDialog.setTitle(R.string.available_reboot_manual_title)
                .setMessage(R.string.available_reboot_manual_message)
                .setPositiveButton(R.string.cancel, null);
    }


    private void setupChangeLog() {
        TextView changelogView = (TextView) findViewById(R.id.tv_available_changelog_content);
        Bypass byPass = new Bypass(this);
        String changeLogStr = RomUpdate.getChangelog(mContext);
        CharSequence string = byPass.markdownToSpannable(changeLogStr);
        changelogView.setText(string);
        changelogView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void setupUpdateNameInfo() {
        boolean isDownloadOnGoing = Preferences.getIsDownloadOnGoing(mContext);
        TextView updateNameInfoText = (TextView) findViewById(R.id.tv_available_update_name);
        String downloading = getResources().getString(R.string.available_downloading);
        String filename = RomUpdate.getFilename(mContext);

        if (isDownloadOnGoing) {
            updateNameInfoText.setText(downloading);
        } else {
            updateNameInfoText.setText(filename);
        }
    }

    private void setupMd5Info() {
        TextView md5Text = (TextView) findViewById(R.id.tv_available_md5);
        String md5Prefix = getResources().getString(R.string.available_md5);
        String md5 = RomUpdate.getMd5(mContext);
        if (md5.equals("null")) {
            md5Text.setText(md5Prefix + " N/A");
        } else {
            md5Text.setText(md5Prefix + " " + md5);
        }
    }

    private void download() {
        String httpUrl = RomUpdate.getHttpUrl(mContext);
        String directUrl = RomUpdate.getDirectUrl(mContext);
        String error = getResources().getString(R.string.available_url_error);

        boolean isMobile = Utils.isMobileNetwork(mContext);
        boolean isSettingWiFiOnly = Preferences.getNetworkType(mContext).equals(WIFI_ONLY);

        if (isMobile && isSettingWiFiOnly) {
            mNetworkDialog.show();
        } else {
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
                setupUpdateNameInfo();
                setupMenuToolbar(mContext); // Reset options menu
            }
        }
    }

    private class MD5Check extends AsyncTask<Object, Boolean, Boolean> {

        public final String TAG = this.getClass().getSimpleName();

        Context mContext;
        ProgressDialog mMD5CheckDialog;

        MD5Check(Context context) {
            mContext = context;
        }

        @Override
        protected void onPreExecute() {
            // Setup Checking dialog
            mMD5CheckDialog = new ProgressDialog(mContext, R.style.AlertDialogStyle);
            mMD5CheckDialog.setCancelable(false);
            mMD5CheckDialog.setIndeterminate(true);
            mMD5CheckDialog.setMessage(mContext.getString(R.string.available_checking_md5));
            mMD5CheckDialog.show();
        }

        @Override
        protected Boolean doInBackground(Object... params) {
            String file = RomUpdate.getFullFile(mContext).getAbsolutePath(); // Full file, with path
            String md5Remote = RomUpdate.getMd5(mContext); // Remote MD5 form the manifest. This
            // is what we expect it to be
            String md5Local = Tools.shell("md5sum " + file + " | cut -d ' ' -f 1", false); // Run
            // the check on our local file
            md5Local = md5Local.trim(); // Trim both to remove any whitespace
            md5Remote = md5Remote.trim();
            return md5Local.equalsIgnoreCase(md5Remote); // Return the comparison
        }

        @Override
        protected void onPostExecute(Boolean result) {
            mMD5CheckDialog.cancel(); // Remove dialog

            // Show toast letting the user know immediately
            if (result) {
                Toast.makeText(mContext, mContext.getString(R.string.available_md5_ok), Toast
                        .LENGTH_LONG).show();
            } else {
                Toast.makeText(mContext, mContext.getString(R.string.available_md5_failed), Toast
                        .LENGTH_LONG).show();
            }

            Preferences.setMD5Passed(mContext, result); // Set value for other persistent settings
            setupMenuToolbar(mContext); // Reset options menu
            super.onPostExecute(result);
        }
    }
}
