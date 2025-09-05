package com.example.pi.managers;

import android.content.Context;
import android.widget.Toast;

import com.example.pi.database.OrderRepository;
import com.example.pi.models.Order;
import com.example.pi.models.OrderItem;
import com.example.pi.models.OrderStatus;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class OrderManager {
    private final OrderRepository orderRepository;
    private final Context context;

    public OrderManager(Context context) {
        this.context = context;
        this.orderRepository = new OrderRepository(context);
    }

    // ========== MÉTODOS DE CONSULTA ==========

    public List<Order> getAllOrders() {
        return orderRepository.getAllOrders();
    }

    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.getOrdersByStatus(status);
    }

    public List<Order> getOrdersByUser(String userId) {
        return orderRepository.getOrdersByUser(userId);
    }

    public Order getOrderById(String orderId) {
        return orderRepository.getOrderById(orderId);
    }

    public int getTotalOrdersCount() {
        return orderRepository.getOrdersCount();
    }

    // ========== MÉTODOS DE CRIAÇÃO ==========

    public boolean createOrder(Order order) {
        try {
            // Gerar código único para o pedido
            String uniqueCode = generateUniqueCode();
            order.setUniqueCode(uniqueCode);

            // Definir data atual e status inicial
            order.setOrderDate(new Date());
            order.setStatus(OrderStatus.PENDING);

            // Calcular total automaticamente se não estiver definido
            if (order.getTotalAmount() == 0 && order.getItems() != null) {
                double total = 0;
                for (OrderItem item : order.getItems()) {
                    total += item.getSubtotal();
                }
                order.setTotalAmount(total);
            }

            // Salvar pedido no banco local
            long result = orderRepository.createOrder(order);

            if (result != -1) {
                showToast("Pedido criado: " + order.getUniqueCode());
                return true;
            } else {
                showToast("Erro ao criar pedido");
                return false;
            }

        } catch (Exception e) {
            showToast("Erro: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean createOrder(String userId, String userName, List<OrderItem> items) {
        Order order = new Order();
        order.setUserId(userId);
        order.setUserName(userName);

        if (items != null && !items.isEmpty()) {
            for (OrderItem item : items) {
                order.addItem(item);
            }
        }

        return createOrder(order);
    }

    // ========== MÉTODOS DE ATUALIZAÇÃO ==========

    public boolean updateOrderStatus(String orderId, OrderStatus newStatus) {
        int result = orderRepository.updateOrderStatus(orderId, newStatus);
        if (result > 0) {
            showToast("Status atualizado para: " + newStatus.getDescription());
            return true;
        } else {
            showToast("Erro ao atualizar status");
            return false;
        }
    }

    public boolean confirmOrder(String orderId) {
        return updateOrderStatus(orderId, OrderStatus.CONFIRMED);
    }

    public boolean startPreparingOrder(String orderId) {
        return updateOrderStatus(orderId, OrderStatus.PREPARING);
    }

    public boolean markOrderAsReady(String orderId) {
        return updateOrderStatus(orderId, OrderStatus.READY);
    }

    public boolean markOrderAsDelivered(String orderId) {
        return updateOrderStatus(orderId, OrderStatus.DELIVERED);
    }

    public boolean cancelOrder(String orderId) {
        return updateOrderStatus(orderId, OrderStatus.CANCELLED);
    }

    // ========== MÉTODOS DE EXCLUSÃO ==========

    public boolean deleteOrder(String orderId) {
        int result = orderRepository.deleteOrder(orderId);
        if (result > 0) {
            showToast("Pedido excluído");
            return true;
        } else {
            showToast("Erro ao excluir pedido");
            return false;
        }
    }

    public void clearAllOrders() {
        orderRepository.clearAllOrders();
        showToast("Todos os pedidos foram removidos");
    }

    // ========== MÉTODOS DE VERIFICAÇÃO ==========

    public boolean orderExists(String orderId) {
        return getOrderById(orderId) != null;
    }

    public int getOrdersCountByStatus(OrderStatus status) {
        List<Order> orders = getOrdersByStatus(status);
        return orders != null ? orders.size() : 0;
    }

    // ========== MÉTODOS DE FILTRO (CONVENIÊNCIA) ==========

    public List<Order> getPendingOrders() {
        return getOrdersByStatus(OrderStatus.PENDING);
    }

    public List<Order> getPreparingOrders() {
        return getOrdersByStatus(OrderStatus.PREPARING);
    }

    public List<Order> getReadyOrders() {
        return getOrdersByStatus(OrderStatus.READY);
    }

    public List<Order> getDeliveredOrders() {
        return getOrdersByStatus(OrderStatus.DELIVERED);
    }

    public List<Order> getCancelledOrders() {
        return getOrdersByStatus(OrderStatus.CANCELLED);
    }

    // ========== MÉTODOS UTILITÁRIOS ==========

    private String generateUniqueCode() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        String timestamp = sdf.format(new Date());
        Random random = new Random();
        int randomNum = random.nextInt(900) + 100; // Gera número entre 100 e 999

        return "ORD" + timestamp + randomNum;
    }

    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    // ========== MÉTODOS ESTÁTICOS (UTILITÁRIOS) ==========

    public static String formatOrderDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(date);
    }

    public static String formatCurrency(double value) {
        return String.format(Locale.getDefault(), "R$ %.2f", value);
    }

    // ========== MÉTODO PARA TESTE RÁPIDO ==========

    public void createSampleOrder() {
        Order sampleOrder = new Order();
        sampleOrder.setUserId("user_test_001");
        sampleOrder.setUserName("João Silva (Teste)");

        OrderItem item1 = new OrderItem("prod1", "X-Burger", 2, 15.90);
        OrderItem item2 = new OrderItem("prod2", "Refrigerante", 1, 6.50);
        sampleOrder.addItem(item1);
        sampleOrder.addItem(item2);

        createOrder(sampleOrder);
    }

    // ========== MÉTODO PARA VERIFICAÇÃO RÁPIDA ==========

    public void checkDatabase() {
        int count = getTotalOrdersCount();
        showToast("Total de pedidos: " + count);

        if (count == 0) {
            showToast("Criando pedido de exemplo...");
            createSampleOrder();
        }
    }
}