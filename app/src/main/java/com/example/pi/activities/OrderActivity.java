package com.example.pi.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pi.R;
import com.example.pi.adapters.OrderAdapter;
import com.example.pi.managers.OrderManager;
import com.example.pi.models.Order;
import com.example.pi.models.OrderItem;
import com.example.pi.models.OrderStatus;

import java.util.List;

public class OrderActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private OrderAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        initializeViews();
        loadOrders();

        // Exemplo de como criar um pedido (pode ser removido depois)
        createSampleOrder();
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerViewOrders);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new OrderAdapter();
        recyclerView.setAdapter(adapter);
    }

    private void loadOrders() {
        OrderManager orderManager = new OrderManager(this);
        List<Order> orders = orderManager.getAllOrders();

        if (orders.isEmpty()) {
            Toast.makeText(this, "Nenhum pedido encontrado", Toast.LENGTH_SHORT).show();
        } else {
            adapter.setOrders(orders);
        }
    }

    // Método de exemplo para criar um pedido de teste
    private void createSampleOrder() {
        OrderManager orderManager = new OrderManager(this);

        // Criar um pedido de exemplo
        Order sampleOrder = new Order();
        sampleOrder.setUserId("user123");
        sampleOrder.setUserName("João Silva");

        // Adicionar itens ao pedido
        OrderItem item1 = new OrderItem("prod1", "X-Burger", 2, 15.90);
        OrderItem item2 = new OrderItem("prod2", "Refrigerante", 1, 6.50);
        sampleOrder.addItem(item1);
        sampleOrder.addItem(item2);

        // Criar o pedido
        boolean success = orderManager.createOrder(sampleOrder);

        if (success) {
            Toast.makeText(this, "Pedido de exemplo criado!", Toast.LENGTH_SHORT).show();
            // Recarregar a lista para mostrar o novo pedido
            loadOrders();
        } else {
            Toast.makeText(this, "Erro ao criar pedido de exemplo", Toast.LENGTH_SHORT).show();
        }
    }

    // Método para criar pedido quando usuário clicar em um botão (exemplo)
    private void createOrderFromButtonClick() {
        OrderManager orderManager = new OrderManager(this);

        Order newOrder = new Order();
        newOrder.setUserId("user456");
        newOrder.setUserName("Maria Santos");

        OrderItem item = new OrderItem("prod3", "Batata Frita", 1, 12.90);
        newOrder.addItem(item);

        boolean created = orderManager.createOrder(newOrder);

        if (created) {
            Toast.makeText(this, "Novo pedido criado!", Toast.LENGTH_SHORT).show();
            loadOrders(); // Atualizar a lista
        }
    }

    // Método para demonstrar outras funcionalidades
    private void demonstrateOtherFeatures() {
        OrderManager orderManager = new OrderManager(this);

        // Buscar pedidos pendentes
        List<Order> pendingOrders = orderManager.getPendingOrders();

        // Buscar pedidos de um usuário específico
        List<Order> userOrders = orderManager.getOrdersByUser("user123");

        // Atualizar status de um pedido
        if (!pendingOrders.isEmpty()) {
            String firstOrderId = pendingOrders.get(0).getOrderId();
            boolean updated = orderManager.updateOrderStatus(firstOrderId, OrderStatus.PREPARING);

            if (updated) {
                Toast.makeText(this, "Status atualizado!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}