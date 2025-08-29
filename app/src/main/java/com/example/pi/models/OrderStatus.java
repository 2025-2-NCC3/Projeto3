package com.example.pi.models;

public enum OrderStatus {
    PENDING("Pendente"),
    CONFIRMED("Confirmado"),
    PREPARING("Preparando"),
    READY("Pronto"),
    DELIVERED("Entregue"),
    CANCELLED("Cancelado");

    private String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}