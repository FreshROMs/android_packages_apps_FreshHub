package de.dlyt.yanndroid.fresh.utils;

import android.content.Context;
import android.util.Log;

import java.text.DecimalFormat;

import de.dlyt.yanndroid.fresh.Constants;
import de.dlyt.yanndroid.fresh.database.TnsOta;
import de.dlyt.yanndroid.fresh.database.TnsOtaDownload;

public class File {
    private static final int KILOBYTE = 1024;
    public static DecimalFormat decimalFormat = new DecimalFormat("##0.#");
    private static int KB = KILOBYTE;
    private static int MB = KB * KB;
    private static int GB = MB * KB;

    static {
        decimalFormat.setMaximumIntegerDigits(3);
        decimalFormat.setMaximumFractionDigits(1);
    }

    public static String formatDataFromBytes(long size) {

        String symbol;
        KB = KILOBYTE;
        symbol = "B";
        if (size < KB) {
            return decimalFormat.format(size) + symbol;
        } else if (size < MB) {
            return decimalFormat.format(size / (float) KB) + 'k' + symbol;
        } else if (size < GB) {
            return decimalFormat.format(size / (float) MB) + 'M' + symbol;
        }
        return decimalFormat.format(size / (float) GB) + 'G' + symbol;
    }

    public static void deleteFile(java.io.File file) {
        if (file.exists()) {
            if (!file.delete()) Log.e(Tools.TAG, "Unable to delete file...");
        }
    }

    public static void setHasFileDownloaded(Context context) {
        java.io.File file = TnsOta.getFullFile(context);
        long filesize = TnsOta.getFileSize(context);
        boolean downloadIsRunning = TnsOtaDownload.getIsDownloadOnGoing(context);

        boolean status = false;
        if (Constants.DEBUGGING) {
            Log.d(Tools.TAG, "Local file " + file.getAbsolutePath());
            Log.d(Tools.TAG, "Local filesize " + file.length());
            Log.d(Tools.TAG, "Remote filesize " + filesize);
        }
        if (file.length() != 0 && file.length() == filesize && !downloadIsRunning) {
            status = true;
        }
        TnsOtaDownload.setDownloadFinished(context, status);
    }
}
