package com.example.myapplication;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class OrderManager {
    //contador pra gerar códigos únicos
    private static int orderCounter = 0;
    //estoque da cantina
    private static Map<String, Product> products = new HashMap<>();
    //pedidos em andamento
    private static Map<String, Order> orders = new HashMap<>();

    // Status dos pedidos
    public static final String STATUS_PENDING = "PENDENTE";
    public static final String STATUS_CONFIRMED = "CONFIRMADO";
    public static final String STATUS_PREPARING = "PREPARANDO";
    public static final String STATUS_READY = "PRONTO";
    public static final String STATUS_DELIVERED = "ENTREGUE";
    public static final String STATUS_CANCELLED = "CANCELADO";

    // Inicializar produtos(simulação de estoque)
    static {
        products.put("1", new Product("1", "Café", 2.50, 50));
        products.put("2", new Product("2", "Sanduíche", 8.00, 20));
        products.put("3", new Product("3", "Suco Natural", 5.00, 30));
        products.put("4", new Product("4", "Salgado", 4.50, 25));
        products.put("5", new Product("5", "Água", 3.00, 40));
    }

    // Gera código único e sequencial
    public static String generateOrderCode() {
        orderCounter++;
        String date = new SimpleDateFormat("ddMMyy", Locale.getDefault()).format(new Date());
        return "ORD" + date + String.format("%03d", orderCounter);
    }

    // Verifica se tem quantidade suficiente de cada produto antes de confirmar o pedido
    public static String validateStock(Order order) {
        for (OrderItem item : order.getItems()) {
            Product product = products.get(item.getProductId());
            if (product == null) {
                return "Produto não encontrado: " + item.getProductName();
            }
            if (!product.hasStock(item.getQuantity())) {
                return "Estoque insuficiente para: " + item.getProductName();
            }
        }
        return null;
    }

    // Atualiza estoque depois que o pedido é confirmado
    public static void updateStock(Order order) {
        for (OrderItem item : order.getItems()) {
            Product product = products.get(item.getProductId());
            if (product != null) {
                product.reduceStock(item.getQuantity());
            }
        }
    }

    // Endpoint POST/api/orders
    public static OrderResponse createOrder(OrderRequest request) {
        //cria pedido
        Order order = new Order();
        order.setStudentId(request.getStudentId());
        order.setStudentName(request.getStudentName());

        // adiciona itens
        for (OrderItemRequest itemRequest : request.getItems()) {
            Product product = products.get(itemRequest.getProductId());
            if (product != null) {
                order.addItem(new OrderItem(
                        product.getId(),
                        product.getName(),
                        itemRequest.getQuantity(),
                        product.getPrice()
                ));
            }
        }

        // valida estoque
        String stockError = validateStock(order);
        if (stockError != null) {
            return new OrderResponse(false, stockError, null);
        }

        // confirma pedido
        updateStock(order);
        order.setStatus(STATUS_CONFIRMED);
        orders.put(order.getId(), order); //salva pedido

        return new OrderResponse(true, "Pedido criado com sucesso", order);
    }

//Métodos de Consulta
    // Buscar pedido por id
    public static Order getOrder(String orderId) {
        return orders.get(orderId);
    }

    // Atualizar status
    public static boolean updateOrderStatus(String orderId, String newStatus) {
        Order order = orders.get(orderId);
        if (order != null) {
            order.setStatus(newStatus);
            return true;
        }
        return false; //pedido não encontrado
    }

    // Buscar pedidos do aluno
    public static Map<String, Order> getStudentOrders(String studentId) {
        Map<String, Order> studentOrders = new HashMap<>();
        for (Order order : orders.values()) {
            if (studentId.equals(order.getStudentId())) {
                studentOrders.put(order.getId(), order);
            }
        }
        return studentOrders;
    }
}