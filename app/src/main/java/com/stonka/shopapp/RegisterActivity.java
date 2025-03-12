package com.stonka.shopapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {
    private EditText nameRegister, birthDateRegister, emailRegister, passwordRegister;
    private Button registerButton;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        databaseReference =
                FirebaseDatabase.getInstance("https://stonka-shop-app-default-rtdb.europe-west1.firebasedatabase.app/")
                                .getReference("users");

        nameRegister = findViewById(R.id.nameRegister);
        birthDateRegister = findViewById(R.id.birthDateRegister);
        emailRegister = findViewById(R.id.emailRegister);
        passwordRegister = findViewById(R.id.passwordRegister);
        registerButton = findViewById(R.id.registerButton);

        // Opening calendar picker after click in text field
        birthDateRegister.setOnClickListener(v -> showDatePicker());

        registerButton.setOnClickListener(v -> registerUser());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
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
            Toast.makeText(this, "Wprowadź wszystkie dane!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidEmail(email)) {
            Toast.makeText(this, "Niepoprawny format e-maila!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidBirthDate(birthDate)) {
            Toast.makeText(this, "Niepoprawna data urodzenia!", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            user.sendEmailVerification()
                                    .addOnCompleteListener(this, emailTask -> {
                                        if (emailTask.isSuccessful()) {
                                            saveUserToDatabase(user.getUid(), name, birthDate, email);
                                        } else {
                                            Toast.makeText(RegisterActivity.this,
                                                    "Błąd wysyłania e-maila weryfikacyjnego.",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(RegisterActivity.this, "Rejestracja nie powiodła się!", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(this, "Musisz mieć co najmniej 13 lat!", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(RegisterActivity.this,
                                "Rejestracja udana! Sprawdź e-mail, aby potwierdzić konto.",
                                Toast.LENGTH_LONG).show();
                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this,
                                "Błąd zapisu danych do bazy!",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
