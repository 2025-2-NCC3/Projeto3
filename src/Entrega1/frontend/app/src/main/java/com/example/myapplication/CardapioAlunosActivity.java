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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CardapioAlunosActivity extends AppCompatActivity {
    private static final String TAG = "CardapioAlunosActivity";

    Button botaoVoltar, botaoAdmin;
    LinearLayout boxLista;
    private SupabaseClient supabaseClient;
    private List<Produto> produtos;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cardapio);

        // Inicializar componentes
        botaoVoltar = findViewById(R.id.botaoVoltar);
        boxLista = findViewById(R.id.boxLista);
        produtos = new ArrayList<>();
        sessionManager = SessionManager.getInstance(getApplicationContext());

        // Inicializar SupabaseClient
        supabaseClient = SupabaseClient.getInstance(this);

        // Retornar à MainActivity
        botaoVoltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sessionManager.logout();
            }
        });

        // Carregar produtos do banco de dados
        carregarProdutosDoSupabase();
    }

    private void carregarProdutosDoSupabase() {
        // Mostrar mensagem de carregamento
        Toast.makeText(this, "Carregando cardápio...", Toast.LENGTH_SHORT).show();

        supabaseClient.getAllProducts(new SupabaseClient.SupabaseCallback<List<Produto>>() {
            @Override
            public void onSuccess(List<Produto> produtosDoBank) {
                runOnUiThread(() -> {
                    Log.d(TAG, "Produtos carregados do Supabase: " + produtosDoBank.size());

                    // Atualizar lista de produtos
                    produtos.clear();
                    produtos.addAll(produtosDoBank);

                    // Exibir produtos na tela
                    exibirProdutos(produtos);

                    Toast.makeText(CardapioAlunosActivity.this,
                            "Cardápio carregado: " + produtos.size() + " itens",
                            Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Log.e(TAG, "Erro ao carregar produtos: " + error);

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

            // Formatar preço
            precoProduto.setText(String.format(Locale.getDefault(), "R$ %.2f", produto.getPreco()));

            // CORREÇÃO: Carregar imagem do Supabase usando Glide
            String caminhoImagem = produto.getCaminhoImagem();

            if (caminhoImagem != null && !caminhoImagem.isEmpty()) {
                // Verificar se é uma URL completa (começa com http:// ou https://)
                if (caminhoImagem.startsWith("http://") || caminhoImagem.startsWith("https://")) {
                    // É uma URL do Supabase, carregar com Glide
                    Log.d(TAG, "Carregando imagem do Supabase: " + caminhoImagem);

                    Glide.with(this)
                            .load(caminhoImagem)
                            .placeholder(R.drawable.sem_imagem) // Imagem enquanto carrega
                            .error(R.drawable.sem_imagem) // Imagem se der erro
                            .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache para melhor performance
                            .into(imagemProduto);
                } else {
                    // É um nome de arquivo local (sistema antigo), tentar carregar do drawable
                    Log.d(TAG, "Tentando carregar imagem local: " + caminhoImagem);

                    String nomeImagem = caminhoImagem.startsWith("/") ?
                            caminhoImagem.substring(1).replace("/", "_").replace(".", "_") :
                            caminhoImagem.replace("/", "_").replace(".", "_");

                    int imageResId = getResources().getIdentifier(nomeImagem, "drawable", getPackageName());
                    if (imageResId != 0) {
                        imagemProduto.setImageResource(imageResId);
                    } else {
                        imagemProduto.setImageResource(R.drawable.sem_imagem);
                    }
                }
            } else {
                // Produto sem imagem
                Log.d(TAG, "Produto sem imagem: " + produto.getNome());
                imagemProduto.setImageResource(R.drawable.sem_imagem);
            }

            // Alterar aparência se produto sem estoque
            if (!temEstoque) {
                productView.setAlpha(0.6f);
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

    // Metodo para filtrar produtos por categoria
    public void filtrarPorCategoria(int categoria) {
        Toast.makeText(this, "Carregando categoria...", Toast.LENGTH_SHORT).show();

        supabaseClient.getProductsByCategory(categoria, new SupabaseClient.SupabaseCallback<List<Produto>>() {
            @Override
            public void onSuccess(List<Produto> produtosFiltrados) {
                runOnUiThread(() -> {
                    Log.d(TAG, "Produtos filtrados por categoria " + categoria + ": " + produtosFiltrados.size());
                    exibirProdutos(produtosFiltrados);

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
        // Recarregar cardápio quando voltar de outras telas
        Log.d(TAG, "onResume - Recarregando cardápio");
        carregarProdutosDoSupabase();
    }
}