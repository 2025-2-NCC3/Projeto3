package com.example.pi.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pi.R;
import com.example.pi.managers.OrderManager;
import com.example.pi.models.Order;
import com.example.pi.models.OrderItem;
import com.example.pi.models.OrderStatus;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button btnTestOrder, btnViewOrders, btnClearOrders, btnViewProducts;
    private Button btnPendingOrders, btnPreparingOrders, btnReadyOrders;
    private TextView tvOrderStats;
    private OrderManager orderManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        orderManager = new OrderManager(this);
        initializeViews();
        setupClickListeners();
        updateOrderStats();

        // Verifica e cria pedido de exemplo se necessário
        orderManager.checkDatabase();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Atualiza estatísticas quando a activity retorna
        updateOrderStats();
    }

    private void initializeViews() {
        btnTestOrder = findViewById(R.id.btnTestOrder);
        btnViewOrders = findViewById(R.id.btnViewOrders);
        btnClearOrders = findViewById(R.id.btnClearOrders);
        btnViewProducts = findViewById(R.id.btnViewProducts);
        btnPendingOrders = findViewById(R.id.btnPendingOrders);
        btnPreparingOrders = findViewById(R.id.btnPreparingOrders);
        btnReadyOrders = findViewById(R.id.btnReadyOrders);
        tvOrderStats = findViewById(R.id.tvOrderStats);
    }

    private void setupClickListeners() {
        // Botão 1: Criar pedido de teste
        btnTestOrder.setOnClickListener(v -> createTestOrder());

        // Botão 2: Ver todos os pedidos
        btnViewOrders.setOnClickListener(v -> viewAllOrders());

        // Botão 3: Limpar todos os pedidos
        btnClearOrders.setOnClickListener(v -> clearAllOrders());

        // Botão 4: Ver produtos (futura implementação)
        btnViewProducts.setOnClickListener(v -> viewProducts());

        // Botão 5: Ver pedidos pendentes
        btnPendingOrders.setOnClickListener(v -> viewPendingOrders());

        // Botão 6: Ver pedidos em preparação
        btnPreparingOrders.setOnClickListener(v -> viewPreparingOrders());

        // Botão 7: Ver pedidos prontos
        btnReadyOrders.setOnClickListener(v -> viewReadyOrders());
    }

    private void createTestOrder() {
        try {
            Order order = new Order();
            order.setUserId("user_test_" + System.currentTimeMillis());
            order.setUserName("Cliente Teste");

            // Adicionar itens variados ao pedido
            OrderItem item1 = new OrderItem("prod1", "X-Burger", 2, 15.90);
            OrderItem item2 = new OrderItem("prod2", "Refrigerante", 1, 6.50);
            OrderItem item3 = new OrderItem("prod3", "Batata Frita", 1, 12.90);

            order.addItem(item1);
            order.addItem(item2);
            order.addItem(item3);

            boolean success = orderManager.createOrder(order);

            if (success) {
                Toast.makeText(this, "✅ Pedido criado com sucesso!", Toast.LENGTH_SHORT).show();
                updateOrderStats();
            } else {
                Toast.makeText(this, "❌ Erro ao criar pedido", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Toast.makeText(this, "Erro: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void viewAllOrders() {
        List<Order> orders = orderManager.getAllOrders();
        if (orders.isEmpty()) {
            Toast.makeText(this, "Nenhum pedido encontrado", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(this, OrderActivity.class);
            startActivity(intent);
        }
    }

    private void viewPendingOrders() {
        List<Order> orders = orderManager.getPendingOrders();
        if (orders.isEmpty()) {
            Toast.makeText(this, "Nenhum pedido pendente", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(this, OrderActivity.class);
            intent.putExtra("filter_status", "PENDING");
            startActivity(intent);
        }
    }

    private void viewPreparingOrders() {
        List<Order> orders = orderManager.getPreparingOrders();
        if (orders.isEmpty()) {
            Toast.makeText(this, "Nenhum pedido em preparação", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(this, OrderActivity.class);
            intent.putExtra("filter_status", "PREPARING");
            startActivity(intent);
        }
    }

    private void viewReadyOrders() {
        List<Order> orders = orderManager.getReadyOrders();
        if (orders.isEmpty()) {
            Toast.makeText(this, "Nenhum pedido pronto", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(this, OrderActivity.class);
            intent.putExtra("filter_status", "READY");
            startActivity(intent);
        }
    }

    private void clearAllOrders() {
        try {
            orderManager.clearAllOrders();
            Toast.makeText(this, "🗑️ Todos os pedidos foram removidos", Toast.LENGTH_SHORT).show();
            updateOrderStats();
        } catch (Exception e) {
            Toast.makeText(this, "Erro ao limpar pedidos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void viewProducts() {
        // Para implementação futura - pode abrir ProductsActivity
        Toast.makeText(this, "Funcionalidade em desenvolvimento", Toast.LENGTH_SHORT).show();

        // Exemplo futuro:
        // Intent intent = new Intent(this, ProductsActivity.class);
        // startActivity(intent);
    }

    private void updateOrderStats() {
        int totalOrders = orderManager.getTotalOrdersCount();
        int pendingOrders = orderManager.getOrdersCountByStatus(OrderStatus.PENDING);
        int preparingOrders = orderManager.getOrdersCountByStatus(OrderStatus.PREPARING);
        int readyOrders = orderManager.getOrdersCountByStatus(OrderStatus.READY);

        String statsText = String.format(
                "📊 Estatísticas:\n" +
                        "Total: %d pedidos\n" +
                        "⏳ Pendentes: %d\n" +
                        "👨‍🍳 Preparando: %d\n" +
                        "✅ Prontos: %d",
                totalOrders, pendingOrders, preparingOrders, readyOrders
        );

        tvOrderStats.setText(statsText);
    }

    // Método para demonstrar outras funcionalidades
    private void demonstrateFeatures() {
        // Exemplo: Atualizar status de um pedido
        List<Order> orders = orderManager.getAllOrders();
        if (!orders.isEmpty()) {
            Order firstOrder = orders.get(0);
            orderManager.startPreparingOrder(firstOrder.getOrderId());
        }

        // Exemplo: Deletar um pedido
        if (orders.size() > 1) {
            Order lastOrder = orders.get(orders.size() - 1);
            orderManager.deleteOrder(lastOrder.getOrderId());
        }
    }

    // Método para criar múltiplos pedidos de teste
    private void createMultipleTestOrders() {
        for (int i = 0; i < 3; i++) {
            Order order = new Order();
            order.setUserId("user_multi_" + i);
            order.setUserName("Cliente " + (i + 1));

            OrderItem item1 = new OrderItem("prod" + i, "Item " + i, i + 1, 10.0 + i);
            order.addItem(item1);

            orderManager.createOrder(order);
        }
        updateOrderStats();
    }
}