package com.example.myapplication;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OrderManager {
    private static int orderCounter = 0;
    private static Map<String, Produto> products = new HashMap<>();
    private static Map<Integer, Order> orders = new HashMap<>();

    // Status dos pedidos
    public static final String STATUS_PENDING = "PENDENTE";
    public static final String STATUS_CONFIRMED = "CONFIRMADO";
    public static final String STATUS_PREPARING = "PREPARANDO";
    public static final String STATUS_READY = "PRONTO";
    public static final String STATUS_DELIVERED = "ENTREGUE";
    public static final String STATUS_CANCELLED = "CANCELADO";

    static {
        // Inicializar produtos
        products.put("1", new Produto(1, "Café", "Café quente", "sem_imagem", 5.0, 10, 3));
        products.put("2", new Produto(2, "Sanduíche", "Sanduíche natural", "sem_imagem", 2.0, 15, 2));
        products.put("3", new Produto(3, "Suco Natural", "Suco de laranja", "sem_imagem", 3.0, 8, 3));
        products.put("4", new Produto(4, "Salgado", "Coxinha", "coxinha_exemplo", 2.5, 20, 1));
        products.put("5", new Produto(5, "Água", "Água mineral", "sem_imagem", 4.0, 25, 3));
    }

    public static String generateOrderCode() {
        orderCounter++;
        String date = new SimpleDateFormat("ddMMyy", Locale.getDefault()).format(new Date());
        return "ORD" + date + String.format("%03d", orderCounter);
    }

    public static String validateStock(Order order) {
        for (OrderItem item : order.getItems()) {
            Produto produto = products.get(String.valueOf(item.getProductId()));
            if (produto == null) {
                return "Produto não encontrado: " + item.getProductName();
            }
            if (produto.getEstoque() < item.getQuantity()) {
                return "Estoque insuficiente para: " + item.getProductName() +
                        " (Disponível: " + produto.getEstoque() + ", Solicitado: " + item.getQuantity() + ")";
            }
        }
        return null;
    }

    public static void updateStock(Order order) {
        for (OrderItem item : order.getItems()) {
            Produto produto = products.get(String.valueOf(item.getProductId()));
            if (produto != null) {
                produto.setEstoque(produto.getEstoque() - item.getQuantity());
            }
        }
    }

    public static OrderResponse createOrder(OrderRequest request) {
        Order order = new Order();
        order.setStudentId(request.getStudentId());
        order.setStudentName(request.getStudentName());

        // Adicionar itens ao pedido
        for (OrderItemRequest itemRequest : request.getItems()) {
            Produto produto = products.get(itemRequest.getProductId());
            if (produto != null) {
                OrderItem item = new OrderItem(
                        produto.getId(),
                        produto.getNome(),
                        itemRequest.getQuantity(),
                        produto.getPreco()
                );
                order.addItem(item);
            }
        }

        // Validar estoque
        String stockError = validateStock(order);
        if (stockError != null) {
            return new OrderResponse(false, stockError, null);
        }

        // Confirmar pedido
        updateStock(order);
        order.setStatus(STATUS_CONFIRMED);
        orders.put(order.getId(), order);

        return new OrderResponse(true, "Pedido criado com sucesso! Código: " + order.getCode(), order);
    }

    // Métodos de consulta
    public static Order getOrder(int orderId) {
        return orders.get(orderId);
    }

    public static boolean updateOrderStatus(int orderId, String newStatus) {
        Order order = orders.get(orderId);
        if (order != null) {
            order.setStatus(newStatus);
            return true;
        }
        return false;
    }

    public static Map<Integer, Order> getStudentOrders(String studentId) {
        Map<Integer, Order> studentOrders = new HashMap<>();
        for (Order order : orders.values()) {
            if (studentId.equals(order.getStudentId())) {
                studentOrders.put(order.getId(), order);
            }
        }
        return studentOrders;
    }

    public static List<Order> getStudentOrdersList(String studentId) {
        List<Order> studentOrders = new ArrayList<>();
        for (Order order : orders.values()) {
            if (studentId.equals(order.getStudentId())) {
                studentOrders.add(order);
            }
        }
        return studentOrders;
    }

    public static Produto getProduct(String productId) {
        return products.get(productId);
    }

    public static Map<String, Produto> getAllProducts() {
        return new HashMap<>(products);
    }

    public static List<Produto> getAvailableProducts() {
        List<Produto> availableProducts = new ArrayList<>();
        for (Produto produto : products.values()) {
            if (produto.getEstoque() > 0) {
                availableProducts.add(produto);
            }
        }
        return availableProducts;
    }
}