package com.example.pi.api;

import java.util.List;

public class OrderRequest {
    private String userId;
    private String userName;
    private List<OrderItemRequest> items;

    // Getters e Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public List<OrderItemRequest> getItems() { return items; }
    public void setItems(List<OrderItemRequest> items) { this.items = items; }
}