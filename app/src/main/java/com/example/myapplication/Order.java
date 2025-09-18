package com.example.myapplication;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

//Aqui é o modelo do pedido
public class Order {
    private String id;
    private String studentId;
    private String studentName;
    private List<OrderItem> items;
    private double total;
    private String status;
    private Date createdAt;
    private String code;

    public Order() {
        this.id = UUID.randomUUID().toString();
        this.status = "PENDENTE";
        this.createdAt = new Date();
        this.items = new ArrayList<>();
        this.code = OrderManager.generateOrderCode();
    }

    public String getId() { return id; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public List<OrderItem> getItems() { return items; }
    public double getTotal() { return total; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Date getCreatedAt() { return createdAt; }
    public String getCode() { return code; }

    public void addItem(OrderItem item) {
        items.add(item);
        calculateTotal();
    }

    public void calculateTotal() {
        total = 0;
        for (OrderItem item : items) {
            total += item.getQuantity() * item.getPrice();
        }
    }
}