package com.guyi.class25a_ands_2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class DeviceBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("pttt", "DeviceBootReceiver");

        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            MyReminder.startReminder(context);
        }
    }
}