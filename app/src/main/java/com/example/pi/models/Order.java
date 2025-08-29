package com.example.pi.models;

import java.util.Date;
import java.util.List;

public class Order {
    private String orderId;
    private String userId;
    private String userName;
    private Date orderDate;
    private OrderStatus status;
    private double totalAmount;
    private List<OrderItem> items;
    private String uniqueCode;

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

    public void addItem(OrderItem item) {
        this.items.add(item);
        this.totalAmount += item.getSubtotal();
    }
}