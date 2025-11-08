package com.example.myapplication;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PedidoManager {
    private static int orderCounter = 0;
    private static Map<String, Produto> products = new HashMap<>();
    private static Map<String, Pedido> orders = new HashMap<>();

    // Status dos pedidos
    public static final String STATUS_PENDING = "PENDENTE";
    public static final String STATUS_CONFIRMED = "CONFIRMADO";
    public static final String STATUS_PREPARING = "PREPARANDO";
    public static final String STATUS_READY = "PRONTO";
    public static final String STATUS_DELIVERED = "ENTREGUE";
    public static final String STATUS_CANCELLED = "CANCELADO";

    // ⭐ NOVO: Método para atualizar a lista de produtos
    public static void updateProducts(List<Produto> produtosList) {
        products.clear();
        for (Produto produto : produtosList) {
            products.put(produto.getId(), produto);
        }
    }

    // ⭐ NOVO: Método para adicionar/atualizar um produto
    public static void addOrUpdateProduct(Produto produto) {
        products.put(produto.getId(), produto);
    }

    public static String generateOrderCode() {
        orderCounter++;
        String date = new SimpleDateFormat("ddMMyy", Locale.getDefault()).format(new Date());
        return "ORD" + date + String.format("%03d", orderCounter);
    }

    public static String validateStock(Pedido pedido) {
        for (PedidoItem item : pedido.getItems()) {
            Produto produto = products.get(item.getProductId());
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

    public static void updateStock(Pedido pedido) {
        for (PedidoItem item : pedido.getItems()) {
            Produto produto = products.get(item.getProductId());
            if (produto != null) {
                produto.setEstoque(produto.getEstoque() - item.getQuantity());
            }
        }
    }

    public static PedidoResponse createOrder(PedidoRequest request) {
        Pedido pedido = new Pedido();
        pedido.setStudentId(request.getStudentId());
        pedido.setStudentName(request.getStudentName());

        // Adicionar itens ao pedido
        for (PedidoItemRequest itemRequest : request.getItems()) {
            Produto produto = products.get(itemRequest.getProductId());
            if (produto != null) {
                PedidoItem item = new PedidoItem(
                        produto.getId(),
                        produto.getNome(),
                        itemRequest.getQuantity(),
                        produto.getPreco()
                );
                pedido.addItem(item);
            }
        }

        // Validar estoque
        String stockError = validateStock(pedido);
        if (stockError != null) {
            return new PedidoResponse(false, stockError, null);
        }

        // Confirmar pedido
        updateStock(pedido);
        pedido.setStatus(STATUS_CONFIRMED);
        orders.put(pedido.getId(), pedido);

        return new PedidoResponse(true, "Pedido criado com sucesso! Código: " + pedido.getCode(), pedido);
    }

    // Métodos de consulta
    public static Pedido getOrder(String orderId) {
        return orders.get(orderId);
    }

    public static boolean updateOrderStatus(String orderId, String newStatus) {
        Pedido pedido = orders.get(orderId);
        if (pedido != null) {
            pedido.setStatus(newStatus);
            return true;
        }
        return false;
    }

    public static Map<String, Pedido> getStudentOrders(String studentId) {
        Map<String, Pedido> studentOrders = new HashMap<>();
        for (Pedido pedido : orders.values()) {
            if (studentId.equals(pedido.getStudentId())) {
                studentOrders.put(pedido.getId(), pedido);
            }
        }
        return studentOrders;
    }

    public static List<Pedido> getStudentOrdersList(String studentId) {
        List<Pedido> studentPedidos = new ArrayList<>();
        for (Pedido pedido : orders.values()) {
            if (studentId.equals(pedido.getStudentId())) {
                studentPedidos.add(pedido);
            }
        }
        return studentPedidos;
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