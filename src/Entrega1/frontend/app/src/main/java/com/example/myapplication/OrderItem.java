package com.example.myapplication;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class OrderItem implements Serializable {

    @SerializedName("product_id")
    private String productId;

    @SerializedName("product_name")
    private String productName;

    @SerializedName("quantity")
    private int quantity;

    @SerializedName("price")
    private double price;

    // Construtor vazio para Gson
    public OrderItem() {
    }

    public OrderItem(String productId, String productName, int quantity, double price) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getSubtotal() {
        return quantity * price;
    }

    @Override
    public String toString() {
        return "OrderItem{" +
                "productId='" + productId + '\'' +
                ", productName='" + productName + '\'' +
                ", quantity=" + quantity +
                ", price=" + price +
                ", subtotal=" + getSubtotal() +
                '}';
    }
}