package com.example.pi.activities;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pi.R;
import com.example.pi.adapters.OrderItemsAdapter;
import com.example.pi.models.Order;
import com.example.pi.models.OrderItem;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class OrderDetailActivity extends AppCompatActivity {

    private TextView textOrderCode, textOrderDate, textOrderStatus, textOrderTotal;
    private RecyclerView recyclerViewOrderItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        // Inicializar views
        initializeViews();

        // Obter o pedido passado pela intent
        Order order = (Order) getIntent().getSerializableExtra("ORDER");

        if (order != null) {
            displayOrderDetails(order);
        } else {
            finish(); // Se não há pedido, fecha a activity
        }
    }

    private void initializeViews() {
        textOrderCode = findViewById(R.id.textOrderCode);
        textOrderDate = findViewById(R.id.textOrderDate);
        textOrderStatus = findViewById(R.id.textOrderStatus);
        textOrderTotal = findViewById(R.id.textOrderTotal);
        recyclerViewOrderItems = findViewById(R.id.recyclerViewOrderItems);

        // Configurar RecyclerView
        recyclerViewOrderItems.setLayoutManager(new LinearLayoutManager(this));
    }

    private void displayOrderDetails(Order order) {
        // Exibir informações básicas do pedido
        textOrderCode.setText(order.getUniqueCode());

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        textOrderDate.setText(sdf.format(order.getOrderDate()));

        textOrderStatus.setText(order.getStatus().getDescription());
        textOrderTotal.setText(String.format("Total: R$ %.2f", order.getTotalAmount()));

        // Exibir itens do pedido
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            OrderItemsAdapter adapter = new OrderItemsAdapter(order.getItems());
            recyclerViewOrderItems.setAdapter(adapter);
        }
    }
}