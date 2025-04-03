package com.stonka.shopapp.ui.shakeomat;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.stonka.shopapp.databinding.FragmentShakeomatBinding;

import java.util.Random;
import java.util.UUID;

public class ShakeomatFragment extends Fragment implements SensorEventListener {

    private static final float SHAKE_THRESHOLD = 12.0f;
    private final String[] rewards = {"10% rabatu!", "Darmowa dostawa!", "20% zniżki na kolejne zakupy!", "Bonusowy produkt!"};

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private long lastShakeTime = 0;
    private FragmentShakeomatBinding binding;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private TextView textView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ShakeomatViewModel shakeomatViewModel =
                new ViewModelProvider(this).get(ShakeomatViewModel.class);

        binding = FragmentShakeomatBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        textView = binding.textShakeomat;
        shakeomatViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            textView.setText("Aby otrzymać promocję musisz się zalogować!");
        } else {
            textView.setText("Potrząśnij aby otrzymać nagrodę!");

            databaseReference = FirebaseDatabase.getInstance()
                                                .getReference("users/" + currentUser.getUid() + "/rewards");

            // Configure Shakeomat
            sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
            if (sensorManager != null) {
                accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            }
        }

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (accelerometer != null && mAuth.getCurrentUser() != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (accelerometer != null) {
            sensorManager.unregisterListener(this);
        }
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
                openShakeomat();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //Do nothing
    }

    private void saveRewardToFirebase(String reward) {
        databaseReference.child(UUID.randomUUID().toString()).setValue(reward)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(requireContext(), "Nagroda zapisana!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Błąd zapisu nagrody.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openShakeomat() {
        String reward = rewards[new Random().nextInt(rewards.length)];
        textView.setText("Twoja nagroda: " + reward);

        saveRewardToFirebase(reward);
    }
}