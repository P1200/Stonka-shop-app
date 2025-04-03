package com.stonka.shopapp;

public class ShoppingListItem {

    private String id;
    private String name;

    public ShoppingListItem(String name, String id) {
        this.name = name;
        this.id = id;
    }

    public ShoppingListItem() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
