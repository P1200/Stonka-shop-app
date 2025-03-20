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

public class ResetPasswordFragment extends Fragment {
    private EditText emailReset;
    private Button resetPasswordButton;
    private FirebaseAuth mAuth;
    View root;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        FragmentResetPasswordBinding binding =
                FragmentResetPasswordBinding.inflate(inflater, container, false);
        root = binding.getRoot();

        mAuth = FirebaseAuth.getInstance();
        emailReset = root.findViewById(R.id.emailReset);
        resetPasswordButton = root.findViewById(R.id.resetPasswordButton);

        resetPasswordButton.setOnClickListener(v -> resetPassword());

        return root;
    }

    private void resetPassword() {
        String email = emailReset.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(root.getContext(), "Podaj swój e-mail!", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(root.getContext(),
                                "Link do resetowania hasła został wysłany na podany adres e-mail.",
                                Toast.LENGTH_LONG).show();
                        NavController navController =
                                Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
                        navController.navigate(R.id.loginFragment);
                    } else {
                        Toast.makeText(root.getContext(),
                                "Błąd: nie znaleziono konta z podanym adresem email.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
