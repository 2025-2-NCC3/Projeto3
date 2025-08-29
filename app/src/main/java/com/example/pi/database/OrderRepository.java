package com.example.pi.database;

import android.content.Context;

import com.example.pi.models.Order;
import com.example.pi.models.OrderStatus;

import java.util.List;

public class OrderRepository {
    private DatabaseHelper dbHelper;

    public OrderRepository(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public long createOrder(Order order) {
        return dbHelper.addOrder(order);
    }

    public List<Order> getAllOrders() {
        return dbHelper.getAllOrders();
    }

    public List<Order> getOrdersByStatus(OrderStatus status) {
        List<Order> allOrders = getAllOrders();
        // Filtrar por status
        allOrders.removeIf(order -> order.getStatus() != status);
        return allOrders;
    }
}