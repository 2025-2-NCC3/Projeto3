package com.example.pi.models;

public class Product {
    private String id;
    private String name;
    private String description;
    private double price;
    private int stock;
    private String category;
    private String imageUrl;
    private static int numInstancias;

    public Product() {}

    public Product(String id, String name, String description, double price, int stock, String category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.category = category;

        numInstancias++;
    }

    // Getters e Setters
    public static int getNumInstancias() { return numInstancias; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    // Método para verificar se há estoque suficiente
    public boolean hasSufficientStock(int requestedQuantity) {
        return stock >= requestedQuantity;
    }

    // Método para diminuir o estoque
    public void decreaseStock(int quantity) {
        if (quantity <= stock) {
            stock -= quantity;
        }
    }

    // Método para aumentar o estoque
    public void increaseStock(int quantity) {
        stock += quantity;
    }
}