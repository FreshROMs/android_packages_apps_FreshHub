package de.dlyt.yanndroid.fresh.hub;

import android.annotation.SuppressLint;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.AppBarLayout;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.utils.StorageUtils;

import java.io.File;
import java.util.ArrayList;

import de.dlyt.yanndroid.fresh.Constants;
import de.dlyt.yanndroid.fresh.R;
import de.dlyt.yanndroid.fresh.database.TnsAddon;
import de.dlyt.yanndroid.fresh.database.TnsAddonDownload;
import de.dlyt.yanndroid.fresh.services.TnsAddonApiService;
import de.dlyt.yanndroid.fresh.services.download.DownloadAddon;
import de.dlyt.yanndroid.fresh.services.download.DownloadAddonProgress;
import de.dlyt.yanndroid.fresh.utils.AddonProperties;
import de.dlyt.yanndroid.fresh.utils.RecoveryInstall;
import in.uncod.android.bypass.Bypass;

public class AddonActivity extends AppCompatActivity implements Constants {

    public final static String TAG = "AddonActivity";

    public static Context mContext;
    public static ImageLoader mImageLoader;
    public static Handler UIHandler;
    private static Dialog mLoadingDialog;
    private static DisplayImageOptions mImageLoaderOptions;
    private static ListView mListview;
    private static DownloadAddon mDownloadAddon;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(DOWNLOAD_ADDON_DONE)) {
                mLoadingDialog.show();
                new TnsAddonApiService(AddonActivity.this, mContext, mLoadingDialog);
            }
        }
    };

    public static void runOnUI(Runnable runnable) {
        UIHandler.post(runnable);
    }

    @SuppressLint("NewApi")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        mContext = this;
        super.onCreate(savedInstanceState);

        File cacheDir = StorageUtils.getCacheDirectory(mContext);

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(mContext)
                .diskCache(new UnlimitedDiskCache(cacheDir))
                .diskCacheFileCount(150)
                .threadPoolSize(4)
                .build();

        mImageLoaderOptions = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.color.sesl_control_normal_color)
                .showImageOnFail(R.color.sesl_control_normal_color)
                .cacheOnDisk(true)
                .build();

        mImageLoader = ImageLoader.getInstance();
        mImageLoader.init(config);

        final View loadingLayout = getLayoutInflater().inflate(R.layout.dialog_full_loading, null);

        mLoadingDialog = new Dialog(mContext, R.style.LargeProgressDialog);
        mLoadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mLoadingDialog.setCancelable(false);
        mLoadingDialog.setContentView(loadingLayout);

        setContentView(R.layout.activity_addons);

        initToolbar();
        settilte(getString(R.string.main_addon));
        setSubtitle(" ");

        mDownloadAddon = new DownloadAddon();
    }

    @Override
    public void onStart() {
        super.onStart();
        mLoadingDialog.show();
        this.registerReceiver(mReceiver, new IntentFilter(DOWNLOAD_ADDON_DONE));
        UIHandler = new Handler(Looper.getMainLooper());
        mImageLoader = ImageLoader.getInstance();
        new TnsAddonApiService(this, mContext, mLoadingDialog);
    }

    @Override
    public void onStop() {
        super.onStop();
        this.unregisterReceiver(mReceiver);
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

    public void setSubtitle(String subtitle) {
        TextView expanded_subtitle = findViewById(R.id.expanded_subtitle);
        expanded_subtitle.setText(subtitle);
    }


    public void setupListView(ArrayList<TnsAddon> addonsList) {
        final AddonsArrayAdapter adapter = new AddonsArrayAdapter(mContext, addonsList);
        mListview = (ListView) findViewById(R.id.listview);
        mListview.removeAllViewsInLayout();
        mListview.setAdapter(adapter);
    }

    public static class AddonsArrayAdapter extends ArrayAdapter<TnsAddon> {

        AddonsArrayAdapter(Context context, ArrayList<TnsAddon> users) {
            super(context, 0, users);
        }

        public static void updateProgress(int index, int progress, boolean finished, int downloaded, boolean successful) {
            View view = mListview.getChildAt((index - 1) -
                    mListview.getFirstVisiblePosition());

            Long currentTime = System.currentTimeMillis();

            if (view == null) {
                return;
            }

            ProgressBar progressBar = view.findViewById(R.id.progress_bar);
            TextView downloadSpeed = (TextView) view.findViewById(R.id.download_speed);
            TextView startTimeText = (TextView) view.findViewById(R.id.start_time);
            Long startTime = Long.parseLong(String.valueOf(startTimeText.getText()));
            int addonDownloadSpeed = (downloaded / (int) (currentTime - startTime));
            String localizedSpeed = de.dlyt.yanndroid.fresh.utils.File.formatDataFromBytes(addonDownloadSpeed * 1000);

            if (finished) {
                TnsAddonDownload.removeAddonDownload(mContext, index, TnsAddonDownload.getAddonDownload(mContext, index));

                if (successful) {
                    progressBar.setProgress(0);
                    updateButtons(index, true);
                } else {
                    progressBar.setProgress(0);
                    updateButtons(index, false);
                }
            } else {
                progressBar.setProgress(progress);
                downloadSpeed.setText(localizedSpeed + "/s");
            }
        }

        public static void updateButtons(int index, boolean finished) {
            View view = mListview.getChildAt((index - 1) -
                    mListview.getFirstVisiblePosition());

            if (view == null) {
                return;
            }

            final LinearLayout download = (LinearLayout) view.findViewById(R.id.download_button);
            final LinearLayout cancel = (LinearLayout) view.findViewById(R.id.cancel_button);
            final LinearLayout delete = (LinearLayout) view.findViewById(R.id.delete_button);
            final LinearLayout progressContainer = (LinearLayout) view.findViewById(R.id.download_progress_container);
            final LinearLayout infoContainer = (LinearLayout) view.findViewById(R.id.addon_info_container);

            progressContainer.setVisibility(View.GONE);
            infoContainer.setVisibility(View.VISIBLE);

            if (finished) {
                download.setVisibility(View.GONE);
                download.setClickable(false);
                delete.setVisibility(View.VISIBLE);
                cancel.setVisibility(View.GONE);
            } else {
                download.setVisibility(View.VISIBLE);
                download.setClickable(true);
                cancel.setVisibility(View.GONE);
                delete.setVisibility(View.GONE);
            }
        }

        private void deleteConfirm(final File file, final TnsAddon item) {
            Builder deleteConfirm = new Builder(mContext, R.style.AlertDialogStyle);
            deleteConfirm.setTitle(R.string.delete);
            deleteConfirm.setMessage(mContext.getResources().getString(R.string.delete_addon_confirm, item.getTitle()));
            deleteConfirm.setPositiveButton(R.string.ok, (dialog, which) -> {
                if (file.exists()) {
                    updateButtons(item.getId(), false);
                    TnsAddonDownload.setIsUninstallingAddon(mContext, item.getTitle() + "_" + item.getVersionNumber());
                    new RecoveryInstall(mContext, true, item.getTitle() + "_" + item.getVersionNumber() + ".zip");
                }
            });
            deleteConfirm.setNegativeButton(R.string.cancel, null);
            deleteConfirm.show();
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            final TnsAddon item = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.card_addons_list_item, parent, false);
            }

            TextView title = (TextView) convertView.findViewById(R.id.title);
            TextView progress_title = (TextView) convertView.findViewById(R.id.progress_title);
            TextView desc = (TextView) convertView.findViewById(R.id.description);
            TextView updatedOn = (TextView) convertView.findViewById(R.id.updatedOn);
            TextView filesize = (TextView) convertView.findViewById(R.id.size);
            final LinearLayout install = (LinearLayout) convertView.findViewById(R.id.install_button);
            final LinearLayout download = (LinearLayout) convertView.findViewById(R.id.download_button);
            final LinearLayout cancel = (LinearLayout) convertView.findViewById(R.id.cancel_button);
            final LinearLayout delete = (LinearLayout) convertView.findViewById(R.id.delete_button);
            final LinearLayout progressContainer = (LinearLayout) convertView.findViewById(R.id.download_progress_container);
            final LinearLayout infoContainer = (LinearLayout) convertView.findViewById(R.id.addon_info_container);
            final TextView startTime = (TextView) convertView.findViewById(R.id.start_time);
            final ImageView addonThumbnail = (ImageView) convertView.findViewById(R.id.addon_info_thumbnail);

            assert item != null;
            title.setText(item.getTitle());
            progress_title.setText(item.getTitle());

            Bypass byPass = new Bypass(mContext);
            String descriptionStr = item.getDesc();
            CharSequence string = byPass.markdownToSpannable(descriptionStr);
            desc.setText(string);
            desc.setMovementMethod(LinkMovementMethod.getInstance());

            mImageLoader.displayImage(item.getImageUrl(), addonThumbnail, mImageLoaderOptions);

            String versionName = item.getVersionName();
            updatedOn.setText(versionName);

            filesize.setText(de.dlyt.yanndroid.fresh.utils.File.formatDataFromBytes(item.getFilesize()));
            final File file = new File(mContext.getExternalFilesDir(OTA_DIR_ADDONS),
                    item.getTitle() + "_" + item.getVersionNumber() + ".zip");

            if (DEBUGGING) {
                Log.d(TAG, "file path " + file.getAbsolutePath());
                Log.d(TAG, "file length " + file.length() + " remoteLength " + item.getFilesize());
            }

            DownloadManager downloadManager = (DownloadManager) mContext.getSystemService(Context
                    .DOWNLOAD_SERVICE);

            boolean finished = (file.exists() && file.length() >= item.getFilesize());
            boolean installed = AddonProperties.isAddonInstalled(item.getPackageName());
            boolean noUninstall = AddonProperties.isAddonNonUninstall(item.getPackageName());
            boolean updated = AddonProperties.getInstalledAddonVersion(item.getPackageName()) >= item.getVersionNumber();
            if (updated)
                finished = (file.exists()); // Addons installed outside Fresh Hub has a different size

            boolean downloading = false;
            long downloadId = TnsAddonDownload.getAddonDownload(mContext, item.getId());

            if (downloadId != 0) {
                downloading = true;
            }

            install.setVisibility(View.GONE);

            if (installed) {
                infoContainer.setVisibility(View.VISIBLE);
                progressContainer.setVisibility(View.GONE);
                if (updated && noUninstall) {
                    download.setVisibility(View.GONE);
                    install.setVisibility(View.GONE);
                    cancel.setVisibility(View.GONE);
                    delete.setVisibility(View.GONE);
                } else if (updated) {
                    delete.setVisibility(!finished ? View.GONE : View.VISIBLE);
                    download.setVisibility(View.GONE);
                    cancel.setVisibility(View.GONE);
                } else if (finished) {
                    download.setVisibility(View.GONE);
                    install.setVisibility(View.VISIBLE);
                    cancel.setVisibility(View.GONE);
                    delete.setVisibility(View.GONE);
                } else {
                    download.setVisibility(View.VISIBLE);
                    download.setClickable(true);
                    cancel.setVisibility(View.GONE);
                    delete.setVisibility(View.GONE);
                }
            } else if (downloading) {
                download.setVisibility(View.GONE);
                progressContainer.setVisibility(View.VISIBLE);
                infoContainer.setVisibility(View.GONE);
                cancel.setVisibility(View.VISIBLE);
                startTime.setText(String.valueOf(System.currentTimeMillis() - 1000));
                delete.setVisibility(View.GONE);
                new DownloadAddonProgress(mContext, downloadManager, item.getId(), downloadId);
            } else if (finished) {
                infoContainer.setVisibility(View.VISIBLE);
                progressContainer.setVisibility(View.GONE);
                download.setVisibility(View.GONE);
                install.setVisibility(View.VISIBLE);
                download.setClickable(true);
                cancel.setVisibility(View.GONE);
                delete.setVisibility(View.GONE);
            } else {
                progressContainer.setVisibility(View.GONE);
                download.setVisibility(View.VISIBLE);
                cancel.setVisibility(View.GONE);
                delete.setVisibility(View.GONE);
                infoContainer.setVisibility(View.VISIBLE);
            }

            download.setOnClickListener(v -> {

                download.setVisibility(View.GONE);
                startTime.setText(String.valueOf(System.currentTimeMillis()));
                progressContainer.setVisibility(View.VISIBLE);
                infoContainer.setVisibility(View.GONE);
                cancel.setVisibility(View.VISIBLE);
                mDownloadAddon.startDownload(mContext, item.getDownloadLink(), item
                        .getTitle(), item.getId(), item.getVersionNumber());

                long downloadIdNew = TnsAddonDownload.getAddonDownload(mContext, item.getId());
                new DownloadAddonProgress(mContext, downloadManager, item.getId(), downloadIdNew);
            });

            install.setOnClickListener(v -> {
                new RecoveryInstall(mContext, true, item.getTitle() + "_" + item.getVersionNumber() + ".zip");
            });

            cancel.setOnClickListener(v -> {
                download.setVisibility(View.VISIBLE);
                progressContainer.setVisibility(View.GONE);
                infoContainer.setVisibility(View.VISIBLE);
                cancel.setVisibility(View.GONE);
                mDownloadAddon.cancelDownload(mContext, item.getId());
                updateProgress(item.getId(), 0, true, 1, false);
            });
            delete.setOnClickListener(v -> deleteConfirm(file, item));

            convertView.setOnClickListener(v -> {
                Intent i = new Intent(mContext, AddonInfoActivity.class);
                i.putExtra("id", item.getId());
                i.putExtra("name", item.getTitle());
                i.putExtra("packageName", item.getPackageName());
                i.putExtra("downloadUrl", item.getDownloadLink());
                i.putExtra("totalSize", item.getFilesize());
                i.putExtra("versionName", item.getVersionName());
                i.putExtra("fullInfo", item.getFullInfo());
                i.putExtra("versionNumber", item.getVersionNumber());
                i.putExtra("thumbnailUrl", item.getImageUrl());
                mContext.startActivity(i);
            });

            return convertView;
        }
    }

}
