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
    private static Map<String, Produto> products = new HashMap<>();
    //pedidos em andamento
    private static Map<Integer, Order> orders = new HashMap<>();

    // Status dos pedidos
    public static final String STATUS_PENDING = "PENDENTE";
    public static final String STATUS_CONFIRMED = "CONFIRMADO";
    public static final String STATUS_PREPARING = "PREPARANDO";
    public static final String STATUS_READY = "PRONTO";
    public static final String STATUS_DELIVERED = "ENTREGUE";
    public static final String STATUS_CANCELLED = "CANCELADO";

    // Inicializar produtos (simulação de estoque)
    static {
        products.put("1", new Produto(1, "Café", "Café quente", "descrição", 5.0, 1, 3, R.drawable.sem_imagem));
        products.put("2", new Produto(2, "Sanduíche", "Sanduíche natural", "descrição", 2.0, 2, 2, R.drawable.sem_imagem));
        products.put("3", new Produto(3, "Suco Natural", "Suco de laranja", "descrição", 3.0, 3, 3, R.drawable.sem_imagem));
        products.put("4", new Produto(4, "Salgado", "Coxinha", "descrição", 2.5, 1, 1, R.drawable.coxinha_exemplo));
        products.put("5", new Produto(5, "Água", "Água mineral", "descrição", 4.0, 3, 3, R.drawable.sem_imagem));
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
            Produto produto = products.get(item.getProductId());
            if (produto == null) {
                return "Produto não encontrado: " + item.getProductName();
            }
            if (produto.getEstoque() < item.getQuantity()) {
                return "Estoque insuficiente para: " + item.getProductName();
            }
        }
        return null;
    }

    // Atualiza estoque depois que o pedido é confirmado
    public static void updateStock(Order order) {
        for (OrderItem item : order.getItems()) {
            Produto produto = products.get(item.getProductId());
            if (produto != null) {
                produto.setEstoque(produto.getEstoque() - item.getQuantity());
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
        /*for (OrderItemRequest itemRequest : request.getItems()) {
            Produto produto = products.get(itemRequest.getProductId());
            if (produto != null) {
                order.addItem(new OrderItem(
                        produto.getId(),
                        produto.getNome(),
                        itemRequest.getQuantity(),
                        produto.getPreco()
                ));
            }
        }*/

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
    public static Map<Integer, Order> getStudentOrders(String studentId) {
        Map<Integer, Order> studentOrders = new HashMap<>();
        for (Order order : orders.values()) {
            if (studentId.equals(order.getStudentId())) {
                studentOrders.put(order.getId(), order);
            }
        }
        return studentOrders;
    }
}