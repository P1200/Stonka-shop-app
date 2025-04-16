package com.stonka.shopapp;

/**
 * Klasa reprezentująca przedmiot na liście zakupów.
 * Zawiera dane przedmiotu, takie jak jego nazwa i unikalne ID.
 */
public class ShoppingListItem {

    private String id;   // Unikalne ID przedmiotu
    private String name; // Nazwa przedmiotu

    /**
     * Konstruktor klasy ShoppingListItem.
     * Używany do tworzenia nowego przedmiotu z nazwą i ID.
     *
     * @param name Nazwa przedmiotu.
     * @param id   Unikalne ID przedmiotu.
     */
    public ShoppingListItem(String name, String id) {
        this.name = name;
        this.id = id;
    }

    /**
     * Konstruktor bezargumentowy.
     * Używany do tworzenia pustego obiektu, np. podczas deserializacji z Firebase.
     */
    public ShoppingListItem() {
    }

    /**
     * Zwraca nazwę przedmiotu.
     *
     * @return Nazwa przedmiotu.
     */
    public String getName() {
        return name;
    }

    /**
     * Ustawia nazwę przedmiotu.
     *
     * @param name Nazwa przedmiotu.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Zwraca unikalne ID przedmiotu.
     *
     * @return Unikalne ID przedmiotu.
     */
    public String getId() {
        return id;
    }

    /**
     * Ustawia unikalne ID przedmiotu.
     *
     * @param id Unikalne ID przedmiotu.
     */
    public void setId(String id) {
        this.id = id;
    }
}
