package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
<<<<<<< HEAD
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.button.MaterialButton;

=======
>>>>>>> 1943653 ([ERIC] Feito ajustes nas pastas e realizado entregas de PI e POO)
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CardapioAlunosActivity extends AppCompatActivity {
    private static final String TAG = "CardapioAlunosActivity";

<<<<<<< HEAD
    private ImageButton botaoVoltar;
    private ImageButton btnLimparBusca;
    private EditText searchInput;
    private MaterialButton btnTodos, btnLanches, btnBebidas, btnDoces, btnMarmitas;
    private Button btnCarrinho, btnMeusPedidos;
    private RecyclerView recyclerViewProdutos;
    private SwipeRefreshLayout swipeRefreshLayout;

=======
    Button botaoVoltar, botaoAdmin;
    LinearLayout boxLista;
>>>>>>> 1943653 ([ERIC] Feito ajustes nas pastas e realizado entregas de PI e POO)
    private SupabaseClient supabaseClient;
    private List<Produto> produtos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cardapio);

        // Inicializar componentes
        botaoVoltar = findViewById(R.id.botaoVoltar);
        botaoAdmin = findViewById(R.id.botaoAdmin);
        boxLista = findViewById(R.id.boxLista);
        produtos = new ArrayList<>();

        // Inicializar SupabaseClient
        supabaseClient = SupabaseClient.getInstance(this);

        // Retornar à MainActivity
        botaoVoltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CardapioAlunosActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

<<<<<<< HEAD
            // Configurar RecyclerView
            configurarRecyclerView();

            // Configurar SwipeRefresh
            configurarSwipeRefresh();

            // Configurar listeners
            configurarBotoes();
            configurarBusca();

            // Carregar a navbar
            NavbarHelper.setupNavbar(this, "cardapio");

            // Carregar produtos do banco de dados
            carregarProdutosDoSupabase();

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
        btnLimparBusca = findViewById(R.id.btnLimparBusca);
        btnTodos = findViewById(R.id.btnTodos);
        btnLanches = findViewById(R.id.btnLanches);
        btnBebidas = findViewById(R.id.btnBebidas);
        btnDoces = findViewById(R.id.btnDoces);
        btnMarmitas = findViewById(R.id.btnMarmitas);
        btnCarrinho = findViewById(R.id.btnCarrinho);
        btnMeusPedidos = findViewById(R.id.btnMeusPedidos);
        recyclerViewProdutos = findViewById(R.id.recyclerViewProdutos);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
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

    private void configurarSwipeRefresh() {
        try {
            if (swipeRefreshLayout != null) {
                // Definir as cores do círculo de loading
                swipeRefreshLayout.setColorSchemeResources(
                        R.color.brown,
                        R.color.dark_green,
                        R.color.brown
                );

                // Definir cor de fundo durante o refresh
                swipeRefreshLayout.setProgressBackgroundColorSchemeResource(android.R.color.white);

                // Configurar o listener
                swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        Log.d(TAG, "SwipeRefresh acionado - Recarregando produtos");
                        carregarProdutosDoSupabase();
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao configurar SwipeRefresh: " + e.getMessage(), e);
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

                        // Mostrar/ocultar botão de limpar busca
                        if (btnLimparBusca != null) {
                            btnLimparBusca.setVisibility(
                                    buscaAtual.isEmpty() ? View.GONE : View.VISIBLE
                            );
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Erro ao buscar: " + e.getMessage(), e);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            // Configurar clique no botão de limpar
            if (btnLimparBusca != null) {
                btnLimparBusca.setOnClickListener(v -> {
                    try {
                        searchInput.setText("");
                        searchInput.clearFocus();
                    } catch (Exception e) {
                        Log.e(TAG, "Erro ao limpar busca: " + e.getMessage(), e);
                    }
                });
            }

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
        // Mostrar indicador de loading apenas se não for um refresh
        if (swipeRefreshLayout != null && !swipeRefreshLayout.isRefreshing()) {
            Toast.makeText(this, "Carregando cardápio...", Toast.LENGTH_SHORT).show();
        }
=======
        // Ir para página de administração do cardápio
        botaoAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CardapioAlunosActivity.this, AdminCardapioActivity.class);
                startActivity(intent);
            }
        });

        // Carregar produtos do banco de dados
        carregarProdutosDoSupabase();
    }

    private void carregarProdutosDoSupabase() {
        // Mostrar mensagem de carregamento
        Toast.makeText(this, "Carregando cardápio...", Toast.LENGTH_SHORT).show();
>>>>>>> 1943653 ([ERIC] Feito ajustes nas pastas e realizado entregas de PI e POO)

        supabaseClient.getActiveProducts(new SupabaseClient.SupabaseCallback<List<Produto>>() {
            @Override
            public void onSuccess(List<Produto> produtosDoBank) {
                runOnUiThread(() -> {
                    Log.d(TAG, "Produtos carregados do Supabase: " + produtosDoBank.size());

                    // Atualizar lista de produtos
                    produtos.clear();
                    produtos.addAll(produtosDoBank);

                    // Exibir produtos na tela
                    exibirProdutos(produtos);

<<<<<<< HEAD
                        // Parar o refresh se estiver ativo
                        if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
                            swipeRefreshLayout.setRefreshing(false);
                            Toast.makeText(CardapioAlunosActivity.this,
                                    "✅ Cardápio atualizado",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(CardapioAlunosActivity.this,
                                    "Cardápio carregado: " + produtos.size() + " itens",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Erro ao processar produtos: " + e.getMessage(), e);
                        if (swipeRefreshLayout != null) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                        Toast.makeText(CardapioAlunosActivity.this,
                                "Erro ao carregar produtos", Toast.LENGTH_SHORT).show();
                    }
=======
                    Toast.makeText(CardapioAlunosActivity.this,
                            "Cardápio carregado: " + produtos.size() + " itens",
                            Toast.LENGTH_SHORT).show();
>>>>>>> 1943653 ([ERIC] Feito ajustes nas pastas e realizado entregas de PI e POO)
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Log.e(TAG, "Erro ao carregar produtos: " + error);

<<<<<<< HEAD
                    // Parar o refresh se estiver ativo
                    if (swipeRefreshLayout != null) {
                        swipeRefreshLayout.setRefreshing(false);
                    }

                    Toast.makeText(CardapioAlunosActivity.this,
                            "❌ Erro ao carregar cardápio",
                            Toast.LENGTH_SHORT).show();
=======
                    // Em caso de erro, usar produtos de exemplo
                    Toast.makeText(CardapioAlunosActivity.this,
                            "Erro ao carregar do servidor. Usando dados locais.",
                            Toast.LENGTH_LONG).show();

                    // Carregar produtos de exemplo como fallback
                    carregarProdutosExemplo();
                });
            }
        });
    }

    private void carregarProdutosExemplo() {
        // Produtos de exemplo caso não consiga conectar com o Supabase
        produtos.clear();
        produtos.add(new Produto(1, "Coxinha", "Coxinha recheada de frango.", "coxinha_exemplo", 1.99, 10, 1));
        produtos.add(new Produto(2, "Croissant", "Croissant de presunto e queijo.", "croissant_exemplo", 2.99, 12, 1));
        produtos.add(new Produto(3, "Brownie", "Brownie de chocolate.", "brownie_exemplo", 2.49, 20, 2));

        exibirProdutos(produtos);
    }

    private void exibirProdutos(List<Produto> produtosParaExibir) {
        // Limpar produtos anteriores
        boxLista.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(this);

        for (Produto produto : produtosParaExibir) {
            // Verificar se o produto tem estoque
            boolean temEstoque = produto.getEstoque() > 0;

            if (!temEstoque) {
                Log.d(TAG, "Produto " + produto.getNome() + " está sem estoque: " + produto.getEstoque());
                // Continue exibindo produtos sem estoque, mas com visual diferente
            }

            // Cria a visualização do produto que será adicionado no layout
            View productView = inflater.inflate(R.layout.produto, boxLista, false);

            // Pega referencias para cada elemento
            LinearLayout boxProduto = productView.findViewById(R.id.boxProduto);
            ImageView imagemProduto = productView.findViewById(R.id.imagemProduto);
            TextView tituloProduto = productView.findViewById(R.id.tituloProduto);
            TextView precoProduto = productView.findViewById(R.id.precoProduto);

            // Altera a informação de cada elemento
            tituloProduto.setText(produto.getNome());

            // Formatar preço manualmente
            precoProduto.setText(String.format(Locale.getDefault(), "R$ %.2f", produto.getPreco()));

            // Altera a imagem do produto
            String caminhoImagem = produto.getCaminhoImagem();
            if (caminhoImagem != null && !caminhoImagem.isEmpty()) {
                // Remove possível "/" do início para buscar no drawable
                String nomeImagem = caminhoImagem.startsWith("/") ?
                        caminhoImagem.substring(1).replace("/", "_").replace(".", "_") :
                        caminhoImagem.replace("/", "_").replace(".", "_");

                int imageResId = getResources().getIdentifier(nomeImagem, "drawable", getPackageName());
                if (imageResId != 0) {
                    imagemProduto.setImageResource(imageResId);
                } else {
                    // Usa um placeholder caso o produto não tenha imagem
                    imagemProduto.setImageResource(R.drawable.sem_imagem);
                }
            } else {
                imagemProduto.setImageResource(R.drawable.sem_imagem);
            }

            // Alterar aparência se produto sem estoque
            if (!temEstoque) {
                // Deixar o produto com aparência "desabilitada"
                productView.setAlpha(0.6f);

                // Adicionar texto de indisponível se houver um TextView adicional
                // (isso é opcional, só funciona se você tiver um TextView extra no layout produto.xml)
            }

            // Adiciona a visualização configurada no activity_cardapio
            boxLista.addView(productView);

            // Adiciona função para abrir a página de informações ao clicar no produto
            boxProduto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (temEstoque) {
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
        }

        Log.d(TAG, "Exibidos " + produtosParaExibir.size() + " produtos na tela");
    }

    // Método para filtrar produtos por categoria (pode ser chamado por botões)
    public void filtrarPorCategoria(int categoria) {
        Toast.makeText(this, "Carregando categoria...", Toast.LENGTH_SHORT).show();

        supabaseClient.getProductsByCategory(categoria, new SupabaseClient.SupabaseCallback<List<Produto>>() {
            @Override
            public void onSuccess(List<Produto> produtosFiltrados) {
                runOnUiThread(() -> {
                    Log.d(TAG, "Produtos filtrados por categoria " + categoria + ": " + produtosFiltrados.size());
                    exibirProdutos(produtosFiltrados);

                    // Mapear categoria para texto
                    String categoriaTexto = "";
                    switch (categoria) {
                        case 1: categoriaTexto = "Bebidas"; break;
                        case 2: categoriaTexto = "Pratos Principais"; break;
                        case 3: categoriaTexto = "Sobremesas"; break;
                        case 4: categoriaTexto = "Entradas"; break;
                        case 5: categoriaTexto = "Lanches"; break;
                        default: categoriaTexto = "Categoria " + categoria; break;
                    }

                    Toast.makeText(CardapioAlunosActivity.this,
                            categoriaTexto + ": " + produtosFiltrados.size() + " itens",
                            Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Log.e(TAG, "Erro ao filtrar por categoria: " + error);
                    Toast.makeText(CardapioAlunosActivity.this,
                            "Erro ao carregar categoria: " + error,
                            Toast.LENGTH_LONG).show();
>>>>>>> 1943653 ([ERIC] Feito ajustes nas pastas e realizado entregas de PI e POO)
                });
            }
        });
    }

    // Método para recarregar todos os produtos
    public void recarregarCardapio() {
        carregarProdutosDoSupabase();
    }

    // Métodos que podem ser chamados por botões no layout para filtrar categorias
    public void mostrarBebidas(View view) {
        filtrarPorCategoria(1);
    }

    public void mostrarPratosPrincipais(View view) {
        filtrarPorCategoria(2);
    }

    public void mostrarSobremesas(View view) {
        filtrarPorCategoria(3);
    }

    public void mostrarEntradas(View view) {
        filtrarPorCategoria(4);
    }

    public void mostrarLanches(View view) {
        filtrarPorCategoria(5);
    }

    public void mostrarTudo(View view) {
        recarregarCardapio();
    }

    // Método adicional para forçar atualização após adicionar produtos
    @Override
    protected void onResume() {
        super.onResume();
<<<<<<< HEAD
        try {
            Log.d(TAG, "onResume - Recarregando cardápio");
            carregarProdutosDoSupabase();
            atualizarBadgeCarrinho();
        } catch (Exception e) {
            Log.e(TAG, "Erro no onResume: " + e.getMessage(), e);
        }
=======
        // Recarregar cardápio quando voltar de outras telas
        Log.d(TAG, "onResume - Recarregando cardápio");
        carregarProdutosDoSupabase();
>>>>>>> 1943653 ([ERIC] Feito ajustes nas pastas e realizado entregas de PI e POO)
    }
}