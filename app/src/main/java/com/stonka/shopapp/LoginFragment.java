package com.stonka.shopapp;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import net.glxn.qrgen.android.QRCode;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class LoginFragment extends Fragment {

    private Button generateQrButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        generateQrButton = view.findViewById(R.id.generateQrButton);
        generateQrButton.setOnClickListener(v -> generateQrCode());

        return view;
    }

    private void generateQrCode() {
        String email = getEmailFromConfig();

        if (email == null || email.isEmpty()) {
            showToast("Nie znaleziono adresu email w konfiguracji");
            return;
        }

        try {
            Bitmap qrBitmap = QRCode.from(email)
                    .withSize(500, 500)
                    .bitmap();

            showQrDialog(qrBitmap, email);
        } catch (Exception e) {
            showToast("Błąd generowania QR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String getEmailFromConfig() {
        try {
            Resources resources = getResources();
            InputStream inputStream = resources.openRawResource(R.raw.config);

            Properties properties = new Properties();
            properties.load(inputStream);
            inputStream.close();

            return properties.getProperty("EMAIL_ADDRESS");
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
            showToast("Nie znaleziono pliku konfiguracyjnego");
        } catch (IOException e) {
            e.printStackTrace();
            showToast("Błąd odczytu pliku konfiguracyjnego");
        }
        return null;
    }

    private void showQrDialog(Bitmap qrBitmap, String email) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Twój kod QR");
        builder.setMessage("Wygenerowano dla: " + email);

        ImageView imageView = new ImageView(requireContext());
        imageView.setImageBitmap(qrBitmap);
        builder.setView(imageView);

        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
}