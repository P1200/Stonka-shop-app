package com.stonka.shopapp;

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
import com.stonka.shopapp.databinding.FragmentResetPasswordBinding;

/**
 * Fragment odpowiedzialny za resetowanie hasła użytkownika.
 * Umożliwia użytkownikowi wprowadzenie swojego e-maila i wysyłanie linku do resetowania hasła.
 */
public class ResetPasswordFragment extends Fragment {
    private EditText emailReset;
    private Button resetPasswordButton;
    private FirebaseAuth mAuth;
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

        // Inicjalizacja widoku fragmentu
        FragmentResetPasswordBinding binding =
                FragmentResetPasswordBinding.inflate(inflater, container, false);
        root = binding.getRoot();

        // Inicjalizacja instancji FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Inicjalizacja widoków
        emailReset = root.findViewById(R.id.emailReset);
        resetPasswordButton = root.findViewById(R.id.resetPasswordButton);

        // Ustawienie akcji po kliknięciu przycisku resetowania hasła
        resetPasswordButton.setOnClickListener(v -> resetPassword());

        return root;
    }

    /**
     * Resetuje hasło użytkownika, wysyłając link do resetowania hasła na podany adres e-mail.
     */
    private void resetPassword() {
        String email = emailReset.getText().toString().trim();

        // Sprawdzenie, czy użytkownik podał e-mail
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(root.getContext(), "Podaj swój e-mail!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Wysłanie e-maila do resetowania hasła
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Jeśli link do resetowania hasła został wysłany pomyślnie
                        Toast.makeText(root.getContext(),
                                "Link do resetowania hasła został wysłany na podany adres e-mail.",
                                Toast.LENGTH_LONG).show();

                        // Nawigacja do ekranu logowania po wysłaniu linku
                        NavController navController =
                                Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
                        navController.navigate(R.id.loginFragment);
                    } else {
                        // Jeśli wystąpił błąd (np. e-mail nie został znaleziony)
                        Toast.makeText(root.getContext(),
                                "Błąd: nie znaleziono konta z podanym adresem email.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
