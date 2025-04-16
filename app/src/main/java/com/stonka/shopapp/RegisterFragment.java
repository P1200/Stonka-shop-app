package com.stonka.shopapp;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.stonka.shopapp.databinding.FragmentRegisterBinding;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

/**
 * Fragment odpowiedzialny za rejestrację nowego użytkownika.
 * Umożliwia wprowadzenie danych, takich jak imię, data urodzenia, email i hasło.
 * Sprawdza poprawność danych i zapisuje je w bazie danych Firebase.
 */
public class RegisterFragment extends Fragment {
    private EditText nameRegister, birthDateRegister, emailRegister, passwordRegister;
    private Button registerButton;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    View root;

    /**
     * Tworzy widok fragmentu i inicjalizuje elementy UI.
     *
     * @param inflater Inflater używany do zainicjowania widoku.
     * @param container Kontener, do którego widok zostanie dodany.
     * @param savedInstanceState Stan zapisany z poprzednich cykli życia fragmentu.
     * @return Zainicjowany widok fragmentu.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentRegisterBinding binding = FragmentRegisterBinding.inflate(inflater, container, false);
        root = binding.getRoot();

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        // Inicjalizacja widoków
        nameRegister = root.findViewById(R.id.nameRegister);
        birthDateRegister = root.findViewById(R.id.birthDateRegister);
        emailRegister = root.findViewById(R.id.emailRegister);
        passwordRegister = root.findViewById(R.id.passwordRegister);
        registerButton = root.findViewById(R.id.registerButton);

        // Otwieranie kalendarza po kliknięciu w pole daty urodzenia
        birthDateRegister.setOnClickListener(v -> showDatePicker());

        // Obsługa kliknięcia przycisku rejestracji
        registerButton.setOnClickListener(v -> registerUser());

        return root;
    }

    /**
     * Wyświetla okno wyboru daty urodzenia.
     */
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(root.getContext(),
                (view, yearSelected, monthSelected, daySelected) -> {
                    // Formatowanie daty i ustawianie jej w polu tekstowym
                    String selectedDate = String.format(Locale.getDefault(), "%02d-%02d-%d",
                            daySelected, monthSelected + 1, yearSelected);
                    birthDateRegister.setText(selectedDate);
                }, year, month, day);

        // Ustawienie maksymalnej daty na dzisiaj
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());

        datePickerDialog.show();
    }

    /**
     * Rejestruje nowego użytkownika w Firebase i wysyła email weryfikacyjny.
     */
    private void registerUser() {
        String name = nameRegister.getText().toString().trim();
        String birthDate = birthDateRegister.getText().toString().trim();
        String email = emailRegister.getText().toString().trim();
        String password = passwordRegister.getText().toString().trim();

        // Walidacja danych wejściowych
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(birthDate) ||
                TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(root.getContext(), "Wprowadź wszystkie dane!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Sprawdzenie poprawności adresu e-mail
        if (!isValidEmail(email)) {
            Toast.makeText(root.getContext(), "Niepoprawny format e-maila!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Sprawdzenie poprawności daty urodzenia
        if (!isValidBirthDate(birthDate)) {
            Toast.makeText(root.getContext(), "Niepoprawna data urodzenia!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tworzenie użytkownika w Firebase
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Wysyłanie e-maila weryfikacyjnego
                            user.sendEmailVerification()
                                    .addOnCompleteListener(requireActivity(), emailTask -> {
                                        if (emailTask.isSuccessful()) {
                                            saveUserToDatabase(user.getUid(), name, birthDate, email);
                                        } else {
                                            Toast.makeText(root.getContext(),
                                                    "Błąd wysyłania e-maila weryfikacyjnego.",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(root.getContext(), "Rejestracja nie powiodła się!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Sprawdza, czy e-mail ma poprawny format.
     *
     * @param email Adres e-mail.
     * @return Zwraca true, jeśli e-mail jest poprawny, w przeciwnym razie false.
     */
    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Sprawdza, czy data urodzenia jest poprawna i użytkownik ma co najmniej 13 lat.
     *
     * @param birthDate Data urodzenia w formacie "dd-MM-yyyy".
     * @return Zwraca true, jeśli data urodzenia jest poprawna, w przeciwnym razie false.
     */
    private boolean isValidBirthDate(String birthDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        sdf.setLenient(false);
        try {
            Calendar birthCal = Calendar.getInstance();
            birthCal.setTime(Objects.requireNonNull(sdf.parse(birthDate)));

            Calendar today = Calendar.getInstance();
            int age = today.get(Calendar.YEAR) - birthCal.get(Calendar.YEAR);

            // Uwzględnienie dnia urodzin w bieżącym roku
            if (today.get(Calendar.DAY_OF_YEAR) < birthCal.get(Calendar.DAY_OF_YEAR)) {
                age--;
            }

            // Sprawdzanie, czy użytkownik ma co najmniej 13 lat
            if (age < 13) {
                Toast.makeText(root.getContext(), "Musisz mieć co najmniej 13 lat!", Toast.LENGTH_SHORT).show();
                return false;
            }

            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    /**
     * Zapisuje dane użytkownika do bazy danych Firebase.
     *
     * @param userId ID użytkownika w Firebase.
     * @param name Imię użytkownika.
     * @param birthDate Data urodzenia użytkownika.
     * @param email E-mail użytkownika.
     */
    private void saveUserToDatabase(String userId, String name, String birthDate, String email) {
        HashMap<String, String> userMap = new HashMap<>();
        userMap.put("name", name);
        userMap.put("birthDate", birthDate);
        userMap.put("email", email);

        // Zapisanie danych użytkownika w bazie Firebase
        databaseReference.child(userId).setValue(userMap)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(root.getContext(),
                                "Rejestracja udana! Sprawdź e-mail, aby potwierdzić konto.",
                                Toast.LENGTH_LONG).show();
                        // Nawigacja do ekranu logowania
                        NavController navController =
                                Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
                        navController.navigate(R.id.loginFragment);
                    } else {
                        Toast.makeText(root.getContext(),
                                "Błąd zapisu danych do bazy!",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
