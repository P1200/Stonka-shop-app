package com.stonka.shopapp.ui.shakeomat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.stonka.shopapp.databinding.FragmentShakeomatBinding;

public class ShakeomatFragment extends Fragment {

    private FragmentShakeomatBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ShakeomatViewModel shakeomatViewModel =
                new ViewModelProvider(this).get(ShakeomatViewModel.class);

        binding = FragmentShakeomatBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textDashboard;
        shakeomatViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}