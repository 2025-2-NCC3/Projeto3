package com.example.pi.models;

import java.util.Date;
import java.util.List;

public class Order {
    private String orderId;          // ID único do pedido
    private String userId;           // ID do usuário que fez o pedido
    private String userName;         // Nome do usuário
    private Date orderDate;          // Data e hora do pedido
    private OrderStatus status;      // Status atual (Pendente, Confirmado, etc.)
    private double totalAmount;      // Valor total do pedido
    private List<OrderItem> items;   // Lista de itens do pedido
    private String uniqueCode;       // Código único para identificação


    public Order() {
        this.status = OrderStatus.PENDING;
        this.orderDate = new Date();
    }

    // Getters e Setters
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public Date getOrderDate() { return orderDate; }
    public void setOrderDate(Date orderDate) { this.orderDate = orderDate; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    public String getUniqueCode() { return uniqueCode; }
    public void setUniqueCode(String uniqueCode) { this.uniqueCode = uniqueCode; }

    // Método para adicionar itens e atualizar o total automaticamente
    public void addItem(OrderItem item) {
        this.items.add(item);
        this.totalAmount += item.getSubtotal();
    }
}