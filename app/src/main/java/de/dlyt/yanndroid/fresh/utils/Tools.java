package de.dlyt.yanndroid.fresh.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.PowerManager;

import de.dlyt.yanndroid.fresh.Constants;

public class Tools implements Constants {
    public static final String TAG = "FreshHubTools";

    public static void rebootUpdate(Context context) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        powerManager.reboot("recovery");
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
