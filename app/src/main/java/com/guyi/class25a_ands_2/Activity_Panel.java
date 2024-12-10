package com.guyi.class25a_ands_2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.gson.Gson;
import com.guyi.class25a_ands_2.databinding.ActivityPanelBinding;

public class Activity_Panel extends AppCompatActivity {

    private ActivityPanelBinding binding;

    private BroadcastReceiver locationBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String json = intent.getStringExtra(LocationService.BROADCAST_LOCATION_KEY);
            try {
                MyLoc myLoc = new Gson().fromJson(json, MyLoc.class);
                binding.textView.setText("Speed: " + myLoc.getSpeed() + " " + myLoc.getBearing());

            } catch (Exception exception) {
                exception.printStackTrace();
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPanelBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.FABAction1.setOnClickListener(v -> startTask());
        binding.FABAction2.setOnClickListener(v -> stopTask());
//        binding.button.setOnClickListener(v -> Log.d("pttt", "Button Clicked: " + LocationService.isMyServiceRunning(Activity_Panel.this)));
        binding.button.setOnClickListener(v -> {
            int i = Integer.valueOf("34m");
        });

        MyReminder.startReminder(this);
    }

    private void startTask() {
        MyDB.saveState(this, true);
        Intent intent = new Intent(this, LocationService.class);
        intent.setAction(LocationService.ACTION_START_SERVICE);
        startForegroundService(intent);
    }

    private void stopTask() {
        MyDB.saveState(this, false);
        Intent intent = new Intent(this, LocationService.class);
        intent.setAction(LocationService.ACTION_STOP_SERVICE);
        startForegroundService(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter(LocationService.BROADCAST_LOCATION);
        LocalBroadcastManager.getInstance(this).registerReceiver(locationBroadcastReceiver, intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(locationBroadcastReceiver);
    }
}