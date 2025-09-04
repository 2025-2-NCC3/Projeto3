package com.example.pi.exceptions;

public class InsufficientStockException extends Exception {
    public InsufficientStockException(String productName) {
        super("Estoque insuficiente para: " + productName);
    }
}