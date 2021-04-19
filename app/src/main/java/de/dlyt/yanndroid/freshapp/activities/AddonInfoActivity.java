package de.dlyt.yanndroid.freshapp.activities;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.AppBarLayout;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;

import de.dlyt.yanndroid.freshapp.R;
import de.dlyt.yanndroid.freshapp.download.DownloadAddonInfo;
import de.dlyt.yanndroid.freshapp.download.DownloadAddonInfoProgress;
import de.dlyt.yanndroid.freshapp.utils.Constants;
import de.dlyt.yanndroid.freshapp.utils.AddonDownloadDB;
import de.dlyt.yanndroid.freshapp.utils.Preferences;
import de.dlyt.yanndroid.freshapp.utils.Utils;
import in.uncod.android.bypass.Bypass;

public class AddonInfoActivity extends AppCompatActivity implements Constants {

    private static Context mContext;
    public static ImageLoader mImageLoader;
    public static TextView mDownloadedSize;
    public static LinearLayout mDownloadButton;
    public static LinearLayout mInstallButton;
    public static LinearLayout mUpdateButton;
    public static LinearLayout mUninstallButton;
    public static LinearLayout mDownloadProgressLayout;
    public static ProgressBar mDownloadProgress;
    private static DownloadAddonInfo mDownloadAddon;

    private static String mDownloadUrl;
    private static String mPackageName;
    private static String mTitle;
    private static Integer mAddonId;
    private static Long mFileSize;
    private static Integer mVersionNumber;
    public static Handler UIHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mContext = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addon_info);
        mImageLoader = ImageLoader.getInstance();
        mDownloadAddon = new DownloadAddonInfo();

        initToolbar();

        TextView expanded_subtitle = findViewById(R.id.expanded_subtitle);
        expanded_subtitle.setText("");

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

        mImageLoader.displayImage(thumbnailUrl, addonThumbnail);
        mDownloadedSize.setText(Utils.formatDataFromBytes(0));
        addonVersion.setText(versionName);
        addonPackageName.setText(mPackageName);
        addonTotalSize.setText(Utils.formatDataFromBytes(mFileSize));
        addonName.setText(mTitle);
        settilte("");

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

        Bypass byPass = new Bypass(mContext);
        CharSequence string = byPass.markdownToSpannable(fullInfo);
        addonFullInfo.setText(string);
        addonFullInfo.setMovementMethod(LinkMovementMethod.getInstance());

        getDownloadStatus();
    }

    @Override
    public void onStart() {
        super.onStart();
        UIHandler = new Handler(Looper.getMainLooper());
        mImageLoader = ImageLoader.getInstance();
    }

    public static void runOnUI(Runnable runnable) {
        UIHandler.post(runnable);
    }

    public void startAddonDownload(View v) {
        boolean isMobile = Utils.isMobileNetwork(mContext);
        boolean isSettingWiFiOnly = Preferences.getNetworkType(mContext).equals
                (WIFI_ONLY);

        if (isMobile && isSettingWiFiOnly) {
            showNetworkDialog();
        } else {
            mDownloadButton.setVisibility(View.GONE);
            mInstallButton.setVisibility(View.GONE);
            mDownloadProgressLayout.setVisibility(View.VISIBLE);
            mDownloadAddon.startDownload(mContext, mDownloadUrl, mTitle, mAddonId);
        }
    }

    private void getDownloadStatus() {
        DownloadManager downloadManager = (DownloadManager) mContext.getSystemService(Context
                .DOWNLOAD_SERVICE);

        final File file = new File(mContext.getExternalFilesDir(OTA_DIR_ADDONS),
                mTitle + ".zip");

        boolean finished = file.length() >= mFileSize;
        boolean installed = Utils.isAddonInstalled(mPackageName);
        boolean updated = Utils.getInstalledAddonVersion(mPackageName) >= mVersionNumber;

        mDownloadButton.setVisibility(View.GONE);
        mUpdateButton.setVisibility(View.GONE);
        mUninstallButton.setVisibility(View.GONE);
        mInstallButton.setVisibility(View.GONE);
        mDownloadProgressLayout.setVisibility(View.GONE);

        boolean downloading = false;
        long downloadId = AddonDownloadDB.getAddonDownload(mContext, mAddonId);

        if (downloadId != 0) {
            downloading = true;
        }

        if (installed) {
            if (updated) {
                mUninstallButton.setVisibility(View.VISIBLE);
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

    public static void updateProgress(int progress, boolean finished, int downloaded, boolean successful) {
        String localizedSize = Utils.formatDataFromBytes(downloaded);

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

    private void showNetworkDialog() {
        AlertDialog.Builder mNetworkDialog = new AlertDialog.Builder(mContext, R.style.AlertDialogStyle);
        mNetworkDialog.setTitle(R.string.available_wrong_network_title)
                .setMessage(R.string.available_wrong_network_message)
                .setPositiveButton(R.string.ok, null)
                .setNeutralButton(R.string.settings, (dialog, which) -> {
                    Intent intent = new Intent(mContext, SettingsActivity.class);
                    mContext.startActivity(intent);
                });

        mNetworkDialog.show();
    }

    public void initToolbar() {
        /** Def */
        androidx.appcompat.widget.Toolbar toolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        AppBarLayout AppBar = findViewById(R.id.app_bar);


        /** 1/3 of the Screen */
        ViewGroup.LayoutParams layoutParams = AppBar.getLayoutParams();
        layoutParams.height = (int) ((double) this.getResources().getDisplayMetrics().heightPixels / 2.6);

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
}