package com.example.myapplication;

//Aqui é o modelo de cada item do pedido
public class OrderItem {
    private int productId;
    private String productName;
    private int quantity;
    private double price;

    public OrderItem(int productId, String productName, int quantity, double price) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
    }

    public int getProductId() { return productId; }
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }

    //calcula o preço do item
    public double getSubtotal() { return quantity * price; }
}