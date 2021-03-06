package io.tensevntysevn.fresh;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;

import de.dlyt.yanndroid.fresh.settings.sub.ScreenResolutionActivity;

public class ExperienceUtils {

    public static boolean isGalaxyThemeApplied(Context context) {
        String themePackage = Settings.System.getString(context.getContentResolver(),
                "current_sec_active_themepackage");
        String themePackageVersion = Settings.System.getString(context.getContentResolver(),
                "current_sec_active_themepackage_version");

        final boolean isThemePackageMissing = themePackage == null || themePackage.equals("");
        final boolean isThemeVersionMissing = themePackageVersion == null || themePackageVersion.equals("");

        // Return true if these setting keys are not null or blank
        return !isThemePackageMissing && !isThemeVersionMissing;
    }

    public static boolean isLsWallpaperUnavailable(Context context) {
        Integer pluginWallpaperType = Settings.Secure.getInt(context.getContentResolver(),
                    "plugin_lock_wallpaper_type", 0);

        return pluginWallpaperType > 0;
    }

    public static boolean isVideoEnhancerEnabled(Context context) {
        int enhancerEnabled = Settings.System.getInt(context.getContentResolver(),
                "hdr_effect", 0);

        return enhancerEnabled == 1;
    }

    public static int getRealScreenWidth(Context context, Activity activity) {
        String PREF_NAME = "fresh_system_settings";
        String SCREEN_RESOLUTION = "device_screen_resolution_int";

        int realScreenResolution = 0;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getRealMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;

       if (width >= 1080) {
            realScreenResolution = 2;
        } else if (width >= 720) {
           realScreenResolution = 1;
       }

       Settings.System.putInt(context.getContentResolver(), SCREEN_RESOLUTION, realScreenResolution);
       return realScreenResolution;
    }

    public static void setVideoEnhancerEnabled(Context context, Boolean bool) {
        Settings.System.putInt(context.getContentResolver(), "hdr_effect", bool ? 1 : 0);
    }

    public static void setBypassBlacklist(Context context, boolean bool) {
        /* List of Global settings that allow blacklisted APIs to be called */
        String[] blacklistGlobalSettings = {
                "hidden_api_policy",
                "hidden_api_policy_pre_p_apps",
                "hidden_api_policy_p_apps"
        };

        SharedPreferences sharedPreferencesBp = context.getSharedPreferences(ScreenResolutionActivity.PREF_NAME, Activity.MODE_PRIVATE);
        int default_restricted_api_setting = sharedPreferencesBp.getInt(ScreenResolutionActivity.RESTRICTED_API, 0);

        if (bool) {
            for (String setting : blacklistGlobalSettings) {
                Settings.Global.putInt(context.getContentResolver(), setting, 1);
            }
        } else {
            for (String setting : blacklistGlobalSettings) {
                Settings.Global.putInt(context.getContentResolver(), setting, default_restricted_api_setting);
            }
        }
    }

    public static void checkDefaultApiSetting(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(ScreenResolutionActivity.PREF_NAME, Activity.MODE_PRIVATE);

        try {
            int privateApiSetting = Settings.Global.getInt(context.getContentResolver(), "hidden_api_policy");
            sharedPreferences.edit().putInt(ScreenResolutionActivity.RESTRICTED_API, privateApiSetting).apply();
        } catch (Exception ignored) { /* Fail */ }
    }

    @SuppressWarnings({"JavaReflectionMemberAccess", "rawtypes"})
    public static boolean isDesktopMode(Context context) {
        boolean enabled;
        Configuration config = context.getResources().getConfiguration();
        try {
            Class configClass = config.getClass();
            enabled = configClass.getField("SEM_DESKTOP_MODE_ENABLED").getInt(configClass)
                    == configClass.getField("semDesktopModeEnabled").getInt(config);
            return enabled;
        } catch (NoSuchFieldException | IllegalAccessException | IllegalArgumentException ignored) {
            // Ignored
        }

        return false;
    }

    public static boolean isPackageInstalled(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();

        try {
            packageManager.getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static void stopPackage(Context context, String packageName) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        boolean packageInstalled = isPackageInstalled(context, packageName);
        if (packageInstalled) {
            try {
                @SuppressLint("PrivateApi")
                Method forceStopPackage = am.getClass()
                        .getDeclaredMethod("forceStopPackage", String.class);
                forceStopPackage.setAccessible(true);
                forceStopPackage.invoke(am, packageName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static String getProp(String propName) {
        Process p;
        String result = "";
        try {
            p = new ProcessBuilder("/system/bin/getprop", propName).redirectErrorStream(true)
                    .start();
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                result = line;
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
