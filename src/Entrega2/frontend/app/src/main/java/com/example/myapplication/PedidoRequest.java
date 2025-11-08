package com.example.myapplication;

import java.util.List;

//Todos os dados do pedido
public class PedidoRequest {
    private String studentId;
    private String studentName;
    private List<PedidoItemRequest> items;

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public List<PedidoItemRequest> getItems() { return items; }
    public void setItems(List<PedidoItemRequest> items) { this.items = items; }
}