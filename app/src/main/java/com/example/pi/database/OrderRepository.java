package com.example.pi.database;

import android.content.Context;

import com.example.pi.models.Order;
import com.example.pi.models.OrderStatus;

import java.util.List;

public class OrderRepository {
    private final DatabaseHelper dbHelper;

    public OrderRepository(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    // Criar um pedido
    public long createOrder(Order order) {
        return dbHelper.addOrder(order);
    }

    // Buscar todos os pedidos
    public List<Order> getAllOrders() {
        return dbHelper.getAllOrders();
    }

    // Buscar pedidos por status
    public List<Order> getOrdersByStatus(OrderStatus status) {
        return dbHelper.getOrdersByStatus(status);
    }

    // Buscar pedidos por usuário
    public List<Order> getOrdersByUser(String userId) {
        return dbHelper.getOrdersByUser(userId);
    }

    // Buscar pedido por ID
    public Order getOrderById(String orderId) {
        return dbHelper.getOrderById(orderId);
    }

    // Atualizar status do pedido
    public int updateOrderStatus(String orderId, OrderStatus status) {
        return dbHelper.updateOrderStatus(orderId, status);
    }

    // Deletar pedido
    public int deleteOrder(String orderId) {
        return dbHelper.deleteOrder(orderId);
    }

    // Obter contagem de pedidos
    public int getOrdersCount() {
        return dbHelper.getOrdersCount();
    }

    // Limpar todos os pedidos (apenas para desenvolvimento)
    public void clearAllOrders() {
        dbHelper.clearAllData();
    }
}