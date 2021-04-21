package de.dlyt.yanndroid.fresh.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;

import com.stericson.RootTools.BuildConfig;
import com.stericson.RootTools.RootTools;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import de.dlyt.yanndroid.fresh.Constants;

public class Tools implements Constants {
    public static final String TAG = "FreshHubTools";

    public static String shell(String cmd, boolean root) {
        String out = "";
        ArrayList<String> r = system(root ? getSuBin() : "sh", cmd).getStringArrayList("out");
        assert r != null;
        for (String l : r) {
            out += l + "\n";
        }
        return out;
    }

    public static boolean isRootAvailable() {
        return RootTools.isRootAvailable();
    }

    public static void rebootUpdate(Context context) {
        try {
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            powerManager.reboot("recovery");
        } catch (Exception e) {
            Log.e("Tools", "reboot '" + "recovery" + "' error: " + e.getMessage());
            shell("reboot " + "recovery", true);
        }
    }

    private static boolean isUiThread() {
        return (Looper.myLooper() == Looper.getMainLooper());
    }

    private static String getSuBin() {
        if (new File("/system/bin", "su").exists()) {
            return "/system/bin/su";
        }
        if (RootTools.isRootAvailable()) {
            return "su";
        }
        return "sh";
    }

    private static Bundle system(String shell, String command) {
        if (DEBUGGING) {
            if (isUiThread()) {
                Log.e(shell, "Application attempted to run a shell command from the main thread");
            }
            Log.d(shell, "START");
        }

        ArrayList<String> res = new ArrayList<>();
        ArrayList<String> err = new ArrayList<>();
        boolean success = false;
        try {
            Process process = Runtime.getRuntime().exec(shell);
            DataOutputStream STDIN = new DataOutputStream(process.getOutputStream());
            BufferedReader STDOUT = new BufferedReader(new InputStreamReader(process
                    .getInputStream()));
            BufferedReader STDERR = new BufferedReader(new InputStreamReader(process
                    .getErrorStream()));
            if (BuildConfig.DEBUG) Log.i(shell, command);
            STDIN.writeBytes(command + "\n");
            STDIN.flush();
            STDIN.writeBytes("exit\n");
            STDIN.flush();

            process.waitFor();
            if (process.exitValue() == 255) {
                if (BuildConfig.DEBUG) Log.e(shell, "SU was probably denied! Exit value is 255");
                err.add("SU was probably denied! Exit value is 255");
            }

            while (STDOUT.ready()) {
                String read = STDOUT.readLine();
                if (BuildConfig.DEBUG) Log.d(shell, read);
                res.add(read);
            }
            while (STDERR.ready()) {
                String read = STDERR.readLine();
                if (BuildConfig.DEBUG) Log.e(shell, read);
                err.add(read);
            }

            process.destroy();
            success = err.size() <= 0;
        } catch (IOException e) {
            if (BuildConfig.DEBUG) Log.e(shell, "IOException: " + e.getMessage());
            err.add("IOException: " + e.getMessage());
        } catch (InterruptedException e) {
            if (BuildConfig.DEBUG) Log.e(shell, "InterruptedException: " + e.getMessage());
            err.add("InterruptedException: " + e.getMessage());
        }
        if (BuildConfig.DEBUG) Log.d(shell, "END");
        Bundle r = new Bundle();
        r.putBoolean("success", success);
        r.putString("cmd", command);
        r.putString("binary", shell);
        r.putStringArrayList("out", res);
        r.putStringArrayList("error", err);
        return r;
    }

    public static boolean isDeviceOnline(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network nw = connectivityManager.getActiveNetwork();
        if (nw == null) return false;
        NetworkCapabilities actNw = connectivityManager.getNetworkCapabilities(nw);
        return actNw != null && (actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                || actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                || actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                || actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH));
    }
}
