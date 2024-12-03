package com.guyi.class25a_ands_2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.guyi.class25a_ands_2.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.FABAction1.setOnClickListener(v -> startTask());
        binding.FABAction2.setOnClickListener(v -> stopTask());
        binding.button.setOnClickListener(v -> Log.d("pttt", "Button Clicked: " + LocationService.isMyServiceRunning(MainActivity.this)));
    }

    private void startTask() {
        Intent intent = new Intent(this, LocationService.class);
        intent.setAction(LocationService.ACTION_START_SERVICE);
        startService(intent);
    }

    private void stopTask() {
        Intent intent = new Intent(this, LocationService.class);
        intent.setAction(LocationService.ACTION_STOP_SERVICE);
        startService(intent);
    }
}