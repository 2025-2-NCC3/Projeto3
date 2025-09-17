package com.example.myapplication;

//Aqui é o modelo dos produtos da cantina e estoque
public class Product {
    private String id;
    private String name;
    private double price;
    private int stock;

    public Product(String id, String name, double price, int stock) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    //Verifica se tem estoque
    public boolean hasStock(int quantity) {
        return stock >= quantity;
    }

    //Reduz do estoque
    public void reduceStock(int quantity) {
        if (hasStock(quantity)) {
            stock -= quantity;
        }
    }
}