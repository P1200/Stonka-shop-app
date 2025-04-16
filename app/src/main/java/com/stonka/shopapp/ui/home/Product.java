package com.stonka.shopapp.ui.home;

/**
 * Klasa reprezentująca produkt w sklepie.
 * Zawiera informacje o nazwie produktu, cenie oraz ścieżce do obrazu produktu.
 */
public class Product {

    private String name; // Nazwa produktu
    private double price; // Cena produktu
    private String imageResource; // Ścieżka do zasobu obrazu produktu (np. URL lub lokalny zasób)

    /**
     * Konstruktor domyślny wymagany przez Firebase dla deserializacji obiektów.
     */
    public Product() {

    }

    /**
     * Konstruktor inicjalizujący wszystkie pola obiektu.
     *
     * @param name Nazwa produktu.
     * @param price Cena produktu.
     * @param imageResource Ścieżka do obrazu produktu.
     */
    public Product(String name, double price, String imageResource) {
        this.name = name;
        this.price = price;
        this.imageResource = imageResource;
    }

    /**
     * Pobiera nazwę produktu.
     *
     * @return Nazwa produktu.
     */
    public String getName() {
        return name;
    }

    /**
     * Pobiera cenę produktu.
     *
     * @return Cena produktu.
     */
    public double getPrice() {
        return price;
    }

    /**
     * Pobiera ścieżkę do obrazu produktu.
     *
     * @return Ścieżka do zasobu obrazu.
     */
    public String getImageResource() {
        return imageResource;
    }

    /**
     * Ustawia nazwę produktu.
     *
     * @param name Nowa nazwa produktu.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Ustawia cenę produktu.
     *
     * @param price Nowa cena produktu.
     */
    public void setPrice(double price) {
        this.price = price;
    }

    /**
     * Ustawia ścieżkę do obrazu produktu.
     *
     * @param imageResource Nowa ścieżka do obrazu produktu.
     */
    public void setImageResource(String imageResource) {
        this.imageResource = imageResource;
    }
}
