package com.pvnptl.exploringreddit.accessibilityservice;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

public class Notifier {

    private static Notifier mInstance = null;

    private NotificationManager mNotificationManager;

    public static final int TYPE_EXPLORING_REDDIT_SERVICE_RUNNING = 1;

    private static final String TAG = "Notifier";

    public static synchronized Notifier getInstance() {
        if (mInstance == null) {
            mInstance = new Notifier();
        }
        return mInstance;
    }

    private Notifier() {
        this.mNotificationManager = (NotificationManager) AccessibilityServiceApplication.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void cleanAll() {
        if (mNotificationManager != null) {
            mNotificationManager.cancelAll();
        }
    }

    public void cancelByType(int type) {
        if (mNotificationManager != null) {
            mNotificationManager.cancel(type);
        }
    }

    public void notify(String title, String message, String subName, int type, boolean canClear) {
        try {
            Context context = AccessibilityServiceApplication.getInstance();
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

            Intent intent = new Intent();
            PendingIntent contentIntent = null;
            switch (type) {
                case TYPE_EXPLORING_REDDIT_SERVICE_RUNNING:
                    intent.setComponent(new ComponentName("com.pvnptl.exploringreddit", "com.pvnptl.exploringreddit.activity.HomeActivity"));
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    intent.putExtra("subredditname", subName);
                    contentIntent = PendingIntent.getActivity(context, TYPE_EXPLORING_REDDIT_SERVICE_RUNNING, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    break;
            }

            builder.setSmallIcon(R.drawable.ic_launcher)
                    .setDefaults(Notification.DEFAULT_LIGHTS)
                    .setTicker(message)
                    .setWhen(System.currentTimeMillis())
                    .setContentTitle(title)
                    .setContentText(message);

            if (Build.VERSION.SDK_INT >= 16) {
                builder.setPriority(Notification.PRIORITY_MAX);
            }

            if (contentIntent != null) {
                Notification nt = builder.setContentIntent(contentIntent).build();

                if (canClear)
                    nt.flags |= Notification.FLAG_AUTO_CANCEL;
                else
                    nt.flags |= Notification.FLAG_NO_CLEAR;

                mNotificationManager.notify(type, nt);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
