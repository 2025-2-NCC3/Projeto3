package com.example.myapplication;

import java.util.List;

//Todos os dados do pedido
public class OrderRequest {
    private String studentId;
    private String studentName;
    private List<OrderItemRequest> items;

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public List<OrderItemRequest> getItems() { return items; }
    public void setItems(List<OrderItemRequest> items) { this.items = items; }
}