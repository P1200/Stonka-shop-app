package com.stonka.shopapp.ui.account;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.stonka.shopapp.R;
import com.stonka.shopapp.databinding.FragmentAccountBinding;

import net.glxn.qrgen.android.QRCode;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.Executors;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class AccountFragment extends Fragment {

    // Inicjalizacja Firebase Auth
    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private FragmentAccountBinding binding;
    private Button logoutButton;
    private FirebaseAuth mAuth;
    private Button loginButton;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        // Inflatuj widok przy pomocy View Binding
        binding = FragmentAccountBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Inicjalizacja pól z layoutu
        final EditText messageInput = binding.textInput;
        final Button sendMessageButton = binding.sendButton;
        final Button generateQrButton = binding.generateQrButton;

        // Ukryj pola, jeśli użytkownik nie jest zalogowany
        if (firebaseAuth.getCurrentUser() == null) {
            messageInput.setVisibility(View.GONE);
            sendMessageButton.setVisibility(View.GONE);
            generateQrButton.setVisibility(View.GONE);
        }

        // Ustawienie listenera do wysyłania wiadomości
        sendMessageButton.setOnClickListener(event -> sendMailMessage(messageInput, root));

        // Listener do generowania kodu QR
        generateQrButton.setOnClickListener(v -> generateQrCode());

        // Przycisk logowania – przechodzi do fragmentu logowania
        loginButton = root.findViewById(R.id.loginButton);
        loginButton.setOnClickListener(event -> {
            NavController navController =
                    Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
            navController.navigate(R.id.loginFragment);
        });

        // Przycisk wylogowania – wylogowuje użytkownika i przekierowuje do logowania
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

        // Sprawdzenie, czy użytkownik jest zalogowany – odpowiednie pokazanie przycisków
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
        binding = null; // Zapobiega wyciekom pamięci
    }

    // Metoda do wysyłania wiadomości email
    private void sendMailMessage(EditText messageInput, View root) {
        Properties props = getMailProperties(); // Konfiguracja serwera SMTP

        // Wykonanie wysyłki w osobnym wątku
        Executors.newSingleThreadExecutor().execute(() -> {
            try (InputStream input = requireContext().getResources().openRawResource(R.raw.config)) {
                // Utwórz wiadomość email
                Message message = getMessage(messageInput, props, input);
                Transport.send(message); // Wyślij wiadomość

                // Pokazanie toastu po sukcesie
                requireActivity().runOnUiThread(
                        () -> Toast.makeText(root.getContext(), "Wiadomość została wysłana", Toast.LENGTH_SHORT)
                                .show()
                );

                messageInput.getText().clear(); // Wyczyść pole tekstowe

            } catch (IOException | MessagingException e) {
                // Obsługa błędu
                Log.e("HELP_MAIL", e.toString());
                requireActivity().runOnUiThread(
                        () -> Toast.makeText(root.getContext(), "Nie udało się wysłać wiadomości. Przepraszamy", Toast.LENGTH_SHORT)
                                .show()
                );
            }
        });
    }

    // Tworzy obiekt wiadomości email
    @NonNull
    private Message getMessage(EditText messageInput, Properties props, InputStream input) throws IOException, MessagingException {
        props.load(input); // Wczytaj dane z pliku konfiguracyjnego
        String password = props.getProperty("EMAIL_PASSWORD");
        String emailAddress = props.getProperty("EMAIL_ADDRESS");
        String recipientAddress = props.getProperty("EMAIL_RECEPIENT");

        // Utwórz sesję pocztową
        Session session = getMailSession(props, emailAddress, password);

        // Utwórz obiekt wiadomości
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(emailAddress));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientAddress));
        message.setSubject(firebaseAuth.getCurrentUser().getUid()); // UID jako temat
        message.setText(messageInput.getText().toString()); // Treść wiadomości
        return message;
    }

    // Konfiguracja SMTP dla Gmaila
    @NonNull
    private static Properties getMailProperties() {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        return props;
    }

    // Utworzenie sesji mailowej z uwierzytelnieniem
    @NonNull
    private static Session getMailSession(Properties props, String emailAddress, String password) {
        return Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(emailAddress, password);
                    }
                });
    }

    // Generuje kod QR na podstawie UID użytkownika
    private void generateQrCode() {
        String userId = mAuth.getUid();

        if (userId == null || userId.isEmpty()) {
            Log.e("UserQrCode", "There is no user id");
            return;
        }

        try {
            // Wygenerowanie bitmapy z kodem QR
            Bitmap qrBitmap = QRCode.from(userId)
                    .withSize(500, 500)
                    .bitmap();

            showQrDialog(qrBitmap); // Pokaż QR w dialogu
        } catch (Exception e) {
            showToast("Błąd generowania QR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Pokazuje okno dialogowe z kodem QR
    private void showQrDialog(Bitmap qrBitmap) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Twój kod QR");
        builder.setMessage("Pokaż go przy kasie");

        ImageView imageView = new ImageView(requireContext());
        imageView.setImageBitmap(qrBitmap); // Ustawienie obrazka
        builder.setView(imageView);

        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show(); // Wyświetlenie dialogu
    }

    // Pokazuje krótki komunikat typu toast
    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
}
