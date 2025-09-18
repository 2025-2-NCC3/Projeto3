package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView;
import java.util.ArrayList;
import java.util.List;

public class CreateOrderActivity extends AppCompatActivity {

    private Spinner spinnerProdutos;
    private TextView textViewPreco;
    private Button btnConfirmarPedido;

    // Lista de produtos disponíveis
    private List<Produto> produtosDisponiveis = new ArrayList<>();
    private Produto produtoSelecionado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        // Inicializa a lista de produtos
        inicializarProdutos();

        spinnerProdutos = findViewById(R.id.spinnerProdutos);
        textViewPreco = findViewById(R.id.textViewPreco);
        btnConfirmarPedido = findViewById(R.id.btnConfirmarPedido);

        // Configura o Spinner de produtos
        configurarSpinner();


        btnConfirmarPedido.setOnClickListener(v -> criarPedido());
    }

    private void inicializarProdutos() {
        // Adiciona produtos disponíveis (simulados)
        produtosDisponiveis.add(new Produto("1", "Café", "Café quente", 2.50, 50, 1));
        produtosDisponiveis.add(new Produto("2", "Sanduíche", "Sanduíche natural", 8.00, 20, 2));
        produtosDisponiveis.add(new Produto("3", "Suco Natural", "Suco de laranja", 5.00, 30, 3));
        produtosDisponiveis.add(new Produto("4", "Salgado", "Coxinha", 4.50, 25, 2));
        produtosDisponiveis.add(new Produto("5", "Água", "Água mineral", 3.00, 40, 3));

        // Seleciona o primeiro produto por padrão
        produtoSelecionado = produtosDisponiveis.get(0);
    }

    private void configurarSpinner() {
        // Cria lista de nomes de produtos para o Spinner
        List<String> nomesProdutos = new ArrayList<>();
        for (Produto produto : produtosDisponiveis) {
            nomesProdutos.add(produto.getNome() + " - R$ " + produto.getPreco());
        }

        // Configura o adapter do Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                nomesProdutos
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProdutos.setAdapter(adapter);

        // Atualiza o preço quando seleciona um produto diferente
        spinnerProdutos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                produtoSelecionado = produtosDisponiveis.get(position);
                atualizarPreco();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Não faz nada quando nada eh selecionado
            }
        });
    }

    private void atualizarPreco() {
        if (produtoSelecionado != null) {
            textViewPreco.setText("Preço: R$ " + String.format("%.2f", produtoSelecionado.getPreco()));
        }
    }

    private void criarPedido() {
        // Cria o pedido com um usuário fixo
        OrderRequest request = new OrderRequest();
        request.setStudentId("2023001"); // Usuário fixo
        request.setStudentName("Cliente Cantina"); // Nome fixo

        // Adiciona apenas o produto selecionado
        List<OrderItemRequest> items = new ArrayList<>();

        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(produtoSelecionado.getId());
        item.setQuantity(1); // Apenas 1 unidade por enquanto
        items.add(item);

        request.setItems(items);

        // Envia para processamento
        OrderResponse response = OrderManager.createOrder(request);

        // Mostra resultado
        if (response.isSuccess()) {
            Order order = response.getOrder();
            String message = "✅ Pedido criado!\n" +
                    "Item: " + produtoSelecionado.getNome() + "\n" +
                    "Código: " + order.getCode() + "\n" +
                    "Total: R$ " + String.format("%.2f", order.getTotal());

            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            finish();
        } else {
            Toast.makeText(this, "❌ " + response.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}