package de.dlyt.yanndroid.freshapp.tasks;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import de.dlyt.yanndroid.freshapp.R;
import de.dlyt.yanndroid.freshapp.utils.Constants;
import de.dlyt.yanndroid.freshapp.utils.Utils;

public class LoadUpdateManifest extends AsyncTask<Void, Void, Void> implements Constants {

    private static final String MANIFEST = "update_manifest.xml";
    public final String TAG = this.getClass().getSimpleName();
    // Did this come from the BackgroundReceiver class?
    private boolean shouldUpdateForegroundApp;
    private Context mContext;
    private Context context;

    public LoadUpdateManifest(Context context, boolean input) {
        mContext = context;
        shouldUpdateForegroundApp = input;
    }

    @Override
    protected void onPreExecute() {
        File manifest = new File(mContext.getFilesDir().getPath(), MANIFEST);
        if (manifest.exists()) {
            boolean deleted = manifest.delete();
            if (!deleted) Log.e(TAG, "Could not delete manifest file...");
        }
    }

    @Override
    protected Void doInBackground(Void... v) {

        try {
            InputStream input;

            URL url = new URL(
                    Utils.getProp(mContext.getResources().getString(R.string.ota_swupdate_prop_api_url)) + "/"
                            + Utils.getDeviceProduct() + "/"
                            + Utils.getProp(mContext.getResources().getString(R.string.ota_swupdate_prop_branch)) + "/"
                            + Utils.getProp(mContext.getResources().getString(R.string.ota_swupdate_prop_version)) + "/");


            url = new URL("https://ota.tensevntysevn.cf/fresh/a50xx/beta/21040501/"); //todo: remove at release

            URLConnection connection = url.openConnection();
            connection.connect();
            // download the file
            input = new BufferedInputStream(url.openStream());

            OutputStream output = mContext.openFileOutput(
                    MANIFEST, Context.MODE_PRIVATE);

            byte data[] = new byte[1024];
            int count;
            while ((count = input.read(data)) != -1) {
                output.write(data, 0, count);
            }

            output.flush();
            output.close();
            input.close();

            // file finished downloading, parse it!
            RomXmlParser parser = new RomXmlParser();
            parser.parse(new File(mContext.getFilesDir(), MANIFEST), mContext);
        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e.getMessage());
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        Intent intent;
        if (shouldUpdateForegroundApp) {
            intent = new Intent(MANIFEST_LOADED);
        } else {
            intent = new Intent(MANIFEST_CHECK_BACKGROUND);
        }

        mContext.sendBroadcast(intent);
        super.onPostExecute(result);
    }
}