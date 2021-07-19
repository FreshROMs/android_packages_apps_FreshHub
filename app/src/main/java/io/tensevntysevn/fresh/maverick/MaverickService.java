package io.tensevntysevn.fresh.maverick;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.provider.Settings;

import androidx.annotation.Nullable;

import java.io.File;

import de.dlyt.yanndroid.fresh.utils.Notifications;
import io.tensevntysevn.fresh.ExperienceUtils;

public class MaverickService extends Service {
    /* USB security service. Based on GrapheneOS' implementation.
     * Requires kernel support (we will have a check in here).
     * Literal. Freaking. References to MegaMan X.
     * Don't mind me. LMAO.
     */

    public static String REPLOID_SERVICE_LEVEL = "reploid_service_level";

    /**
     * Check if kernel supports the service.
     * So far only Fresh Core supports it right now.
     **/
    public static boolean isReploidPresent() {
        File disableUSB;

        try {
            disableUSB = new File("/proc/sys/kernel/deny_new_usb");
        } catch (Exception e) {
            return false;
        }

        return disableUSB.exists();
    }

    /**
     * Check the service level of MaverickService.
     * 0 - allow everytime (disabled); 1 - allow when unlocked; 2 - disallow everytime
     **/
    public static int getReploidLevel(Context context) {
        return Settings.System.getInt(context.getContentResolver(), REPLOID_SERVICE_LEVEL, 1);
    }

    /**
     * Set the service level of MaverickService.
     **/
    public static void setReploidLevel(Context context, int protectionLevel) {
        Settings.System.putInt(context.getContentResolver(), REPLOID_SERVICE_LEVEL, protectionLevel);
        setMaverickService(context, false);
    }

    /**
     * Report MaverickService status to system. System scripts will handle it at this point.
     * 0 - allow everytime (disabled); 1 - allow when unlocked; 2 - disallow everytime
     **/
    public static void setMaverickService(Context context, boolean isLocked) {
        int level = getReploidLevel(context);

        switch (level) {
            case 1:
                ExperienceUtils.setProp("persist.fresh.security.maverick", isLocked ? "disable" : "enable");
                break;
            case 2:
                ExperienceUtils.setProp("persist.fresh.security.maverick", "disable");
                break;
            case 0:
                ExperienceUtils.setProp("persist.fresh.security.maverick", "enable");
                break;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startForeground(1768, Notifications.sendOngoingRenoirNotification(this));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean isLocked = intent.getBooleanExtra("isLocked", false);
        setMaverickService(this, isLocked);

        stopForeground(true);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
