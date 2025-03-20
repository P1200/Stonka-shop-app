package com.stonka.shopapp.ui.account;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.stonka.shopapp.R;
import com.stonka.shopapp.databinding.FragmentAccountBinding;

public class AccountFragment extends Fragment {

    private FragmentAccountBinding binding;
    private Button logoutButton;
    private FirebaseAuth mAuth;
    private Button loginButton;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        AccountViewModel accountViewModel =
                new ViewModelProvider(this).get(AccountViewModel.class);

        binding = FragmentAccountBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textAccount;
        accountViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        loginButton = root.findViewById(R.id.loginButton);
        loginButton.setOnClickListener(event -> {
            NavController navController =
                    Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
            navController.navigate(R.id.loginFragment);
        });

        mAuth = FirebaseAuth.getInstance();
        logoutButton = root.findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(v -> {
            NavController navController =
                    Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
            navController.navigate(R.id.loginFragment);
            mAuth.signOut();
        });

        return root;
    }



    @Override
    public void onResume() {
        super.onResume();

        if (mAuth.getCurrentUser() != null) {
            loginButton.setVisibility(View.GONE);
            logoutButton.setVisibility(View.VISIBLE);
        } else {
            logoutButton.setVisibility(View.GONE);
            loginButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}