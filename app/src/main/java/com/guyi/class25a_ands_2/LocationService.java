package com.guyi.class25a_ands_2;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;

import java.util.List;


public class LocationService extends Service {

    public static final String ACTION_START_SERVICE = "ACTION_START_SERVICE";
    public static final String ACTION_PAUSE_SERVICE = "ACTION_PAUSE_SERVICE";
    public static final String ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE";

    private int counter = 0;

    private boolean isServiceRunningRightNow = false;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("pttt", "onStartCommand()");

        if (intent == null) {
            return START_STICKY;
        }






        String action = intent.getAction();
        Log.d("pttt", "action = " + action);

        if (action.equals(ACTION_START_SERVICE)) {
            if (isServiceRunningRightNow) {
                return START_STICKY;
            }

            isServiceRunningRightNow = true;
            startRecording();
        } else if (action.equals(ACTION_STOP_SERVICE)) {
            stopRecording();
            stopSelf();
            isServiceRunningRightNow = false;
        }


        //new Thread(() -> runLongProcess()).start();





        return START_STICKY;
    }

    private void startRecording() {
        MCT6.get().cycle(new MCT6.CycleTicker() {
            @Override
            public void secondly(int repeatsRemaining) {
                Log.d("pttt", "Tick " + counter++);
            }

            @Override
            public void done() {

            }
        }, MCT6.CONTINUOUSLY_REPEATS, 2000);
    }

    private void stopRecording() {
        MCT6.get().removeAll();
    }


    private void runLongProcess() {
        Log.d("pttt", "runLongProcess - " + Thread.currentThread().getName());
        int x = 0;
        for (int i = 0; i < 2_000; i++) {
            int y = i;
            int z = 0;
            for (int j = 0; j < 1_000_000; j++) {
                z = j;
                x += (j % 2 == 0) ? j + y : -j - + z;
            }
        }
        Log.d("pttt", "Done " + x);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static boolean isMyServiceRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runs = manager.getRunningServices(Integer.MAX_VALUE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (LocationService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}