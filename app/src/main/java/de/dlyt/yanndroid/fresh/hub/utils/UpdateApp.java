package de.dlyt.yanndroid.fresh.hub.utils;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;

import androidx.core.content.FileProvider;

import java.io.File;

import de.dlyt.yanndroid.fresh.Constants;
import de.dlyt.yanndroid.fresh.hub.AboutActivity;
import de.dlyt.yanndroid.fresh.R;

public class UpdateApp implements Constants {
    public static void DownloadAndInstall(Context context, String url) {
        String destination = context.getExternalFilesDir(OTA_DIR_ADDONS).getAbsolutePath()
                + File.separator
                + "update.apk";

        File file = new File(context.getExternalFilesDir(OTA_DIR_ADDONS),
                "update.apk");

        if (file.exists()) {
            file.delete();
        }

        final Uri uri = Uri.parse("file://" + destination);

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setDestinationInExternalFilesDir(context, OTA_DIR_ADDONS, "update.apk");
        request.setTitle(context.getString(R.string.app_name));
        request.setDestinationUri(uri);

        final DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        final long downloadId = manager.enqueue(request);

        BroadcastReceiver onComplete = new BroadcastReceiver() {
            public void onReceive(Context ctxt, Intent intent) {

                Uri apkfileuri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", file);
                Intent install = new Intent(Intent.ACTION_VIEW);
                install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                install.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                install.setDataAndType(apkfileuri, "application/vnd.android.package-archive");
                context.startActivity(install);

                context.unregisterReceiver(this);
                AboutActivity.reEnableUpdateButton(context);

            }
        };
        context.registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }
}
