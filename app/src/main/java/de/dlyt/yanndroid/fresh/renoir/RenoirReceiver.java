package de.dlyt.yanndroid.fresh.renoir;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class RenoirReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action.equals("android.intent.action.WALLPAPER_CHANGED") ||
                action.equals("com.samsung.android.theme.themecenter.THEME_APPLY") ||
                action.equals("com.samsung.android.theme.themecenter.THEME_APPLY_START")) {
            // Check if Fresh build is compatible with Renoir before bothering with anything
            if (RenoirService.isFreshBuildEligibleForRenoir(context)) {
                // Check for Renoir being enabled
                if (RenoirService.getRenoirEnabled(context)) {
                    runRenoir(context);
                }
            }
        }
    }

    public static void runRenoir(Context context) {
        Intent serviceIntent = new Intent(context, RenoirService.class);
        context.startForegroundService(serviceIntent);
    }
}
