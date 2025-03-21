package com.stonka.shopapp.ui.home;

public class Product {
    private String name;
    private double price;
    private String imageResource;

    public Product() {

    }

    public Product(String name, double price, String imageResource) {
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

    public String getImageResource() {
        return imageResource;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setImageResource(String imageResource) {
        this.imageResource = imageResource;
    }
}