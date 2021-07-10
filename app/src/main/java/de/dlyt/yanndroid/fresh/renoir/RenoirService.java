package de.dlyt.yanndroid.fresh.renoir;

import android.app.Activity;
import android.app.Service;
import android.app.WallpaperColors;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;

import de.dlyt.yanndroid.fresh.Constants;
import de.dlyt.yanndroid.fresh.R;
import de.dlyt.yanndroid.fresh.utils.Notifications;
import io.tensevntysevn.fresh.ExperienceUtils;
import io.tensevntysevn.fresh.OverlayService;

import com.github.tommyettinger.colorful.TrigTools;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RenoirService extends Service {
    private static final String PREF_NAME = "fresh_system_settings";
    private static final String RENOIR_CURRENT_COLOR_THEME = "renoir_current_system_theme";
    public static String RENOIR_SERVICE_ENABLED = "renoir_enabled";
    public static String RENOIR_DEFAULT_THEME = "io.tns.fresh.renoir.one";
    public static String RENOIR_WALLPAPER_BASED_ON_LOCK = "renoir_color_based_on_lock_screen";
    public static String RENOIR_SAMSUNG_THEME_APPLIED = "renoir_third_party_theme_applied";

    public static String getColorScheme(Context context, WallpaperColors wallpaperDrawable) {
        /* Initialize our array to get all available colors */
        int[] mRenoirColors = context.getResources().getIntArray(R.array.renoir_color_resources);
        String[] mRenoirPackages = context.getResources().getStringArray(R.array.renoir_package_resources);

        int bestRenoirPackage = 0; // package with the best score; set to default
        int colorToCompare;
        float renoirScore; // current score
        float currentBestScore = Float.MAX_VALUE; // current best score

        final Color primaryColor = wallpaperDrawable.getPrimaryColor();
        final Color secondaryColor = wallpaperDrawable.getSecondaryColor();

        if (isColorNonDeterminant(primaryColor) && (secondaryColor == null || isColorNonDeterminant(secondaryColor))) {
            if (Constants.DEBUGGING)
                Log.i("RenoirService", "Wallpaper colors are non-determinant. Using defaults.");
            return mRenoirPackages[bestRenoirPackage]; // use default color if secondary color is null or non-determinant AND primary is non-determinant
        } else if (isColorNonDeterminant(primaryColor) && !(secondaryColor == null)) {
            if (Constants.DEBUGGING)
                Log.i("RenoirService", "Primary wallpaper color is non-determinant. Using secondary.");
            colorToCompare = secondaryColor.toArgb(); // use secondary color if primary is non-determinant, as long as it's not null
        } else {
            if (Constants.DEBUGGING)
                Log.i("RenoirService", "Primary wallpaper color is determinant. Using primary.");
            colorToCompare = primaryColor.toArgb(); // use primary color if it is determinant
        }

        if (Constants.DEBUGGING)
            Log.i("RenoirService", "Wallpaper color in HEX: #" + Integer.toHexString(colorToCompare));

        for (int i = 0; i < mRenoirColors.length; i++) {
            int renoirColor = Color.valueOf(mRenoirColors[i]).toArgb();
            renoirScore = getRenoirScore(renoirColor, colorToCompare);

            if (renoirScore < currentBestScore) {
                currentBestScore = renoirScore;
                bestRenoirPackage = i;
            }

            if (Constants.DEBUGGING)
                Log.i("RenoirService", mRenoirPackages[i] + ": " + renoirScore);
        }

        return mRenoirPackages[bestRenoirPackage];
    }

    /**
     * Computes the difference between two RGB colors by converting them to the L*a*b scale and
     * comparing them using the CIEDE2000 algorithm { http://en.wikipedia.org/wiki/Color_difference#CIEDE2000}
     */
    public static float getRenoirScore(int colorA, int colorB) {
        double[] labX = new double[3];
        double[] labY = new double[3];

        // Convert colors into L*a*b before scoring them
        ColorUtils.colorToLAB(colorA, labX);
        ColorUtils.colorToLAB(colorB, labY);

        // L*a*b for colorA
        double L1 = labX[0];
        double a1 = labX[1];
        double b1 = labX[2];

        // L*a*b for colorB
        double L2 = labY[0];
        double a2 = labY[1];
        double b2 = labY[2];

        double Lmean = (L1 + L2) / 2.0;
        double C1 = Math.sqrt(a1 * a1 + b1 * b1);
        double C2 = Math.sqrt(a2 * a2 + b2 * b2);
        double Cmean = (C1 + C2) / 2.0;

        double G = (1 - Math.sqrt(Math.pow(Cmean, 7) / (Math.pow(Cmean, 7) + Math.pow(25, 7)))) / 2;
        double a1prime = a1 * (1 + G);
        double a2prime = a2 * (1 + G);

        double C1prime = Math.sqrt(a1prime * a1prime + b1 * b1);
        double C2prime = Math.sqrt(a2prime * a2prime + b2 * b2);
        double Cmeanprime = (C1prime + C2prime) / 2;

        double h1prime = Math.atan2(b1, a1prime) + 2 * Math.PI * (Math.atan2(b1, a1prime) < 0 ? 1 : 0);
        double h2prime = Math.atan2(b2, a2prime) + 2 * Math.PI * (Math.atan2(b2, a2prime) < 0 ? 1 : 0);
        double Hmeanprime = ((Math.abs(h1prime - h2prime) > Math.PI) ? (h1prime + h2prime + 2 * Math.PI) / 2 : (h1prime + h2prime) / 2);

        double T = 1.0 - 0.17 * Math.cos(Hmeanprime - Math.PI / 6.0) + 0.24 * Math.cos(2 * Hmeanprime) + 0.32 * Math.cos(3 * Hmeanprime + Math.PI / 30) - 0.2 * Math.cos(4 * Hmeanprime - 21 * Math.PI / 60);

        double deltahprime = ((Math.abs(h1prime - h2prime) <= Math.PI) ? h2prime - h1prime : (h2prime <= h1prime) ? h2prime - h1prime + 2 * Math.PI : h2prime - h1prime - 2 * Math.PI);

        double deltaLprime = L2 - L1;
        double deltaCprime = C2prime - C1prime;
        double deltaHprime = 2.0 * Math.sqrt(C1prime * C2prime) * Math.sin(deltahprime / 2.0);
        double SL = 1.0 + ((0.015 * (Lmean - 50) * (Lmean - 50)) / (Math.sqrt(20 + (Lmean - 50) * (Lmean - 50))));
        double SC = 1.0 + 0.045 * Cmeanprime;
        double SH = 1.0 + 0.015 * Cmeanprime * T;

        double deltaTheta = (30 * Math.PI / 180) * Math.exp(-((180 / Math.PI * Hmeanprime - 275) / 25) * ((180 / Math.PI * Hmeanprime - 275) / 25));
        double RC = (2 * Math.sqrt(Math.pow(Cmeanprime, 7) / (Math.pow(Cmeanprime, 7) + Math.pow(25, 7))));
        double RT = (-RC * Math.sin(2 * deltaTheta));

        double KL = 1;
        double KC = 1;
        double KH = 1;

        return (float) Math.sqrt(
                ((deltaLprime / (KL * SL)) * (deltaLprime / (KL * SL))) +
                        ((deltaCprime / (KC * SC)) * (deltaCprime / (KC * SC))) +
                        ((deltaHprime / (KH * SH)) * (deltaHprime / (KH * SH))) +
                        (RT * (deltaCprime / (KC * SC)) * (deltaHprime / (KH * SH)))
        );
    }

    private static boolean isColorNonDeterminant(Color color) {
        if (color == null) return true;
        float luminance = color.luminance();

        if (Constants.DEBUGGING) Log.i("RenoirService", "Determinant: " + luminance);
        return luminance < 0.025 || luminance > 0.895;
    }

    public static void setSystemColorTheme(Context context, String packageName, Boolean isSamsungThemeApplied) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
        final String oldOverlay = getSystemColorTheme(context); // Get old overlay to disable
        boolean wasSamsungThemeApplied = isGalaxyThemeCached(context);

        sharedPreferences.edit().putBoolean(RENOIR_SAMSUNG_THEME_APPLIED, isSamsungThemeApplied).apply();
        if (Constants.DEBUGGING) Log.i("RenoirService", "Renoir overlay set: " + packageName);

        ExecutorService mExecutor = Executors.newCachedThreadPool();

        mExecutor.execute(() -> {
            sharedPreferences.edit().putString(RENOIR_CURRENT_COLOR_THEME, packageName).apply();

            if (isSamsungThemeApplied)
                configureCorePackages(context, false);

            if (wasSamsungThemeApplied)
                configureCorePackages(context, true);

            if (!packageName.equals("disable")) {
                OverlayService.setOverlayState(packageName, true);
            }

            OverlayService.setOverlayState(oldOverlay, false);
        });
    }

    private static String getSystemColorTheme(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
        return sharedPreferences.getString(RENOIR_CURRENT_COLOR_THEME, RENOIR_DEFAULT_THEME);
    }

    public static void setColorBasedOnLock(Context context, Boolean bool) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean(RENOIR_WALLPAPER_BASED_ON_LOCK, bool).apply();
        RenoirReceiver.runRenoir(context);
    }

    public static Boolean getColorBasedOnLock(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
        return sharedPreferences.getBoolean(RENOIR_WALLPAPER_BASED_ON_LOCK, false);
    }

    public static void setRenoirEnabled(Context context, Boolean bool) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
        PackageManager packageManager = context.getPackageManager();
        ComponentName wallpaperChangeIntent = new ComponentName(context, RenoirReceiver.class);

        sharedPreferences.edit().putBoolean(RENOIR_SERVICE_ENABLED, bool).commit();

        if (bool) {
            RenoirReceiver.runRenoir(context);
            configureCorePackages(context, true);
        } else {
            configureCorePackages(context, false);
            setSystemColorTheme(context, "disable", false);
        }

        packageManager.setComponentEnabledSetting(wallpaperChangeIntent,
                bool ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

    public static Boolean getRenoirEnabled(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
        return sharedPreferences.getBoolean(RENOIR_SERVICE_ENABLED, false);
    }

    public static boolean isGalaxyThemeCached(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
        return sharedPreferences.getBoolean(RENOIR_SAMSUNG_THEME_APPLIED, false);
    }

    public static boolean isFreshBuildEligibleForRenoir(Context context) {
        PackageManager packageManager = context.getPackageManager();
        return packageManager.hasSystemFeature("io.tensevntysevn.fresh.sainetto");
    }

    public static void configureCorePackages(Context context, Boolean state) {
        ExecutorService mExecutor = Executors.newCachedThreadPool();
        String[] mRenoirCorePackages = context.getResources().getStringArray(R.array.renoir_core_package_resources);

        mExecutor.execute(() -> {
            for (String mRenoirCorePackage : mRenoirCorePackages) {
                OverlayService.setOverlayState(mRenoirCorePackage, state);
            }
        });
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startForeground(1767, Notifications.sendOngoingRenoirNotification(this));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (getRenoirEnabled(this)) {
            if (ExperienceUtils.isGalaxyThemeApplied(this)) {
                // Skip everything if a third-party theme is applied. Just apply default then finish
                if (!isGalaxyThemeCached(this))
                    setSystemColorTheme(this, "disable", true); // Only apply overlay if status is not cached
                return START_NOT_STICKY;
            }

            /* Get the wallpaper */
            final WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
            WallpaperColors wallpaperDrawable = wallpaperManager.getWallpaperColors(WallpaperManager.FLAG_SYSTEM);

            SystemClock.sleep(900);

            if (getColorBasedOnLock(this) && !ExperienceUtils.isLsWallpaperUnavailable(this)) {
                wallpaperDrawable = wallpaperManager.getWallpaperColors(WallpaperManager.FLAG_LOCK);
            }

            String nextOverlay = getColorScheme(this, wallpaperDrawable);
            String currentOverlay = getSystemColorTheme(this);

            if (!currentOverlay.equals(nextOverlay))
                setSystemColorTheme(this, nextOverlay, false);
        }

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
