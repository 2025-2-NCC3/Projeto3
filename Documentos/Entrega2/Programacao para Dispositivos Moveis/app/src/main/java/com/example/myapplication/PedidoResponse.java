package com.example.myapplication;

//Resultado depois que o pedido é processado se deu certo ou não
public class PedidoResponse {
    private boolean success;
    private String message;
    private Pedido pedido;

    public PedidoResponse(boolean success, String message, Pedido pedido) {
        this.success = success;
        this.message = message;
        this.pedido = pedido;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Pedido getOrder() { return pedido; }
}