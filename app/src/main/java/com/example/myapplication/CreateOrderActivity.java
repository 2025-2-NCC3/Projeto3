package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

public class CreateOrderActivity extends AppCompatActivity {

    private EditText editStudentId, editStudentName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        editStudentId = findViewById(R.id.editStudentId);
        editStudentName = findViewById(R.id.editStudentName);
        Button btnCreateOrder = findViewById(R.id.btnCreateOrder);

        btnCreateOrder.setOnClickListener(v -> createOrder());
    }
   //Método para criar pedido
    private void createOrder() {
        // Pega dados dos campos de texto
        String studentId = editStudentId.getText().toString().trim();
        String studentName = editStudentName.getText().toString().trim();
        // Valida se não está vazio
        if (studentId.isEmpty() || studentName.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Criar request do pedido
        OrderRequest request = new OrderRequest();
        request.setStudentId(studentId);
        request.setStudentName(studentName);

        // Adicionar itens(exemplo)
        List<OrderItemRequest> items = new ArrayList<>();

        OrderItemRequest item1 = new OrderItemRequest();
        item1.setProductId("1"); // Café
        item1.setQuantity(2);
        items.add(item1);

        OrderItemRequest item2 = new OrderItemRequest();
        item2.setProductId("3"); // Suco Natural
        item2.setQuantity(1);
        items.add(item2);

        request.setItems(items);

        // Chamar o OrdeManager
        OrderResponse response = OrderManager.createOrder(request);
        // Exibir mensagem de sucesso ou erro
        if (response.isSuccess()) {
            Order order = response.getOrder();
            String message = "✅ Pedido criado!\n" +
                    "Código: " + order.getCode() + "\n" +
                    "Status: " + order.getStatus() + "\n" +
                    "Total: R$ " + String.format("%.2f", order.getTotal());

            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            finish();
        } else {
            Toast.makeText(this, "❌ " + response.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}