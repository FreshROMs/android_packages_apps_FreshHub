package de.dlyt.yanndroid.freshapp.tasks;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.dlyt.yanndroid.freshapp.R;
import de.dlyt.yanndroid.freshapp.utils.Constants;
import de.dlyt.yanndroid.freshapp.utils.Utils;

public class LoadUpdateManifest implements Constants {

    private static final String MANIFEST = "update_manifest.xml";
    public final String TAG = this.getClass().getSimpleName();

    public LoadUpdateManifest(Context context, boolean isForeground) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            File manifest = new File(context.getFilesDir().getPath(), MANIFEST);
            if (manifest.exists()) {
                boolean deleted = manifest.delete();
                if (!deleted) Log.e(TAG, "Could not delete manifest file...");
            }

            try {
                InputStream input;

                URL url = new URL(
                        Utils.getProp(context.getResources().getString(R.string.ota_swupdate_prop_api_url)) + "/"
                                + Utils.getDeviceProduct() + "/"
                                + Utils.getProp(context.getResources().getString(R.string.ota_swupdate_prop_branch)) + "/"
                                + Utils.getProp(context.getResources().getString(R.string.ota_swupdate_prop_version)) + "/");

                ////////////////////////////////////////////////////////////////////////////////////
                // TO-DO: REMOVE ON PRODUCTION
                // TODO
                // IMPORTANT
                url = new URL("https://ota.tensevntysevn.cf/fresh/a50xx/beta/21040501/"); //todo: remove at release
                // REMOVE ON RELEASE
                // TODO
                // IMPORTANT
                ////////////////////////////////////////////////////////////////////////////////////

                URLConnection connection = url.openConnection();
                connection.connect();
                input = new BufferedInputStream(url.openStream());

                OutputStream output = context.openFileOutput(MANIFEST, Context.MODE_PRIVATE);

                byte[] data = new byte[1024];
                int count;
                while ((count = input.read(data)) != -1) {
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();

                RomXmlParser parser = new RomXmlParser();
                parser.parse(new File(context.getFilesDir(), MANIFEST), context);

                handler.post(() -> {
                    Intent intent;
                    if (isForeground) {
                        intent = new Intent(MANIFEST_LOADED);
                    } else {
                        intent = new Intent(MANIFEST_CHECK_BACKGROUND);
                    }
                    context.sendBroadcast(intent);
                });
            } catch (Exception e) {
                Log.d(TAG, "Exception: " + e.getMessage());
            }
        });
    }
}