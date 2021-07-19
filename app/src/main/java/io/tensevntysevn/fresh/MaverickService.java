package io.tensevntysevn.fresh;

import android.app.KeyguardManager;
import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import com.topjohnwu.superuser.Shell;

import java.io.File;
import java.io.IOException;

import de.dlyt.yanndroid.fresh.Constants;

public class MaverickService {
    /* USB security service. Based on GrapheneOS' implementation.
     * Requires kernel support (we will have a check in here).
     * Literal. Freaking. References to MegaMan X.
     * Don't mind me. LMAO.
     */

    public static String REPLOID_SERVICE_LEVEL = "reploid_service_level";

    static {
        // Set settings before the main shell can be created
        Shell.enableVerboseLogging = Constants.DEBUGGING;
        Shell.setDefaultBuilder(Shell.Builder.create()
                .setFlags(Shell.FLAG_REDIRECT_STDERR)
                .setTimeout(10)
        );
    }

    /**
     * Check if kernel supports the service.
     * So far only Fresh Core supports it right now.
     **/
    public static boolean isReploidPresent() {
        return false;
        /*
        File disableUSB;
        if (Constants.DEBUGGING) Log.d("MaverickService", "Checking presence of Reploid");

        try {
            disableUSB = new File("/proc/sys/kernel/deny_new_usb");
        } catch (Exception e) {
            if (Constants.DEBUGGING) Log.d("MaverickService", "Failed checking presence");
            return false;
        }

        if (Constants.DEBUGGING) Log.d("MaverickService", "Success checking presence: " + disableUSB.exists());
        return disableUSB.exists();
         */
    }

    /**
     * Check the service level of MaverickService.
     * 0 - allow everytime (disabled); 1 - allow when unlocked; 2 - disallow everytime
     **/
    public static int getReploidLevel(Context context) {
        return Settings.System.getInt(context.getContentResolver(), REPLOID_SERVICE_LEVEL, isDeviceSecure(context) ? 1 : 0);
    }

    /**
     * Set the service level of MaverickService.
     **/
    public static void setReploidLevel(Context context, int protectionLevel) {
        Settings.System.putInt(context.getContentResolver(), REPLOID_SERVICE_LEVEL, protectionLevel);
        setMaverickService(context);
    }

    /**
     * Report MaverickService status to system. System scripts will handle it at this point.
     * 0 - allow everytime (disabled); 1 - allow when unlocked; 2 - disallow everytime
     **/
    public static void setMaverickService(Context context) {
        int level = getReploidLevel(context);
        String serviceMode;

        switch (level) {
            case 1:
                serviceMode = "dynamic";
                break;
            case 2:
                serviceMode = "enable";
                break;
            default:
                serviceMode = "disable";
                break;
        }

        Shell.getShell(shell -> {
            Shell.sh("/system/bin/maverickd " + serviceMode).submit();
        });
    }

    /**
     * @return true if pattern set, false if not (or if an issue when checking)
     */
    public static boolean isDeviceSecure(Context context) {
        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        return keyguardManager.isDeviceSecure();
    }
}
