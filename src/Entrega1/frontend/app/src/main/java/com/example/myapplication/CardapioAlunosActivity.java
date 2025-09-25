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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CardapioAlunosActivity extends AppCompatActivity {
    private static final String TAG = "CardapioAlunosActivity";

    Button botaoVoltar, botaoAdmin;
    LinearLayout boxLista;
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