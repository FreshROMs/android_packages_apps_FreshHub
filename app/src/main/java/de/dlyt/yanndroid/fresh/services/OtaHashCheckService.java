package de.dlyt.yanndroid.fresh.services;

import android.app.Dialog;
import android.content.Context;
import android.widget.Toast;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.dlyt.yanndroid.fresh.database.TnsOtaDownload;
import de.dlyt.yanndroid.fresh.R;
import de.dlyt.yanndroid.fresh.hub.AvailableActivity;
import de.dlyt.yanndroid.fresh.database.TnsOta;
import de.dlyt.yanndroid.fresh.utils.Tools;

public class OtaHashCheckService {
    public final String TAG = this.getClass().getSimpleName();

    public OtaHashCheckService(Context context, Dialog dialog) {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            String file = TnsOta.getFullFile(context).getAbsolutePath();
            String md5Remote = TnsOta.getMd5(context);
            String md5Local = Tools.shell("md5sum " + file + " | cut -d ' ' -f 1", false);

            md5Local = md5Local.trim();
            md5Remote = md5Remote.trim();

            boolean result = md5Local.equalsIgnoreCase(md5Remote);
            TnsOtaDownload.setMD5Passed(context, result);
            TnsOtaDownload.setHasMD5Run(context, true);

            AvailableActivity.runOnUI(() -> {
                CharSequence md5Message = context.getString(R.string.available_md5_failed);
                dialog.cancel();

                if (result) {
                    md5Message = context.getString(R.string.available_md5_ok);
                }

                Toast.makeText(context, md5Message, Toast.LENGTH_LONG).show();
                AvailableActivity.setupMenuToolbar(context);
            });
        });
    }
}
