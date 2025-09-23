package com.example.myapplication;

import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CreateOrderActivitySupabase extends AppCompatActivity {

    private Spinner spinnerProdutos;
    private TextView textViewPreco;
    private Button btnConfirmarPedido;

    // Managers
    private SupabaseOrderManager supabaseOrderManager;

    // Lista de produtos disponíveis
    private List<Produto> produtosDisponiveis = new ArrayList<>();
    private Produto produtoSelecionado;

    // Dados do usuário (normalmente viriam da sessão/login)
    private String userAccessToken = "seu_access_token_aqui";
    private String studentId = "2023001";
    private String studentName = "Cliente Cantina";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        // Inicializar managers
        supabaseOrderManager = SupabaseOrderManager.getInstance(this);

        // Inicializa a lista de produtos
        inicializarProdutos();

        spinnerProdutos = findViewById(R.id.spinnerProdutos);
        textViewPreco = findViewById(R.id.textViewPreco);
        btnConfirmarPedido = findViewById(R.id.btnConfirmarPedido);

        // Configura o Spinner de produtos
        configurarSpinner();

        // Recebe o intent com as informações do produto comprado
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            Produto objetoRecebido = (Produto) bundle.getSerializable("produtoComprado", Produto.class);
            if (objetoRecebido != null) {
                // Pre-selecionar o produto recebido
                selecionarProduto(objetoRecebido);
            }
        }

        btnConfirmarPedido.setOnClickListener(v -> criarPedidoSupabase());
    }

    private void inicializarProdutos() {
        // Usar os mesmos produtos do OrderManager
        produtosDisponiveis.add(new Produto(1, "Café", "Café quente", "sem_imagem", 5.0, 10, 3));
        produtosDisponiveis.add(new Produto(2, "Sanduíche", "Sanduíche natural", "sem_imagem", 2.0, 15, 2));
        produtosDisponiveis.add(new Produto(3, "Suco Natural", "Suco de laranja", "sem_imagem", 3.0, 8, 3));
        produtosDisponiveis.add(new Produto(4, "Salgado", "Coxinha", "coxinha_exemplo", 2.5, 20, 1));
        produtosDisponiveis.add(new Produto(5, "Água", "Água mineral", "sem_imagem", 4.0, 25, 3));

        // Seleciona o primeiro produto por padrão
        if (!produtosDisponiveis.isEmpty()) {
            produtoSelecionado = produtosDisponiveis.get(0);
        }
    }

    private void configurarSpinner() {
        // Cria lista de nomes de produtos para o Spinner
        List<String> nomesProdutos = new ArrayList<>();
        for (Produto produto : produtosDisponiveis) {
            String displayText = produto.getNome() + " - R$ " +
                    String.format(Locale.getDefault(), "%.2f", produto.getPreco()) +
                    " (Estoque: " + produto.getEstoque() + ")";
            nomesProdutos.add(displayText);
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
                // Não faz nada quando nada é selecionado
            }
        });
    }

    private void selecionarProduto(Produto produto) {
        // Encontrar o produto na lista e selecionar no spinner
        for (int i = 0; i < produtosDisponiveis.size(); i++) {
            if (produtosDisponiveis.get(i).getId() == produto.getId()) {
                spinnerProdutos.setSelection(i);
                produtoSelecionado = produtosDisponiveis.get(i);
                break;
            }
        }
    }

    private void atualizarPreco() {
        if (produtoSelecionado != null) {
            String precoText = "Preço: R$ " + String.format(Locale.getDefault(), "%.2f", produtoSelecionado.getPreco());
            textViewPreco.setText(precoText);

            // Habilitar/desabilitar botão baseado no estoque
            btnConfirmarPedido.setEnabled(produtoSelecionado.getEstoque() > 0);

            if (produtoSelecionado.getEstoque() == 0) {
                precoText += " - ESGOTADO";
                textViewPreco.setText(precoText);
            }
        }
    }

    private void criarPedidoSupabase() {
        if (produtoSelecionado == null) {
            Toast.makeText(this, "Selecione um produto", Toast.LENGTH_SHORT).show();
            return;
        }

        if (produtoSelecionado.getEstoque() <= 0) {
            Toast.makeText(this, "Produto sem estoque", Toast.LENGTH_SHORT).show();
            return;
        }

        // Desabilitar botão para evitar duplo clique
        btnConfirmarPedido.setEnabled(false);
        btnConfirmarPedido.setText("Processando...");

        // Criar OrderRequest
        OrderRequest request = new OrderRequest();
        request.setStudentId(studentId);
        request.setStudentName(studentName);

        // Adicionar item ao pedido
        List<OrderItemRequest> items = new ArrayList<>();
        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(String.valueOf(produtoSelecionado.getId()));
        item.setQuantity(1); // Por enquanto sempre 1 unidade
        items.add(item);
        request.setItems(items);

        // Mostrar loading
        Toast.makeText(this, "Criando pedido...", Toast.LENGTH_SHORT).show();

        // Enviar para o Supabase
        supabaseOrderManager.createOrder(request, userAccessToken, new SupabaseOrderManager.OrderCallback() {
            @Override
            public void onSuccess(Order order) {
                runOnUiThread(() -> {
                    // Reabilitar botão
                    btnConfirmarPedido.setEnabled(true);
                    btnConfirmarPedido.setText("Confirmar Pedido");

                    // Mostrar sucesso
                    String message = "✅ Pedido criado com sucesso!\n\n" +
                            "Código: " + order.getCode() + "\n" +
                            "Item: " + produtoSelecionado.getNome() + "\n" +
                            "Quantidade: 1\n" +
                            "Total: R$ " + String.format(Locale.getDefault(), "%.2f", order.getTotal()) + "\n" +
                            "Status: " + order.getStatus();

                    Toast.makeText(CreateOrderActivitySupabase.this, message, Toast.LENGTH_LONG).show();

                    // Voltar para activity anterior
                    finish();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    // Reabilitar botão
                    btnConfirmarPedido.setEnabled(true);
                    btnConfirmarPedido.setText("Confirmar Pedido");

                    // Mostrar erro
                    String errorMessage = "❌ Erro ao criar pedido:\n" + error;
                    Toast.makeText(CreateOrderActivitySupabase.this, errorMessage, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    // Método alternativo para criar pedido localmente (fallback)
    private void criarPedidoLocal() {
        // Usa o método original do OrderManager como fallback
        OrderRequest request = new OrderRequest();
        request.setStudentId(studentId);
        request.setStudentName(studentName);

        List<OrderItemRequest> items = new ArrayList<>();
        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(String.valueOf(produtoSelecionado.getId()));
        item.setQuantity(1);
        items.add(item);
        request.setItems(items);

        // Processar localmente
        OrderResponse response = OrderManager.createOrder(request);

        if (response.isSuccess()) {
            Order order = response.getOrder();
            String message = "✅ Pedido criado localmente!\n" +
                    "Item: " + produtoSelecionado.getNome() + "\n" +
                    "Código: " + order.getCode() + "\n" +
                    "Total: R$ " + String.format(Locale.getDefault(), "%.2f", order.getTotal());

            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            finish();
        } else {
            Toast.makeText(this, "❌ " + response.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Limpar recursos se necessário
    }
}