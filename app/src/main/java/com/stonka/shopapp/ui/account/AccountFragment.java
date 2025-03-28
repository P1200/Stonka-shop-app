package com.stonka.shopapp.ui.account;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.stonka.shopapp.R;
import com.stonka.shopapp.databinding.FragmentAccountBinding;

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

    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private FragmentAccountBinding binding;
    private Button logoutButton;
    private FirebaseAuth mAuth;
    private Button loginButton;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentAccountBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final EditText messageInput = binding.textInput;
        final Button sendMessageButton = binding.sendButton;

        if (firebaseAuth.getCurrentUser() == null) {
            messageInput.setVisibility(View.GONE);
            sendMessageButton.setVisibility(View.GONE);
        }

        sendMessageButton.setOnClickListener(event -> sendMailMessage(messageInput, root));

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

    private void sendMailMessage(EditText messageInput, View root) {
        Properties props = getMailProperties();

        Executors.newSingleThreadExecutor().execute(() -> {
            try (InputStream input = requireContext().getResources().openRawResource(R.raw.config)) {
                Message message = getMessage(messageInput, props, input);
                Transport.send(message);

                requireActivity().runOnUiThread(
                        () -> Toast.makeText(root.getContext(), "Wiadomość została wysłana", Toast.LENGTH_SHORT)
                                    .show()
                );

                messageInput.getText()
                            .clear();

            } catch (IOException | MessagingException e) {
                Log.e("HELP_MAIL", e.toString());
                requireActivity().runOnUiThread(
                        () -> Toast.makeText(root.getContext(), "Nie udało się wysłać wiadomości. Przepraszamy", Toast.LENGTH_SHORT)
                                    .show()
                );
            }
        });
    }

    @NonNull
    private Message getMessage(EditText messageInput, Properties props, InputStream input) throws IOException, MessagingException {
        props.load(input);
        String password = props.getProperty("EMAIL_PASSWORD");
        String emailAddress = props.getProperty("EMAIL_ADDRESS");
        String recipientAddress = props.getProperty("EMAIL_RECEPIENT");

        Session session = getMailSession(props, emailAddress, password);

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(emailAddress));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientAddress));
        message.setSubject(firebaseAuth.getCurrentUser().getUid());
        message.setText(messageInput.getText().toString());
        return message;
    }

    @NonNull
    private static Properties getMailProperties() {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        return props;
    }

    @NonNull
    private static Session getMailSession(Properties props, String emailAddress, String password) {
        return Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(emailAddress, password);
                    }
                });
    }
}