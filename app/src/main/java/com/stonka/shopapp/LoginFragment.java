package com.stonka.shopapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.stonka.shopapp.databinding.FragmentLoginBinding;

/**
 * Fragment logowania, który umożliwia użytkownikowi zalogowanie się do aplikacji.
 * Sprawdza poprawność danych logowania, a także obsługuje przejście do rejestracji oraz resetu hasła.
 */
public class LoginFragment extends Fragment {

    // Zmienne dla widoków formularza logowania
    private EditText emailLogin, passwordLogin;
    private Button loginButton, goToRegister, forgotPasswordButton;
    private FirebaseAuth mAuth;
    View root;

    /**
     * Metoda wywoływana przy tworzeniu widoku fragmentu.
     * Inicjalizuje komponenty widoku, takie jak pola tekstowe, przyciski i logikę nawigacji.
     *
     * @param inflater          Inflater do tworzenia widoku.
     * @param container         Kontener, w którym widok ma zostać umieszczony.
     * @param savedInstanceState Stan zachowany po poprzednich zmianach widoku.
     * @return Widok fragmentu.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Powiązanie widoku z plikiem layout
        FragmentLoginBinding binding = FragmentLoginBinding.inflate(inflater, container, false);
        root = binding.getRoot();

        // Inicjalizacja FirebaseAuth do obsługi logowania
        mAuth = FirebaseAuth.getInstance();

        // Powiązanie pól tekstowych i przycisków z widoku
        emailLogin = root.findViewById(R.id.emailLogin);
        passwordLogin = root.findViewById(R.id.passwordLogin);
        loginButton = root.findViewById(R.id.loginButton);
        goToRegister = root.findViewById(R.id.goToRegister);

        // Obsługa kliknięcia przycisku logowania
        loginButton.setOnClickListener(v -> loginUser());

        // Przycisk do przejścia do ekranu rejestracji
        goToRegister.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
            navController.navigate(R.id.registerFragment);
        });

        // Przycisk do przejścia do resetu hasła
        forgotPasswordButton = root.findViewById(R.id.forgotPasswordButton);
        forgotPasswordButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
            navController.navigate(R.id.resetPasswordFragment);
        });

        return root;
    }

    /**
     * Metoda logująca użytkownika przy użyciu wprowadzonych danych (email i hasło).
     * Weryfikuje poprawność danych oraz sprawdza, czy użytkownik potwierdził swój adres email.
     */
    private void loginUser() {
        String email = emailLogin.getText().toString().trim();
        String password = passwordLogin.getText().toString().trim();

        // Sprawdzenie, czy pola email i hasło są wypełnione
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(root.getContext(), "Podaj email i hasło!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Logowanie użytkownika przez Firebase
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();

                        // Sprawdzenie, czy użytkownik jest zweryfikowany
                        if (user != null && user.isEmailVerified()) {
                            Toast.makeText(root.getContext(), "Logowanie udane!", Toast.LENGTH_SHORT).show();
                            // Przejście do głównej aktywności po pomyślnym logowaniu
                            startActivity(new Intent(root.getContext(), MainActivity.class));
                        } else {
                            // Komunikat, jeśli email użytkownika nie został zweryfikowany
                            Toast.makeText(root.getContext(),
                                    "Musisz potwierdzić swój e-mail. Sprawdź swoją skrzynkę pocztową.",
                                    Toast.LENGTH_LONG).show();
                        }
                    } else {
                        // Komunikat, jeśli logowanie nie powiodło się
                        Toast.makeText(root.getContext(), "Błędny login lub hasło!", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
