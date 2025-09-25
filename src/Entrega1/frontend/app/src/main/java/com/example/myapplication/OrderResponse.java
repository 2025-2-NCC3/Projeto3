package com.example.myapplication;

//Resultado depois que o pedido é processado se deu certo ou não
public class OrderResponse {
    private boolean success;
    private String message;
    private Order order;

    public OrderResponse(boolean success, String message, Order order) {
        this.success = success;
        this.message = message;
        this.order = order;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Order getOrder() { return order; }
}