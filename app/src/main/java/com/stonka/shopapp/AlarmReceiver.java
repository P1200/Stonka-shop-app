package com.stonka.shopapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

/**
 * Odbiornik powiadomień, który jest uruchamiany przez BroadcastReceiver.
 * Tworzy powiadomienie informujące o promocji dnia.
 */
public class AlarmReceiver extends BroadcastReceiver {

    /**
     * Metoda wywoływana, gdy odbiornik otrzyma powiadomienie.
     * Tworzy powiadomienie o promocji dnia i wyświetla je w systemie.
     *
     * @param context Kontekst aplikacji, który jest używany do tworzenia powiadomień.
     * @param intent Intent, który wywołuje tę metodę (zawiera dodatkowe dane, jeśli są).
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        // ID kanału powiadomień
        String channelId = "promo_channel";
        // Tytuł powiadomienia
        String title = "🔥 Promocja dnia!";
        // Treść powiadomienia
        String message = "Sprawdź nowe okazje w naszym sklepie.";
        // Ważność powiadomienia (domyślna)
        int importance = NotificationManager.IMPORTANCE_DEFAULT;

        // Tworzenie kanału powiadomień
        NotificationChannel channel = new NotificationChannel(channelId, title, importance);
        channel.setDescription(message);

        // Uzyskanie instancji NotificationManager
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Tworzenie kanału powiadomień w systemie (wymagane od Android 8.0)
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }

        // Tworzenie powiadomienia
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground) // Ikona powiadomienia
                .setContentTitle(title) // Tytuł powiadomienia
                .setContentText(message) // Treść powiadomienia
                .setAutoCancel(true); // Powiadomienie zniknie po kliknięciu

        // Wyświetlenie powiadomienia
        if (manager != null) {
            manager.notify(1001, builder.build()); // Unikalny identyfikator powiadomienia
        }
    }
}
