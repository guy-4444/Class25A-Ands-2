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

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.gson.Gson;

import java.util.List;


public class LocationService extends Service {

    public static final String ACTION_START_SERVICE = "ACTION_START_SERVICE";
    public static final String ACTION_PAUSE_SERVICE = "ACTION_PAUSE_SERVICE";
    public static final String ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE";

    public static final String BROADCAST_LOCATION = "BROADCAST_LOCATION";
    public static final String BROADCAST_LOCATION_KEY = "BROADCAST_LOCATION_KEY";

    public static int NOTIFICATION_ID = 168;
    private int lastShownNotificationId = -1;
    public static String CHANNEL_ID = "com.guyi.class25a_ands_2.CHANNEL_ID_FOREGROUND";
    public static String MAIN_ACTION = "com.guyi.class25a_ands_2.locationservice.action.main";
    private NotificationCompat.Builder notificationBuilder;


    private int counter = 0;

    private boolean isServiceRunningRightNow = false;

    private FusedLocationProviderClient fusedLocationProviderClient;

    private PowerManager.WakeLock wakeLock;
    private PowerManager powerManager;

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
            notifyToUserForForegroundService();
            startRecording();
        } else if (action.equals(ACTION_STOP_SERVICE)) {
            stopRecording();
            stopForeground(true);
            stopSelf();
            isServiceRunningRightNow = false;
        }


        //new Thread(() -> runLongProcess()).start();





        return START_STICKY;
    }

    private void startRecording() {
        // Keep CPU working
        powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "PassiveApp:tag");
        wakeLock.acquire();

        MCT6.get().cycle(new MCT6.CycleTicker() {
            @Override
            public void secondly(int repeatsRemaining) {
                Log.d("pttt", "Tick " + counter++);
            }

            @Override
            public void done() {

            }
        }, MCT6.CONTINUOUSLY_REPEATS, 2000);

        // Run GPS
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationRequest locationRequest = new LocationRequest.Builder(1000)
                    .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                    .setMinUpdateDistanceMeters(1.0f)
                    .setMinUpdateIntervalMillis(1000)
//                    .setMaxUpdateDelayMillis(10000)
                    .build();

            // new Google API SDK v11 uses getFusedLocationProviderClient(this)
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        }
    }

    private void stopRecording() {
        wakeLock.release();

        MCT6.get().removeAll();


    }

    private LocationListener locationCallback = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            double lat = location.getLatitude();
            double lon = location.getLongitude();
            double altitude = location.getAltitude();
            float speed = location.getSpeed() * 3.6f;
            float bearing = location.getBearing();

            Log.d("pttt", lon + ", " + lat);

            MyLoc myLoc = new MyLoc()
                    .setLat(lat)
                    .setLon(lon)
                    .setAltitude(altitude)
                    .setBearing(bearing)
                    .setSpeed(speed);

            String json = new Gson().toJson(myLoc);
            Intent intent = new Intent(BROADCAST_LOCATION);
            intent.putExtra(BROADCAST_LOCATION_KEY, json);
            LocalBroadcastManager.getInstance(LocationService.this).sendBroadcast(intent);

            updateNotification("Speed: " + myLoc.getSpeed() + " " + myLoc.getBearing());
        }
    };

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


    // // // // // // // // // // // // // // // // Notification  // // // // // // // // // // // // // // //

    private void notifyToUserForForegroundService() {
        // On notification click
        Intent notificationIntent = new Intent(this, Activity_Panel.class);
        notificationIntent.setAction(MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, NOTIFICATION_ID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        notificationBuilder = getNotificationBuilder(this,
                CHANNEL_ID,
                NotificationManagerCompat.IMPORTANCE_LOW); //Low importance prevent visual appearance for this notification channel on top

        notificationBuilder
                .setContentIntent(pendingIntent) // Open activity
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_cycling)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round))
                .setContentTitle("App in progress")
                .setContentText("Content")
        ;

        Notification notification = notificationBuilder.build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
        }

        if (NOTIFICATION_ID != lastShownNotificationId) {
            // Cancel previous notification
            final NotificationManager notificationManager = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
            notificationManager.cancel(lastShownNotificationId);
        }
        lastShownNotificationId = NOTIFICATION_ID;
    }

    public static NotificationCompat.Builder getNotificationBuilder(Context context, String channelId, int importance) {
        NotificationCompat.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            prepareChannel(context, channelId, importance);
            builder = new NotificationCompat.Builder(context, channelId);
        } else {
            builder = new NotificationCompat.Builder(context);
        }
        return builder;
    }

    @TargetApi(26)
    private static void prepareChannel(Context context, String id, int importance) {
        final String appName = context.getString(R.string.app_name);
        String notifications_channel_description = "Cycling app location channel";
        final NotificationManager nm = (NotificationManager) context.getSystemService(Service.NOTIFICATION_SERVICE);

        if(nm != null) {
            NotificationChannel nChannel = nm.getNotificationChannel(id);

            if (nChannel == null) {
                nChannel = new NotificationChannel(id, appName, importance);
                nChannel.setDescription(notifications_channel_description);

                // from another answer
                nChannel.enableLights(true);
                nChannel.setLightColor(Color.BLUE);

                nm.createNotificationChannel(nChannel);
            }
        }
    }

    private void updateNotification(String content) {
        notificationBuilder.setContentText(content);
        final NotificationManager notificationManager = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

}