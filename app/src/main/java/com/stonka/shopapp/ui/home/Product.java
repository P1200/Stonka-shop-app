package com.stonka.shopapp.ui.home;

public class Product {
    private String name;
    private double price;
    private int imageResource;

    public Product(String name, double price, int imageResource) {
        this.name = name;
        this.price = price;
        this.imageResource = imageResource;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getImageResource() {
        return imageResource;
    }
}