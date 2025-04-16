package com.stonka.shopapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.stonka.shopapp.databinding.ActivityMainBinding;

/**
 * Główna aktywność aplikacji, która zarządza nawigacją pomiędzy różnymi fragmentami
 * oraz sprawdza uprawnienia do powiadomień i planuje codzienne powiadomienia.
 */
public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    /**
     * Wywoływana przy tworzeniu aktywności.
     * Ustawia interfejs użytkownika, nawigację oraz sprawdza uprawnienia do powiadomień.
     *
     * @param savedInstanceState Stan zapisany poprzednio (jeśli istnieje).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Powiązanie widoku z plikiem layout
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Ustawienie dolnej nawigacji
        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_shakeomat, R.id.navigation_account)
                .build();

        // Powiązanie nawigacji z akcjami
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        // Sprawdzenie uprawnień do powiadomień
        checkNotificationPermissionAndRedirect(this);

        // Zaplanowanie codziennego powiadomienia
        scheduleDailyNotification(this);
    }

    /**
     * Obsługuje przycisk "wstecz" w nawigacji, umożliwiając powrót do poprzedniego ekranu.
     *
     * @return Zwraca wynik nawigacji lub domyślną akcję "wstecz".
     */
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        return navController.navigateUp() || super.onSupportNavigateUp();
    }

    /**
     * Sprawdza, czy aplikacja ma uprawnienia do wyświetlania powiadomień.
     * Jeśli nie, przekierowuje użytkownika do ustawień aplikacji.
     *
     * @param context Kontekst aplikacji.
     */
    private void checkNotificationPermissionAndRedirect(Context context) {
        // Sprawdzenie, czy powiadomienia są włączone
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());

            // Uruchomienie ustawień powiadomień dla aplikacji
            context.startActivity(intent);
        }
    }

    /**
     * Planowanie codziennego powiadomienia, które jest uruchamiane za pomocą AlarmManager.
     * Powiadomienie jest zaplanowane do powtarzania w regularnych odstępach czasu.
     *
     * @param context Kontekst aplikacji.
     */
    private void scheduleDailyNotification(Context context) {
        // Tworzenie intencji dla AlarmReceiver
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Ustawienie AlarmManager
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Określenie czasu pierwszego uruchomienia oraz interwału powtórzeń
        long firstTriggerTime = System.currentTimeMillis();
        long repeatInterval = 60 * 1000; // 1 minuta

        // Ustawienie powtarzającego się alarmu
        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                firstTriggerTime,
                repeatInterval,
                pendingIntent
        );
    }
}
