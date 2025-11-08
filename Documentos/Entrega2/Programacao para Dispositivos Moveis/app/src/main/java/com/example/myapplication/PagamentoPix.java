package com.example.myapplication;

import java.util.Date;

public class PagamentoPix {
    private String id;
    private String pedidoId;
    private double valor;
    private String qrCode; // QR Code em Base64
    private String qrCodeTexto; // Pix Copia e Cola
    private String status; // PENDING, APPROVED, REJECTED, CANCELLED
    private Date criadoEm;
    private Date atualizadoEm;
    private String transactionId; // ID da transação no gateway

    public PagamentoPix() {
        this.criadoEm = new Date();
        this.status = "PENDING";
    }

    // Getters e Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(String pedidoId) {
        this.pedidoId = pedidoId;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public String getQrCodeTexto() {
        return qrCodeTexto;
    }

    public void setQrCodeTexto(String qrCodeTexto) {
        this.qrCodeTexto = qrCodeTexto;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        this.atualizadoEm = new Date();
    }

    public Date getCriadoEm() {
        return criadoEm;
    }

    public Date getAtualizadoEm() {
        return atualizadoEm;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public boolean isPago() {
        return "APPROVED".equals(status);
    }

    public boolean isCancelado() {
        return "CANCELLED".equals(status) || "REJECTED".equals(status);
    }

    public boolean isPendente() {
        return "PENDING".equals(status);
    }
}