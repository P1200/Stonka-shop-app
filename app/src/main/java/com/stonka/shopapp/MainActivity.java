package com.stonka.shopapp;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.stonka.shopapp.databinding.ActivityMainBinding;
import com.stonka.shopapp.ui.ShakeomatActivity;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private ActivityMainBinding binding;
    private Button logoutButton;
    private FirebaseAuth mAuth;
    private Button loginButton;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private static final float SHAKE_THRESHOLD = 12.0f;
    private long lastShakeTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(event -> {
            Intent myIntent = new Intent(MainActivity.this, LoginFragment.class);
            MainActivity.this.startActivity(myIntent);
        });

        mAuth = FirebaseAuth.getInstance();
        logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(MainActivity.this, LoginFragment.class));
            finish();
        });

        // Konfiguracja Shakeomatu
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mAuth.getCurrentUser() != null) {
            loginButton.setVisibility(BottomNavigationView.GONE);
            logoutButton.setVisibility(BottomNavigationView.VISIBLE);
        } else {
            logoutButton.setVisibility(BottomNavigationView.GONE);
            loginButton.setVisibility(BottomNavigationView.VISIBLE);
        }

        // Rejestracja nasłuchiwania czujnika
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Wyrejestrowanie nasłuchiwania czujnika
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            double acceleration = Math.sqrt(x * x + y * y + z * z);
            long currentTime = System.currentTimeMillis();

            if (acceleration > SHAKE_THRESHOLD && (currentTime - lastShakeTime > 1000)) {
                lastShakeTime = currentTime;
                Toast.makeText(this, "Potrząśnięcie wykryte! Uruchamianie Shakeomatu...", Toast.LENGTH_SHORT).show();
                openShakeomat();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void openShakeomat() {
        Intent intent = new Intent(this, ShakeomatActivity.class);
        startActivity(intent);
    }


}
