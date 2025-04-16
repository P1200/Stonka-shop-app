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

/**
 * Fragment dla funkcji Shakeomat, gdzie użytkownik może potrząsnąć telefonem, aby otrzymać losową nagrodę.
 * Użytkownik musi być zalogowany, aby móc odbierać nagrody.
 */
public class ShakeomatFragment extends Fragment implements SensorEventListener {

    private static final float SHAKE_THRESHOLD = 12.0f; // Próg wstrząsu, powyżej którego akcja jest wyzwalana
    private final String[] rewards = {"10% rabatu!", "Darmowa dostawa!", "20% zniżki na kolejne zakupy!", "Bonusowy produkt!"}; // Lista nagród

    private SensorManager sensorManager; // Zarządza czujnikami
    private Sensor accelerometer; // Czujnik przyspieszenia
    private long lastShakeTime = 0; // Czas ostatniego potrząśnięcia
    private FragmentShakeomatBinding binding; // Binding do widoku
    private DatabaseReference databaseReference; // Referencja do bazy danych Firebase
    private FirebaseAuth mAuth; // Obiekt do zarządzania autentykacją
    private TextView textView; // Tekst informujący użytkownika o stanie Shakeomatu

    /**
     * Tworzy widok fragmentu.
     * Inicjalizuje komponenty interfejsu, takie jak binding, autentykację, i konfigurację Shakeomatu.
     *
     * @param inflater LayoutInflater do inflacji widoku
     * @param container Widok rodzica
     * @param savedInstanceState Stan zapisany fragmentu
     * @return Zainfekowany widok fragmentu
     */
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ShakeomatViewModel shakeomatViewModel =
                new ViewModelProvider(this).get(ShakeomatViewModel.class);

        binding = FragmentShakeomatBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        textView = binding.textShakeomat;
        shakeomatViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        mAuth = FirebaseAuth.getInstance(); // Pobranie instancji Firebase Auth

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            textView.setText("Aby otrzymać promocję musisz się zalogować!"); // Jeśli użytkownik nie jest zalogowany
        } else {
            textView.setText("Potrząśnij aby otrzymać nagrodę!"); // Informacja, że użytkownik może potrząsnąć telefonem

            // Inicjalizacja referencji do bazy danych
            databaseReference = FirebaseDatabase.getInstance()
                    .getReference("users/" + currentUser.getUid() + "/rewards");

            // Konfiguracja czujników
            sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
            if (sensorManager != null) {
                accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER); // Pobranie czujnika przyspieszenia
            }
        }

        return root;
    }

    /**
     * Anulowanie bindingu przy zniszczeniu widoku.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Zwalnianie bindingu
    }

    /**
     * Rejestruje nasłuchiwacza czujników przy wznowieniu fragmentu.
     */
    @Override
    public void onResume() {
        super.onResume();

        // Rejestracja nasłuchiwacza tylko jeśli użytkownik jest zalogowany
        if (accelerometer != null && mAuth.getCurrentUser() != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    /**
     * Wyrejestrowuje nasłuchiwacza czujników przy pauzie fragmentu.
     */
    @Override
    public void onPause() {
        super.onPause();
        if (accelerometer != null) {
            sensorManager.unregisterListener(this); // Wyrejestrowanie nasłuchiwacza
        }
    }

    /**
     * Obsługuje zmiany czujnika (wstrząsy telefonu).
     *
     * @param event Zdarzenie zmiany czujnika
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0]; // Odczyt osi X
            float y = event.values[1]; // Odczyt osi Y
            float z = event.values[2]; // Odczyt osi Z

            // Obliczanie przyspieszenia
            double acceleration = Math.sqrt(x * x + y * y + z * z);
            long currentTime = System.currentTimeMillis(); // Pobranie bieżącego czasu

            // Sprawdzanie, czy przyspieszenie przekroczyło próg i czy minęło wystarczająco czasu
            if (acceleration > SHAKE_THRESHOLD && (currentTime - lastShakeTime > 1000)) {
                lastShakeTime = currentTime;
                openShakeomat(); // Otwórz Shakeomat
            }
        }
    }

    /**
     * Funkcja, która nie wykonuje żadnych akcji przy zmianie dokładności czujnika.
     *
     * @param sensor Typ czujnika
     * @param accuracy Nowa dokładność czujnika
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Nic nie robimy w tej metodzie
    }

    /**
     * Zapisuje nagrodę do bazy danych Firebase.
     *
     * @param reward Nagroda, którą użytkownik otrzymał
     */
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

    /**
     * Losuje nagrodę z listy dostępnych nagród i wyświetla ją użytkownikowi.
     * Zapisuje również nagrodę w bazie danych Firebase.
     */
    private void openShakeomat() {
        String reward = rewards[new Random().nextInt(rewards.length)]; // Losowanie nagrody
        textView.setText("Twoja nagroda: " + reward); // Wyświetlanie nagrody

        saveRewardToFirebase(reward); // Zapisanie nagrody w Firebase
    }
}
