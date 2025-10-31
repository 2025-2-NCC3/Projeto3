package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class CardapioAlunosActivity extends AppCompatActivity {
    private static final String TAG = "CardapioAlunosActivity";

    private ImageButton botaoVoltar;
    private EditText searchInput;
    private MaterialButton btnTodos, btnLanches, btnBebidas, btnDoces, btnMarmitas;
    private Button btnCarrinho, btnMeusPedidos;
    private RecyclerView recyclerViewProdutos;

    private SupabaseClient supabaseClient;
    private List<Produto> produtos;
    private List<Produto> produtosFiltrados;
    private SessionManager sessionManager;
    private CarrinhoHelper carrinhoHelper;
    private ProdutoAdapter produtoAdapter;

    private int categoriaAtual = -1; // -1 = todas as categorias
    private String buscaAtual = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cardapio);

        // Inicializar componentes
        inicializarComponentes();

        // Inicializar listas
        produtos = new ArrayList<>();
        produtosFiltrados = new ArrayList<>();

        // Inicializar managers
        sessionManager = SessionManager.getInstance(getApplicationContext());
        supabaseClient = SupabaseClient.getInstance(this);
        carrinhoHelper = CarrinhoHelper.getInstance(this);

        // Configurar RecyclerView
        configurarRecyclerView();

        // Configurar listeners
        configurarBotoes();
        configurarBusca();

        // Carregar produtos do banco de dados
        carregarProdutosDoSupabase();

        // Atualizar badge do carrinho
        atualizarBadgeCarrinho();
    }

    private void inicializarComponentes() {
        botaoVoltar = findViewById(R.id.botaoVoltar);
        searchInput = findViewById(R.id.searchInput);
        btnTodos = findViewById(R.id.btnTodos);
        btnLanches = findViewById(R.id.btnLanches);
        btnBebidas = findViewById(R.id.btnBebidas);
        btnDoces = findViewById(R.id.btnDoces);
        btnMarmitas = findViewById(R.id.btnMarmitas);
        btnCarrinho = findViewById(R.id.btnCarrinho);
        btnMeusPedidos = findViewById(R.id.btnMeusPedidos);
        recyclerViewProdutos = findViewById(R.id.recyclerViewProdutos);
    }

    private void configurarRecyclerView() {
        // Configurar GridLayoutManager com 2 colunas
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerViewProdutos.setLayoutManager(layoutManager);

        // Criar e configurar adapter
        produtoAdapter = new ProdutoAdapter(this, produtosFiltrados, new ProdutoAdapter.OnProdutoClickListener() {
            @Override
            public void onProdutoClick(Produto produto) {
                if (produto.getEstoque() > 0) {
                    Intent intent = new Intent(CardapioAlunosActivity.this, InfoProdutoActivity.class);
                    intent.putExtra("produtoInfo", produto);
                    startActivity(intent);
                } else {
                    Toast.makeText(CardapioAlunosActivity.this,
                            "Produto indisponível no momento",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        recyclerViewProdutos.setAdapter(produtoAdapter);


    }

    private void adicionarAoCarrinho(Produto produto) {
        if (produto.getEstoque() > 0) {
            // Verificar se já tem no limite do estoque
            int quantidadeNoCarrinho = carrinhoHelper.getQuantidadeProduto(produto.getId());
            if (quantidadeNoCarrinho >= produto.getEstoque()) {
                Toast.makeText(this,
                        "❌ Estoque máximo atingido (" + produto.getEstoque() + " un.)",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            carrinhoHelper.adicionarItem(produto);

            int novaQuantidade = carrinhoHelper.getQuantidadeProduto(produto.getId());
            Toast.makeText(this,
                    "✅ " + produto.getNome() + " adicionado (" + novaQuantidade + ")",
                    Toast.LENGTH_SHORT).show();
            atualizarBadgeCarrinho();
        } else {
            Toast.makeText(this, "❌ Produto indisponível", Toast.LENGTH_SHORT).show();
        }
    }
    private void configurarBotoes() {
        // Botão Voltar - Faz logout
        botaoVoltar.setOnClickListener(v -> {
            sessionManager.logout();
            Log.d(TAG, "Logout realizado, voltando para MainActivity");

            Intent intent = new Intent(CardapioAlunosActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Botão Carrinho
        btnCarrinho.setOnClickListener(v -> {
            Intent intent = new Intent(CardapioAlunosActivity.this, CarrinhoActivity.class);
            startActivity(intent);
        });

        // Botão Meus Pedidos
        btnMeusPedidos.setOnClickListener(v -> {
            Intent intent = new Intent(CardapioAlunosActivity.this, MeusPedidosActivity.class);
            startActivity(intent);
        });

        // Botão Todos
        btnTodos.setOnClickListener(v -> {
            categoriaAtual = -1;
            aplicarFiltros();
            atualizarEstiloBotoes(btnTodos);
            Toast.makeText(this, "Mostrando todos os produtos", Toast.LENGTH_SHORT).show();
        });

        // Botões de Categoria
        btnLanches.setOnClickListener(v -> {
            filtrarPorCategoria(5, "Lanches");
            atualizarEstiloBotoes(btnLanches);
        });

        btnBebidas.setOnClickListener(v -> {
            filtrarPorCategoria(1, "Bebidas");
            atualizarEstiloBotoes(btnBebidas);
        });

        btnDoces.setOnClickListener(v -> {
            filtrarPorCategoria(3, "Doces");
            atualizarEstiloBotoes(btnDoces);
        });

        btnMarmitas.setOnClickListener(v -> {
            filtrarPorCategoria(2, "Marmitas");
            atualizarEstiloBotoes(btnMarmitas);
        });
    }

    private void configurarBusca() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                buscaAtual = s.toString();
                aplicarFiltros();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void atualizarEstiloBotoes(MaterialButton botaoSelecionado) {
        // Resetar todos os botões
        resetarTodosBotoes();

        // Aplicar estilo filled no botão selecionado
        selecionarBotao(botaoSelecionado);
    }

    private void resetarTodosBotoes() {
        resetarBotao(btnTodos);
        resetarBotao(btnLanches);
        resetarBotao(btnBebidas);
        resetarBotao(btnDoces);
        resetarBotao(btnMarmitas);
    }

    private void resetarBotao(MaterialButton botao) {
        // Estilo outlined (não selecionado)
        botao.setBackgroundTintList(getColorStateList(android.R.color.transparent));
        botao.setTextColor(getResources().getColor(R.color.brown));
        botao.setStrokeColorResource(R.color.brown);
        botao.setStrokeWidth(4);
    }

    private void selecionarBotao(MaterialButton botao) {
        // Estilo filled (selecionado)
        botao.setBackgroundTintList(getColorStateList(R.color.brown));
        botao.setTextColor(getResources().getColor(android.R.color.white));
        botao.setStrokeWidth(0);
    }

    private void atualizarBadgeCarrinho() {
        int quantidadeItens = carrinhoHelper.getQuantidadeTotal();

        if (quantidadeItens > 0) {
            btnCarrinho.setText("🛒 " + quantidadeItens);
        } else {
            btnCarrinho.setText("🛒");
        }
    }

    private void carregarProdutosDoSupabase() {
        Toast.makeText(this, "Carregando cardápio...", Toast.LENGTH_SHORT).show();

        supabaseClient.getAllProducts(new SupabaseClient.SupabaseCallback<List<Produto>>() {
            @Override
            public void onSuccess(List<Produto> produtosDoBank) {
                runOnUiThread(() -> {
                    Log.d(TAG, "Produtos carregados do Supabase: " + produtosDoBank.size());

                    produtos.clear();
                    produtos.addAll(produtosDoBank);

                    aplicarFiltros();

                    Toast.makeText(CardapioAlunosActivity.this,
                            "Cardápio carregado: " + produtos.size() + " itens",
                            Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Log.e(TAG, "Erro ao carregar produtos: " + error);
                    Toast.makeText(CardapioAlunosActivity.this,
                            "Erro ao carregar cardápio: " + error,
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void filtrarPorCategoria(int categoria, String nomeCategoria) {
        categoriaAtual = categoria;
        aplicarFiltros();

        // Mostrar toast com a categoria selecionada
        Toast.makeText(this, nomeCategoria + ": " + produtosFiltrados.size() + " itens",
                Toast.LENGTH_SHORT).show();
    }

    private void aplicarFiltros() {
        produtosFiltrados.clear();

        for (Produto produto : produtos) {
            boolean passaCategoria = (categoriaAtual == -1 || produto.getCategoria() == categoriaAtual);
            boolean passaBusca = buscaAtual.isEmpty() ||
                    produto.getNome().toLowerCase().contains(buscaAtual.toLowerCase());

            if (passaCategoria && passaBusca) {
                produtosFiltrados.add(produto);
            }
        }

        produtoAdapter.notifyDataSetChanged();

        Log.d(TAG, "Filtros aplicados - Categoria: " + categoriaAtual +
                ", Busca: '" + buscaAtual + "', Resultados: " + produtosFiltrados.size());
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume - Recarregando cardápio");
        carregarProdutosDoSupabase();
        atualizarBadgeCarrinho(); // Atualizar badge ao voltar
    }
}