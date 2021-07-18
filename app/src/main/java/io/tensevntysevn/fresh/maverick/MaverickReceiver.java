package io.tensevntysevn.fresh.maverick;

import android.content.Context;
import android.content.Intent;

import android.content.BroadcastReceiver;

import de.dlyt.yanndroid.fresh.renoir.RenoirService;

public class MaverickReceiver extends BroadcastReceiver {
    /* USB security receiver.
     * Requires kernel support (we have a check).
     */
    public static void runMaverickService(Context context, boolean isLocked) {
        Intent service = new Intent(context, MaverickService.class);
        service.putExtra("isLocked", isLocked);
        context.startService(service);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        // Only run service if it's present
        if (MaverickService.isReploidPresent()) {
            if (action.equals("android.intent.action.USER_PRESENT")) {
                runMaverickService(context, false);
            } else if (action.equals("android.intent.action.SCREEN_OFF")) {
                runMaverickService(context, true);
            }
        }
    }
}
