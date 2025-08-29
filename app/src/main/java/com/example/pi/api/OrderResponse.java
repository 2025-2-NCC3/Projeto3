package com.example.pi.api;

import com.example.pi.models.Order;

public class OrderResponse {
    private boolean success;
    private String message;
    private Order order;

    public OrderResponse(boolean success, String message, Order order) {
        this.success = success;
        this.message = message;
        this.order = order;
    }

    // Getters e Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
}