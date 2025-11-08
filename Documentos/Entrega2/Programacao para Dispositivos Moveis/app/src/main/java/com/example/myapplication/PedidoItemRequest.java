package com.example.myapplication;

public class PedidoItemRequest {
    private String productId;
    private String productName;  // ⭐ NOVO
    private int quantity;
    private double price;  // ⭐ NOVO

    // Construtor completo (NOVO)
    public PedidoItemRequest(String productId, String productName, int quantity, double price) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
    }

    // Construtor antigo (manter para compatibilidade temporária)
    public PedidoItemRequest(String productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
        this.productName = "";
        this.price = 0.0;
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
}