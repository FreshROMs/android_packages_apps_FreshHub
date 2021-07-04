package de.dlyt.yanndroid.fresh.hub;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;

import de.dlyt.yanndroid.fresh.Constants;
import de.dlyt.yanndroid.fresh.R;
import de.dlyt.yanndroid.fresh.database.TnsAddonDownload;
import de.dlyt.yanndroid.fresh.services.download.DownloadAddonInfo;
import de.dlyt.yanndroid.fresh.services.download.DownloadAddonInfoProgress;
import de.dlyt.yanndroid.fresh.utils.AddonProperties;
import de.dlyt.yanndroid.fresh.utils.RecoveryInstall;
import de.dlyt.yanndroid.samsung.layout.ToolbarLayout;
import in.uncod.android.bypass.Bypass;

public class AddonInfoActivity extends AppCompatActivity implements Constants {

    public static ImageLoader mImageLoader;
    public static TextView mDownloadedSize;
    public static LinearLayout mDownloadButton;
    public static LinearLayout mInstallButton;
    public static LinearLayout mUpdateButton;
    public static LinearLayout mUninstallButton;
    public static LinearLayout mDownloadProgressLayout;
    public static ProgressBar mDownloadProgress;
    public static Handler UIHandler;
    private static Context mContext;
    private static DownloadAddonInfo mDownloadAddon;
    private static String mDownloadUrl;
    private static String mPackageName;
    private static String mTitle;
    private static Integer mAddonId;
    private static Long mFileSize;
    private static Integer mVersionNumber;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(DOWNLOAD_ADDON_DONE)) {
                getDownloadStatus();
            }
        }
    };

    public static void runOnUI(Runnable runnable) {
        UIHandler.post(runnable);
    }

    public static void setLayoutEnabled(LinearLayout view, boolean enable) {
        view.setEnabled(enable);
        view.setClickable(enable);
        view.setFocusable(enable);
        view.setAlpha(enable ? 1f : 0.7f);
    }

    public static void updateProgress(int progress, boolean finished, int downloaded, boolean successful) {
        String localizedSize = de.dlyt.yanndroid.fresh.utils.File.formatDataFromBytes(downloaded);

        if (finished) {
            mDownloadProgressLayout.setVisibility(View.GONE);
            mDownloadProgress.setProgress(0);
            if (successful) {
                mInstallButton.setVisibility(View.VISIBLE);
            } else {
                mDownloadButton.setVisibility(View.VISIBLE);
            }
        } else {
            mDownloadProgress.setProgress(progress);
            mDownloadedSize.setText(localizedSize);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mContext = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addon_info);
        mImageLoader = ImageLoader.getInstance();
        mDownloadAddon = new DownloadAddonInfo();

        DisplayImageOptions imageLoaderOptions = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.color.sesl_control_normal_color)
                .showImageOnFail(R.color.sesl_control_normal_color)
                .cacheOnDisk(true)
                .build();

        ToolbarLayout toolbarLayout = findViewById(R.id.toolbar_layout);
        toolbarLayout.setExpanded(false, false);
        setSupportActionBar(toolbarLayout.getToolbar());
        toolbarLayout.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        TextView collapsed_title = findViewById(R.id.collapsed_title);
        collapsed_title.setText(mTitle);
        collapsed_title.setAlpha(0);

        ScrollView content_scroll = findViewById(R.id.content_scroll);
        content_scroll.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                collapsed_title.setAlpha((float) (scrollY) / 120);
            }
        });

        final ImageView addonThumbnail = (ImageView) findViewById(R.id.addon_info_thumbnail);
        final TextView addonVersion = (TextView) findViewById(R.id.version_number);
        final TextView addonPackageName = (TextView) findViewById(R.id.package_name);
        final TextView addonFullInfo = (TextView) findViewById(R.id.addon_information_description);
        final TextView addonTotalSize = (TextView) findViewById(R.id.addon_download_total);
        final TextView addonName = (TextView) findViewById(R.id.title);

        Intent intent = getIntent();

        mAddonId = intent.getIntExtra("id", 0);
        mDownloadUrl = intent.getStringExtra("downloadUrl");
        mTitle = intent.getStringExtra("name");
        mFileSize = intent.getLongExtra("totalSize", 0);
        mPackageName = intent.getStringExtra("packageName");
        mVersionNumber = intent.getIntExtra("versionNumber", 0);

        String versionName = intent.getStringExtra("versionName");
        String fullInfo = intent.getStringExtra("fullInfo");
        String thumbnailUrl = intent.getStringExtra("thumbnailUrl");

        mDownloadedSize = (TextView) findViewById(R.id.addon_download_size);

        mDownloadButton = (LinearLayout) findViewById(R.id.addon_info_download_button);
        mInstallButton = (LinearLayout) findViewById(R.id.addon_info_install_button);
        mUpdateButton = (LinearLayout) findViewById(R.id.addon_info_update_button);
        mUninstallButton = (LinearLayout) findViewById(R.id.addon_info_uninstall_button);

        mDownloadProgressLayout = (LinearLayout) findViewById(R.id.addon_info_progress);
        mDownloadProgress = (ProgressBar) findViewById(R.id.addon_download_progress);

        mImageLoader.displayImage(thumbnailUrl, addonThumbnail, imageLoaderOptions);
        mDownloadedSize.setText(de.dlyt.yanndroid.fresh.utils.File.formatDataFromBytes(0));
        addonVersion.setText(versionName);
        addonPackageName.setText(mPackageName);
        addonTotalSize.setText(de.dlyt.yanndroid.fresh.utils.File.formatDataFromBytes(mFileSize));
        addonName.setText(mTitle);

        Bypass byPass = new Bypass(mContext);
        CharSequence string = byPass.markdownToSpannable(fullInfo);
        addonFullInfo.setText(string);
        addonFullInfo.setMovementMethod(LinkMovementMethod.getInstance());

        getDownloadStatus();
    }

    @Override
    public void onStart() {
        super.onStart();
        this.registerReceiver(mReceiver, new IntentFilter(DOWNLOAD_ADDON_DONE));

        UIHandler = new Handler(Looper.getMainLooper());
        mImageLoader = ImageLoader.getInstance();


    }

    @Override
    public void onStop() {
        super.onStop();
        this.unregisterReceiver(mReceiver);
    }

    public void deleteConfirmAddonInfo(View v) {
        AlertDialog.Builder deleteConfirm = new AlertDialog.Builder(mContext, R.style.DialogStyle);
        deleteConfirm.setTitle(R.string.delete);
        deleteConfirm.setMessage(mContext.getString(R.string.delete_addon_confirm, mTitle));
        deleteConfirm.setPositiveButton(R.string.ok, (dialog, which) -> {
            TnsAddonDownload.setIsUninstallingAddon(mContext, mTitle + "_" + mVersionNumber);
            new RecoveryInstall(mContext, true, mTitle + "_" + mVersionNumber + ".zip");
        });
        deleteConfirm.setNegativeButton(R.string.cancel, null);
        deleteConfirm.show();
    }

    public void startAddonDownload(View v) {
        mDownloadButton.setVisibility(View.GONE);
        mInstallButton.setVisibility(View.GONE);
        mDownloadProgressLayout.setVisibility(View.VISIBLE);
        mDownloadAddon.startDownload(mContext, mDownloadUrl, mTitle, mAddonId, mVersionNumber);
    }

    private void getDownloadStatus() {
        DownloadManager downloadManager = (DownloadManager) mContext.getSystemService(Context
                .DOWNLOAD_SERVICE);

        final File file = new File(mContext.getExternalFilesDir(OTA_DIR_ADDONS),
                mTitle + "_" + mVersionNumber + ".zip");

        boolean finished = (file.exists() && file.length() >= mFileSize);
        boolean installed = AddonProperties.isAddonInstalled(mPackageName);
        boolean noUninstall = AddonProperties.isAddonNonUninstall(mPackageName);
        boolean updated = AddonProperties.getInstalledAddonVersion(mPackageName) >= mVersionNumber;
        if (updated)
            finished = (file.exists()); // Addons installed outside Fresh Hub has a different size

        mDownloadButton.setVisibility(View.GONE);
        mUpdateButton.setVisibility(View.GONE);
        mUninstallButton.setVisibility(View.GONE);
        mInstallButton.setVisibility(View.GONE);
        mDownloadProgressLayout.setVisibility(View.GONE);

        boolean downloading = false;
        long downloadId = TnsAddonDownload.getAddonDownload(mContext, mAddonId);

        if (downloadId != 0) {
            downloading = true;
        }

        if (installed) {
            if (updated && noUninstall) {
                mUninstallButton.setVisibility(View.VISIBLE);
                setLayoutEnabled(mUninstallButton, false);
            } else if (updated) {
                mUninstallButton.setVisibility(View.VISIBLE);
                setLayoutEnabled(mUninstallButton, finished);
            } else if (finished) {
                mInstallButton.setVisibility(View.VISIBLE);
            } else {
                mUpdateButton.setVisibility(View.VISIBLE);
            }
        } else if (downloading) {
            mDownloadProgressLayout.setVisibility(View.VISIBLE);
            new DownloadAddonInfoProgress(mContext, downloadManager, mAddonId, downloadId);
        } else if (finished) {
            mInstallButton.setVisibility(View.VISIBLE);
        } else {
            mDownloadButton.setVisibility(View.VISIBLE);
        }
    }

    public void cancelAddonDownload(View v) {
        mDownloadAddon.cancelDownload(mContext, mAddonId);
        getDownloadStatus();
    }

    public void addonRecovery(View v) {
        new RecoveryInstall(mContext, true, mTitle + "_" + mVersionNumber + ".zip");
    }

}