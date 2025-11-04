// com/example/myapplication/AdminListaProdutosActivity.java
package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminListaProdutosActivity extends AppCompatActivity {
    private static final String TAG = "AdminListaProdutos";

    private Button botaoVoltar;
    private EditText editTextBuscar;
    private LinearLayout boxListaProdutos;

    private SupabaseClient supabaseClient;
    private List<Produto> listaProdutosCompleta;
    private List<Produto> listaProdutosFiltrada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_lista_produtos);

        // Inicializar componentes
        botaoVoltar = findViewById(R.id.botaoVoltar);
        editTextBuscar = findViewById(R.id.editTextBuscar);
        boxListaProdutos = findViewById(R.id.boxListaProdutos);

        supabaseClient = SupabaseClient.getInstance(this);
        listaProdutosCompleta = new ArrayList<>();
        listaProdutosFiltrada = new ArrayList<>();

        // Configurar listeners
        botaoVoltar.setOnClickListener(v -> finish());

        editTextBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrarProdutos(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Carregar produtos
        carregarProdutos();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recarregar produtos ao voltar da tela de edição
        carregarProdutos();
    }

    private void carregarProdutos() {
        boxListaProdutos.removeAllViews();

        // Mostrar loading
        TextView loadingText = new TextView(this);
        loadingText.setText("Carregando produtos...");
        loadingText.setTextSize(16);
        loadingText.setPadding(16, 32, 16, 32);
        loadingText.setGravity(android.view.Gravity.CENTER);
        boxListaProdutos.addView(loadingText);

        supabaseClient.getAllProducts(new SupabaseClient.SupabaseCallback<List<Produto>>() {
            @Override
            public void onSuccess(List<Produto> produtos) {
                runOnUiThread(() -> {
                    boxListaProdutos.removeAllViews();

                    if (produtos == null || produtos.isEmpty()) {
                        TextView emptyText = new TextView(AdminListaProdutosActivity.this);
                        emptyText.setText("Nenhum produto cadastrado");
                        emptyText.setTextSize(16);
                        emptyText.setPadding(16, 32, 16, 32);
                        emptyText.setGravity(android.view.Gravity.CENTER);
                        boxListaProdutos.addView(emptyText);
                        return;
                    }

                    listaProdutosCompleta = produtos;
                    listaProdutosFiltrada = new ArrayList<>(produtos);
                    exibirProdutos(listaProdutosFiltrada);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    boxListaProdutos.removeAllViews();

                    TextView errorText = new TextView(AdminListaProdutosActivity.this);
                    errorText.setText("Erro ao carregar produtos:\n" + error);
                    errorText.setTextSize(14);
                    errorText.setPadding(16, 32, 16, 32);
                    errorText.setGravity(android.view.Gravity.CENTER);
                    boxListaProdutos.addView(errorText);

                    Toast.makeText(AdminListaProdutosActivity.this,
                            "Erro ao carregar produtos: " + error,
                            Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Erro ao carregar produtos: " + error);
                });
            }
        });
    }

    private void exibirProdutos(List<Produto> produtos) {
        boxListaProdutos.removeAllViews();

        for (Produto produto : produtos) {
            View cardView = criarCardProduto(produto);
            boxListaProdutos.addView(cardView);
        }
    }

    private View criarCardProduto(Produto produto) {
        // Inflar o layout do card
        View cardView = LayoutInflater.from(this)
                .inflate(R.layout.item_produto_admin, boxListaProdutos, false);

        // Obter referências dos componentes
        ImageView imagemProduto = cardView.findViewById(R.id.imagemProduto);
        TextView nomeProduto = cardView.findViewById(R.id.nomeProduto);
        TextView precoProduto = cardView.findViewById(R.id.precoProduto);
        TextView estoqueProduto = cardView.findViewById(R.id.estoqueProduto);
        TextView categoriaProduto = cardView.findViewById(R.id.categoriaProduto);
        Button btnEditar = cardView.findViewById(R.id.btnEditar);
        CardView card = cardView.findViewById(R.id.cardProduto);

        // Preencher dados
        nomeProduto.setText(produto.getNome());
        precoProduto.setText(String.format(Locale.getDefault(), "R$ %.2f", produto.getPreco()));
        estoqueProduto.setText("Estoque: " + produto.getEstoque());
        categoriaProduto.setText(getCategoriaTexto(produto.getCategoria()));

        // Carregar imagem
        String caminhoImagem = produto.getCaminhoImagem();
        if (caminhoImagem != null && !caminhoImagem.isEmpty()) {
            Glide.with(this)
                    .load(caminhoImagem)
                    .placeholder(R.drawable.sem_imagem)
                    .error(R.drawable.sem_imagem)
                    .into(imagemProduto);
        } else {
            imagemProduto.setImageResource(R.drawable.sem_imagem);
        }

        // Listeners
        btnEditar.setOnClickListener(v -> abrirEdicaoProduto(produto));

        card.setOnClickListener(v -> abrirEdicaoProduto(produto));

        return cardView;
    }

    private void abrirEdicaoProduto(Produto produto) {
        Intent intent = new Intent(this, AdminEditarProdutoActivity.class);
        intent.putExtra("produtoInfo", produto);
        startActivity(intent);
    }

    private void filtrarProdutos(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            listaProdutosFiltrada = new ArrayList<>(listaProdutosCompleta);
        } else {
            listaProdutosFiltrada = new ArrayList<>();
            String textoBusca = texto.toLowerCase().trim();

            for (Produto produto : listaProdutosCompleta) {
                if (produto.getNome().toLowerCase().contains(textoBusca) ||
                        (produto.getDescricao() != null &&
                                produto.getDescricao().toLowerCase().contains(textoBusca))) {
                    listaProdutosFiltrada.add(produto);
                }
            }
        }

        exibirProdutos(listaProdutosFiltrada);
    }

    private String getCategoriaTexto(int categoria) {
        switch (categoria) {
            case 1: return "Bebidas";
            case 2: return "Pratos Principais";
            case 3: return "Sobremesas";
            case 4: return "Entradas";
            case 5: return "Lanches";
            default: return "Outros";
        }
    }
}