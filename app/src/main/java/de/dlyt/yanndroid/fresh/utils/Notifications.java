package de.dlyt.yanndroid.fresh.utils;

import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;

import de.dlyt.yanndroid.fresh.Constants;
import de.dlyt.yanndroid.fresh.database.TnsOta;
import de.dlyt.yanndroid.fresh.R;
import de.dlyt.yanndroid.fresh.hub.AvailableActivity;
import de.dlyt.yanndroid.fresh.hub.MainActivity;

public class Notifications implements Constants {
    public static void sendPreUpdate(Context context) {
        if (DEBUGGING) Log.d(Tools.TAG, "Showing notification");

        String CHANNEL_ID = context.getString(R.string.system_notification_channel_id);
        int notificationColor = context.getResources().getColor(R.color.sesl_primary_color);
        String version = TnsOta.getReleaseVersion(context);
        String updateVariant = TnsOta.getReleaseVariant(context);

        NotificationManager mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID);
        Intent resultIntent = new Intent(context, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);

        Intent downloadIntent = new Intent(context, AvailableActivity.class);
        PendingIntent hubPendingIntent = PendingIntent.getActivity(context, 0, downloadIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        String notificationTitle = context.getString(R.string.system_notification_available_title);
        String notificationContent = context.getString(R.string.available_update_install_ready, version, updateVariant);

        mBuilder.setContentTitle(notificationTitle)
                .setContentText(notificationContent)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(notificationContent))
                .setSmallIcon(R.drawable.ic_notif)
                .setColor(notificationColor)
                .setContentIntent(hubPendingIntent)
                .setAutoCancel(true)
                .setShowWhen(false)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setDefaults(NotificationCompat.DEFAULT_LIGHTS)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setCategory(NotificationCompat.CATEGORY_SYSTEM);

        mNotifyManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    public static void sendPostUpdateNotification(Context context, String version, Boolean successful) {
        if (DEBUGGING) Log.d(Tools.TAG, "Showing notification");

        String CHANNEL_ID = context.getString(R.string.system_notification_channel_id);
        int notificationColor = context.getResources().getColor(R.color.sesl_primary_color);
        String updateVariant = TnsOta.getReleaseVariant(context);

        NotificationManager mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID);
        Intent resultIntent = new Intent(context, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);

        Intent hubIntent = new Intent(context, MainActivity.class);
        PendingIntent hubPendingIntent = PendingIntent.getActivity(context, 0, hubIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        String notificationTitle = context.getString(R.string.system_notification_failed_title);
        String notificationContent = context.getString(R.string.system_notification_failed_desc, version, updateVariant);

        if (successful) {
            notificationTitle = context.getString(R.string.system_notification_finish_title);
            notificationContent = context.getString(R.string.system_notification_finish_desc, version, updateVariant);
        }

        mBuilder.setContentTitle(notificationTitle)
                .setContentText(notificationContent)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(notificationContent))
                .setSmallIcon(R.drawable.ic_notif)
                .setColor(notificationColor)
                .setContentIntent(hubPendingIntent)
                .setAutoCancel(true)
                .setShowWhen(false)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setDefaults(NotificationCompat.DEFAULT_LIGHTS)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setCategory(NotificationCompat.CATEGORY_SYSTEM);

        mNotifyManager.notify(NOTIFICATION_POST_UPDATE_ID, mBuilder.build());
    }

    public static void sendUpdateNotification(Context context, String version, String variant) {
        if (DEBUGGING) Log.d(Tools.TAG, "Showing notification");

        String CHANNEL_ID = context.getString(R.string.system_notification_channel_id);
        int notificationColor = context.getResources().getColor(R.color.sesl_primary_color);

        NotificationManager mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID);
        Intent resultIntent = new Intent(context, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);

        Intent downloadIntent = new Intent(context, AvailableActivity.class);
        PendingIntent downloadPendingIntent = PendingIntent.getActivity(context, 0, downloadIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentTitle(context.getString(R.string.system_notification_available_title))
                .setContentText(context.getString(R.string.system_notification_available_desc, version, variant))
                .setSmallIcon(R.drawable.ic_notif)
                .setColor(notificationColor)
                .setContentIntent(downloadPendingIntent)
                .setAutoCancel(true)
                .setShowWhen(false)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_LIGHTS)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setCategory(NotificationCompat.CATEGORY_SYSTEM);

        mNotifyManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    public static void sendOngoingCheckNotification(Context context) {
        if (DEBUGGING) Log.d(Tools.TAG, "Showing notification");

        String CHANNEL_ID = context.getString(R.string.system_notification_ongoing_channel_id);
        int notificationColor = context.getResources().getColor(R.color.sesl_primary_color);

        NotificationManager mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID);

        mBuilder.setContentTitle(context.getString(R.string.system_settings_plugin_title))
                .setContentText(context.getString(R.string.main_checking_updates))
                .setSmallIcon(R.drawable.ic_notif)
                .setColor(notificationColor)
                .setAutoCancel(false)
                .setOngoing(true)
                .setShowWhen(false)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setDefaults(NotificationCompat.DEFAULT_LIGHTS)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setCategory(NotificationCompat.CATEGORY_SYSTEM);

        mNotifyManager.notify(NOTIFICATION_ONGOING_ID, mBuilder.build());
    }

    public static void cancelOngoingCheckNotification(Context context) {
        NotificationManager mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifyManager.cancel(NOTIFICATION_ONGOING_ID);
    }

    public static void cancelUpdateNotification(Context context) {
        NotificationManager mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifyManager.cancel(NOTIFICATION_ID);
    }

    public static void setupNotificationChannel(Context context) {
        NotificationManager mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        String GROUP_ID = "tns_ota_group";
        CharSequence groupName = context.getString(R.string.system_notification_group_title);
        String description = context.getString(R.string.system_notification_channel_desc);
        NotificationChannelGroup notificationGroup = new NotificationChannelGroup(GROUP_ID, groupName);

        CharSequence name = context.getString(R.string.system_notification_channel_title);
        String CHANNEL_ID = context.getString(R.string.system_notification_channel_id);
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setGroup(GROUP_ID);
        channel.setDescription(description);
        mNotifyManager.createNotificationChannelGroup(notificationGroup);
        mNotifyManager.createNotificationChannel(channel);
    }

    public static void setupOngoingNotificationChannel(Context context) {
        NotificationManager mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        String GROUP_ID = "tns_ota_group";
        CharSequence groupName = context.getString(R.string.system_notification_group_title);
        String description = context.getString(R.string.system_notification_channel_desc);
        NotificationChannelGroup notificationGroup = new NotificationChannelGroup(GROUP_ID, groupName);

        CharSequence name = context.getString(R.string.system_notification_channel_ongoing_title);
        String CHANNEL_ID = context.getString(R.string.system_notification_ongoing_channel_id);
        int importance = NotificationManager.IMPORTANCE_MIN;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setGroup(GROUP_ID);
        channel.setDescription(description);
        mNotifyManager.createNotificationChannelGroup(notificationGroup);
        mNotifyManager.createNotificationChannel(channel);
    }
}
