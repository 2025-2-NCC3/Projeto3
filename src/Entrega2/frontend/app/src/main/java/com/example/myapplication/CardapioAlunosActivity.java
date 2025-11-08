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

        try {
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

        // Carregar produtos do banco de dados
        carregarProdutosDoSupabase();
        // Carregar a navbar ebaa
        NavbarHelper.setupNavbar(this, "cardapio");
            // Atualizar badge do carrinho
            atualizarBadgeCarrinho();

        } catch (Exception e) {
            Log.e(TAG, "Erro no onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Erro ao inicializar: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
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
        try {
            // Configurar GridLayoutManager com 2 colunas
            GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
            recyclerViewProdutos.setLayoutManager(layoutManager);

            // Criar e configurar adapter
            produtoAdapter = new ProdutoAdapter(this, produtosFiltrados, new ProdutoAdapter.OnProdutoClickListener() {
                @Override
                public void onProdutoClick(Produto produto) {
                    try {
                        if (produto != null && produto.getEstoque() > 0) {
                            Intent intent = new Intent(CardapioAlunosActivity.this, InfoProdutoActivity.class);
                            intent.putExtra("produtoInfo", produto);
                            startActivity(intent);
                        } else {
                            Toast.makeText(CardapioAlunosActivity.this,
                                    "Produto indisponível no momento",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Erro ao abrir produto: " + e.getMessage(), e);
                        Toast.makeText(CardapioAlunosActivity.this,
                                "Erro ao abrir produto", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            recyclerViewProdutos.setAdapter(produtoAdapter);

        } catch (Exception e) {
            Log.e(TAG, "Erro ao configurar RecyclerView: " + e.getMessage(), e);
        }
    }

    private void adicionarAoCarrinho(Produto produto) {
        try {
            if (produto == null) {
                Toast.makeText(this, "❌ Produto inválido", Toast.LENGTH_SHORT).show();
                return;
            }

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
        } catch (Exception e) {
            Log.e(TAG, "Erro ao adicionar ao carrinho: " + e.getMessage(), e);
            Toast.makeText(this, "Erro ao adicionar produto", Toast.LENGTH_SHORT).show();
        }
    }

    private void configurarBotoes() {
        // Botão Voltar - Faz logout
        botaoVoltar.setOnClickListener(v -> {
            try {
                sessionManager.logout();
                Log.d(TAG, "Logout realizado, voltando para MainActivity");

                Intent intent = new Intent(CardapioAlunosActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } catch (Exception e) {
                Log.e(TAG, "Erro ao fazer logout: " + e.getMessage(), e);
                finish();
            }
        });

        // Botão Carrinho
        btnCarrinho.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(CardapioAlunosActivity.this, CarrinhoActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Erro ao abrir carrinho: " + e.getMessage(), e);
                Toast.makeText(this, "Erro ao abrir carrinho", Toast.LENGTH_SHORT).show();
            }
        });

        // Botão Meus Pedidos
        btnMeusPedidos.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(CardapioAlunosActivity.this, MeusPedidosActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Erro ao abrir pedidos: " + e.getMessage(), e);
                Toast.makeText(this, "Erro ao abrir pedidos", Toast.LENGTH_SHORT).show();
            }
        });

        // Botão Todos
        btnTodos.setOnClickListener(v -> {
            try {
                categoriaAtual = -1;
                aplicarFiltros();
                atualizarEstiloBotoes(btnTodos);
                Toast.makeText(this, "Mostrando todos os produtos", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e(TAG, "Erro ao filtrar todos: " + e.getMessage(), e);
            }
        });

        // Botões de Categoria
        btnLanches.setOnClickListener(v -> {
            try {
                filtrarPorCategoria(5, "Lanches");
                atualizarEstiloBotoes(btnLanches);
            } catch (Exception e) {
                Log.e(TAG, "Erro ao filtrar lanches: " + e.getMessage(), e);
            }
        });

        btnBebidas.setOnClickListener(v -> {
            try {
                filtrarPorCategoria(1, "Bebidas");
                atualizarEstiloBotoes(btnBebidas);
            } catch (Exception e) {
                Log.e(TAG, "Erro ao filtrar bebidas: " + e.getMessage(), e);
            }
        });

        btnDoces.setOnClickListener(v -> {
            try {
                filtrarPorCategoria(3, "Doces");
                atualizarEstiloBotoes(btnDoces);
            } catch (Exception e) {
                Log.e(TAG, "Erro ao filtrar doces: " + e.getMessage(), e);
            }
        });

        btnMarmitas.setOnClickListener(v -> {
            try {
                filtrarPorCategoria(2, "Marmitas");
                atualizarEstiloBotoes(btnMarmitas);
            } catch (Exception e) {
                Log.e(TAG, "Erro ao filtrar marmitas: " + e.getMessage(), e);
            }
        });
    }

    private void configurarBusca() {
        try {
            searchInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    try {
                        buscaAtual = s.toString();
                        aplicarFiltros();
                    } catch (Exception e) {
                        Log.e(TAG, "Erro ao buscar: " + e.getMessage(), e);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        } catch (Exception e) {
            Log.e(TAG, "Erro ao configurar busca: " + e.getMessage(), e);
        }
    }

    private void atualizarEstiloBotoes(MaterialButton botaoSelecionado) {
        try {
            // Resetar todos os botões
            resetarTodosBotoes();

            // Aplicar estilo filled no botão selecionado
            selecionarBotao(botaoSelecionado);
        } catch (Exception e) {
            Log.e(TAG, "Erro ao atualizar estilo dos botões: " + e.getMessage(), e);
        }
    }

    private void resetarTodosBotoes() {
        resetarBotao(btnTodos);
        resetarBotao(btnLanches);
        resetarBotao(btnBebidas);
        resetarBotao(btnDoces);
        resetarBotao(btnMarmitas);
    }

    private void resetarBotao(MaterialButton botao) {
        try {
            if (botao != null) {
                // Estilo outlined (não selecionado)
                botao.setBackgroundTintList(getColorStateList(android.R.color.transparent));
                botao.setTextColor(getResources().getColor(R.color.brown));
                botao.setStrokeColorResource(R.color.brown);
                botao.setStrokeWidth(4);
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao resetar botão: " + e.getMessage(), e);
        }
    }

    private void selecionarBotao(MaterialButton botao) {
        try {
            if (botao != null) {
                // Estilo filled (selecionado)
                botao.setBackgroundTintList(getColorStateList(R.color.brown));
                botao.setTextColor(getResources().getColor(android.R.color.white));
                botao.setStrokeWidth(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao selecionar botão: " + e.getMessage(), e);
        }
    }

    private void atualizarBadgeCarrinho() {
        try {
            int quantidadeItens = carrinhoHelper.getQuantidadeTotal();

            if (quantidadeItens > 0) {
                btnCarrinho.setText("🛒 " + quantidadeItens);
            } else {
                btnCarrinho.setText("🛒");
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao atualizar badge: " + e.getMessage(), e);
            btnCarrinho.setText("🛒");
        }
    }

    private void carregarProdutosDoSupabase() {


        supabaseClient.getAllProducts(new SupabaseClient.SupabaseCallback<List<Produto>>() {
            @Override
            public void onSuccess(List<Produto> produtosDoBank) {
                runOnUiThread(() -> {
                    try {
                        Log.d(TAG, "Produtos carregados do Supabase: " + produtosDoBank.size());

                        produtos.clear();
                        produtos.addAll(produtosDoBank);

                        aplicarFiltros();


                    } catch (Exception e) {
                        Log.e(TAG, "Erro ao processar produtos: " + e.getMessage(), e);
                        Toast.makeText(CardapioAlunosActivity.this,
                                "Erro ao carregar produtos", Toast.LENGTH_SHORT).show();
                    }
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
        try {
            categoriaAtual = categoria;
            aplicarFiltros();

            // Mostrar toast com a categoria selecionada
            Toast.makeText(this, nomeCategoria + ": " + produtosFiltrados.size() + " itens",
                    Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Erro ao filtrar categoria: " + e.getMessage(), e);
        }
    }

    private void aplicarFiltros() {
        try {
            produtosFiltrados.clear();

            for (Produto produto : produtos) {
                if (produto == null) continue;

                boolean passaCategoria = (categoriaAtual == -1 || produto.getCategoria() == categoriaAtual);
                boolean passaBusca = buscaAtual.isEmpty() ||
                        (produto.getNome() != null && produto.getNome().toLowerCase().contains(buscaAtual.toLowerCase()));

                if (passaCategoria && passaBusca) {
                    produtosFiltrados.add(produto);
                }
            }

            if (produtoAdapter != null) {
                produtoAdapter.notifyDataSetChanged();
            }

            Log.d(TAG, "Filtros aplicados - Categoria: " + categoriaAtual +
                    ", Busca: '" + buscaAtual + "', Resultados: " + produtosFiltrados.size());

        } catch (Exception e) {
            Log.e(TAG, "Erro ao aplicar filtros: " + e.getMessage(), e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            Log.d(TAG, "onResume - Recarregando cardápio");
            carregarProdutosDoSupabase();
            atualizarBadgeCarrinho(); // Atualizar badge ao voltar
        } catch (Exception e) {
            Log.e(TAG, "Erro no onResume: " + e.getMessage(), e);
        }
    }
}