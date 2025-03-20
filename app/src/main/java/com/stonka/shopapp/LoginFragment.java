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

public class LoginFragment extends Fragment {
    private EditText emailLogin, passwordLogin;
    private Button loginButton, goToRegister, forgotPasswordButton;
    private FirebaseAuth mAuth;
    View root;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        FragmentLoginBinding binding = FragmentLoginBinding.inflate(inflater, container, false);
        root = binding.getRoot();

        mAuth = FirebaseAuth.getInstance();
        emailLogin = root.findViewById(R.id.emailLogin);
        passwordLogin = root.findViewById(R.id.passwordLogin);
        loginButton = root.findViewById(R.id.loginButton);
        goToRegister = root.findViewById(R.id.goToRegister);

        loginButton.setOnClickListener(v -> loginUser());
        goToRegister.setOnClickListener(v -> {
            NavController navController =
                    Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
            navController.navigate(R.id.registerFragment);
        });

        forgotPasswordButton = root.findViewById(R.id.forgotPasswordButton);
        forgotPasswordButton.setOnClickListener(v -> {
            NavController navController =
                    Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
            navController.navigate(R.id.resetPasswordFragment);
        });

        return root;
    }

    private void loginUser() {
        String email = emailLogin.getText().toString().trim();
        String password = passwordLogin.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(root.getContext(), "Podaj email i hasło!", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null && user.isEmailVerified()) {
                            Toast.makeText(root.getContext(), "Logowanie udane!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(root.getContext(), MainActivity.class));
                        } else {
                            Toast.makeText(root.getContext(),
                                    "Musisz potwierdzić swój e-mail. Sprawdź swoją skrzynkę pocztową.",
                                    Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(root.getContext(), "Błędny login lub hasło!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
