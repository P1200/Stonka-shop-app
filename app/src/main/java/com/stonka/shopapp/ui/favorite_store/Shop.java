package com.stonka.shopapp.ui.favorite_store;

/**
 * Klasa modelu reprezentująca sklep z nazwą i współrzędnymi geograficznymi.
 * Używana m.in. do pobierania danych z Firebase i wyświetlania na mapie.
 */
public class Shop {
    // Nazwa sklepu
    private String name;

    // Współrzędne geograficzne sklepu
    private double latitude;
    private double longitude;

    // Konstruktor bezargumentowy wymagany przez Firebase do deserializacji
    public Shop() {
    }

    // Konstruktor z parametrami – ułatwia tworzenie obiektów Shop
    public Shop(String name, double latitude, double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getter nazwy sklepu
    public String getName() {
        return name;
    }

    // Setter nazwy sklepu
    public void setName(String name) {
        this.name = name;
    }

    // Getter szerokości geograficznej
    public double getLatitude() {
        return latitude;
    }

    // Setter szerokości geograficznej
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    // Getter długości geograficznej
    public double getLongitude() {
        return longitude;
    }

    // Setter długości geograficznej
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
