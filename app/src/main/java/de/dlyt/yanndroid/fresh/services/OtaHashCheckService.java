package de.dlyt.yanndroid.fresh.services;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.dlyt.yanndroid.fresh.database.TnsOtaDownload;
import de.dlyt.yanndroid.fresh.R;
import de.dlyt.yanndroid.fresh.hub.AvailableActivity;
import de.dlyt.yanndroid.fresh.database.TnsOta;

public class OtaHashCheckService {
    public static final String TAG = "OtaHashCheckService";

    public OtaHashCheckService(Context context, Dialog dialog) {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            File updateFile = TnsOta.getFullFile(context);
            String md5Server = TnsOta.getMd5(context);

            boolean result = checkMD5(md5Server, updateFile);
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

    public static boolean checkMD5(String md5, File updateFile) {
        if (TextUtils.isEmpty(md5) || updateFile == null) {
            Log.e(TAG, "MD5 string empty or updateFile null");
            return false;
        }

        String calculatedDigest = calculateMD5(updateFile);
        if (calculatedDigest == null) {
            Log.e(TAG, "calculatedDigest null");
            return false;
        }

        Log.v(TAG, "Calculated digest: " + calculatedDigest);
        Log.v(TAG, "Provided digest: " + md5);

        return calculatedDigest.equalsIgnoreCase(md5);
    }

    public static String calculateMD5(File updateFile) {
        MessageDigest digest;

        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Exception while getting digest", e);
            return null;
        }

        InputStream is;
        try {
            is = new FileInputStream(updateFile);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Exception while getting FileInputStream", e);
            return null;
        }

        byte[] buffer = new byte[8192];
        int read;
        try {
            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            byte[] md5sum = digest.digest();
            BigInteger bigInt = new BigInteger(1, md5sum);
            String output = bigInt.toString(16);
            // Fill to 32 chars
            output = String.format("%32s", output).replace(' ', '0');
            return output;
        } catch (IOException e) {
            throw new RuntimeException("Unable to process file for MD5", e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                Log.e(TAG, "Exception on closing MD5 input stream", e);
            }
        }
    }
}
