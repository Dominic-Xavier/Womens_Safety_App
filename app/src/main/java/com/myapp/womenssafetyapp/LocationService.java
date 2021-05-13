package com.myapp.womenssafetyapp;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

public class LocationService extends Service {

    private final static String Chanel_Id = "10";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        automatedCalls();
        return START_STICKY;
    }

    private void automatedCalls() {

        createNotificationChannel();
        Notification notification = new NotificationCompat.Builder(this, Chanel_Id)
                .setContentTitle("Running")
                .setContentText("Reminder app is running")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(android.R.drawable.ic_lock_idle_low_battery)
                .setAutoCancel(false).build();
        startForeground(1, notification);
    }

    private void createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel(Chanel_Id,"Womens safety app is running", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
