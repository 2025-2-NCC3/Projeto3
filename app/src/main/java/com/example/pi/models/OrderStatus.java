package com.example.pi.models;

public enum OrderStatus {
    PENDING("Pendente"),         // Pedido feito mas ainda não confirmado
    CONFIRMED("Confirmado"),     // Pedido confirmado pela cantina
    PREPARING("Preparando"),     // Pedido em preparação
    READY("Pronto"),             // Pedido pronto para retirada
    DELIVERED("Entregue"),       // Pedido entregue ao cliente
    CANCELLED("Cancelado");      // Pedido cancelado


    private String description; // Descrição para exibição

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}