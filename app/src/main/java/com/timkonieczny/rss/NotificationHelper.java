package com.timkonieczny.rss;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;

public class NotificationHelper extends ContextWrapper {

    NotificationManager notificationManager;

    public NotificationHelper(Context base) {
        super(base);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            setUpNotificationChannel();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setUpNotificationChannel(){
        NotificationChannel notificationChannel = new NotificationChannel("default",
                "Primary Channel", NotificationManager.IMPORTANCE_DEFAULT);
        notificationChannel.setLightColor(Color.MAGENTA);
        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        getNotificationManager().createNotificationChannel(notificationChannel);
    }

    public NotificationCompat.Builder getNotification(String title, String body) {
            return new NotificationCompat.Builder(getApplicationContext(), "default")
                    .setContentTitle(title)
                    .setContentText(body)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setAutoCancel(true);
    }

    public void notify(NotificationCompat.Builder notification) {
        getNotificationManager().notify(1, notification.build());
    }

    private NotificationManager getNotificationManager() {
        if (notificationManager == null)
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        return notificationManager;
    }
}
