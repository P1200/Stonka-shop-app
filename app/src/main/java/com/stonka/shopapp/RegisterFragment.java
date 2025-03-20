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

public class RegisterFragment extends Fragment {
    private EditText nameRegister, birthDateRegister, emailRegister, passwordRegister;
    private Button registerButton;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    View root;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        FragmentRegisterBinding binding = FragmentRegisterBinding.inflate(inflater, container, false);
        root = binding.getRoot();

        mAuth = FirebaseAuth.getInstance();
        databaseReference =
                FirebaseDatabase.getInstance("https://stonka-shop-app-default-rtdb.europe-west1.firebasedatabase.app/")
                        .getReference("users");

        nameRegister = root.findViewById(R.id.nameRegister);
        birthDateRegister = root.findViewById(R.id.birthDateRegister);
        emailRegister = root.findViewById(R.id.emailRegister);
        passwordRegister = root.findViewById(R.id.passwordRegister);
        registerButton = root.findViewById(R.id.registerButton);

        // Opening calendar picker after click in text field
        birthDateRegister.setOnClickListener(v -> showDatePicker());

        registerButton.setOnClickListener(v -> registerUser());

        return root;
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(root.getContext(),
                (view, yearSelected, monthSelected, daySelected) -> {
                    String selectedDate = String.format(Locale.getDefault(), "%02d-%02d-%d",
                            daySelected, monthSelected + 1, yearSelected);
                    birthDateRegister.setText(selectedDate);
                }, year, month, day);

        // Max date to current date
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());

        datePickerDialog.show();
    }

    private void registerUser() {
        String name = nameRegister.getText().toString().trim();
        String birthDate = birthDateRegister.getText().toString().trim();
        String email = emailRegister.getText().toString().trim();
        String password = passwordRegister.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(birthDate) ||
                TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(root.getContext(), "Wprowadź wszystkie dane!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidEmail(email)) {
            Toast.makeText(root.getContext(), "Niepoprawny format e-maila!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidBirthDate(birthDate)) {
            Toast.makeText(root.getContext(), "Niepoprawna data urodzenia!", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
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

    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isValidBirthDate(String birthDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        sdf.setLenient(false);
        try {
            Calendar birthCal = Calendar.getInstance();
            birthCal.setTime(Objects.requireNonNull(sdf.parse(birthDate)));

            Calendar today = Calendar.getInstance();
            int age = today.get(Calendar.YEAR) - birthCal.get(Calendar.YEAR);

            if (today.get(Calendar.DAY_OF_YEAR) < birthCal.get(Calendar.DAY_OF_YEAR)) {
                age--;
            }

            if (age < 13) {
                Toast.makeText(root.getContext(), "Musisz mieć co najmniej 13 lat!", Toast.LENGTH_SHORT).show();
                return false;
            }

            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    private void saveUserToDatabase(String userId, String name, String birthDate, String email) {
        HashMap<String, String> userMap = new HashMap<>();
        userMap.put("name", name);
        userMap.put("birthDate", birthDate);
        userMap.put("email", email);

        databaseReference.child(userId).setValue(userMap)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(root.getContext(),
                                "Rejestracja udana! Sprawdź e-mail, aby potwierdzić konto.",
                                Toast.LENGTH_LONG).show();
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
