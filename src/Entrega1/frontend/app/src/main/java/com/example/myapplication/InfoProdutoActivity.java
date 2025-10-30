package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.Locale;

public class InfoProdutoActivity extends AppCompatActivity {
    private static final String TAG = "InfoProdutoActivity";

    private ImageView imagemProduto;
    private TextView nomeProduto, descricaoProduto, precoProduto;
    private Button botaoVoltar, botaoComprar;
    private Produto produto;
    private CarrinhoHelper carrinhoHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_produto);

        carrinhoHelper = CarrinhoHelper.getInstance(this);
        inicializarComponentes();

        // Receber o intent com as informações do produto clicado
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            produto = bundle.getSerializable("produtoInfo", Produto.class);

            if (produto != null) {
                Log.d(TAG, "Produto recebido: " + produto.getNome());
                carregarInformacoesProduto(produto);
            } else {
                Toast.makeText(this, "Erro ao carregar produto", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(this, "Nenhum produto selecionado", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Configurar botão voltar
        if (botaoVoltar != null) {
            botaoVoltar.setOnClickListener(v -> finish());
        }

        // Configurar botão comprar
        if (botaoComprar != null) {
            botaoComprar.setOnClickListener(v -> {
                if (produto != null && produto.getEstoque() > 0) {
                    // Adiciona ao carrinho
                    carrinhoHelper.adicionarProduto(produto, 1);
                    Toast.makeText(InfoProdutoActivity.this,
                            produto.getNome() + " adicionado ao carrinho!",
                            Toast.LENGTH_SHORT).show();

                    // Vai para a tela do carrinho
                    Intent intent = new Intent(InfoProdutoActivity.this, CarrinhoActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(InfoProdutoActivity.this,
                            "Produto indisponível no momento",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void inicializarComponentes() {
        imagemProduto = findViewById(R.id.imagemProduto);
        nomeProduto = findViewById(R.id.nomeProduto);
        descricaoProduto = findViewById(R.id.descricaoProduto);
        precoProduto = findViewById(R.id.precoProduto);
        botaoVoltar = findViewById(R.id.botaoVoltar);
        botaoComprar = findViewById(R.id.botaoComprar);

        Log.d(TAG, "Componentes inicializados");
    }

    private void carregarInformacoesProduto(Produto produto) {
        // Definir nome do produto
        if (nomeProduto != null) {
            nomeProduto.setText(produto.getNome());
        }

        // Definir descrição
        if (descricaoProduto != null) {
            descricaoProduto.setText(produto.getDescricao());
        }

        // Definir preço
        if (precoProduto != null) {
            precoProduto.setText(String.format(Locale.getDefault(), "R$ %.2f", produto.getPreco()));
        }

        // Habilitar/desabilitar botão comprar baseado no estoque
        if (botaoComprar != null) {
            if (produto.getEstoque() > 0) {
                botaoComprar.setEnabled(true);
                botaoComprar.setText("Comprar Agora");
            } else {
                botaoComprar.setEnabled(false);
                botaoComprar.setText("Produto Indisponível");
            }
        }

        // Carregar imagem do produto
        if (imagemProduto != null) {
            String caminhoImagem = produto.getCaminhoImagem();

            if (caminhoImagem != null && !caminhoImagem.isEmpty()) {
                if (caminhoImagem.startsWith("http://") || caminhoImagem.startsWith("https://")) {
                    // Carregar do Supabase com Glide
                    Log.d(TAG, "Carregando imagem do Supabase: " + caminhoImagem);

                    Glide.with(this)
                            .load(caminhoImagem)
                            .placeholder(R.drawable.image_placeholder)
                            .error(R.drawable.sem_imagem)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(imagemProduto);
                } else {
                    // Carregar do drawable (sistema antigo)
                    Log.d(TAG, "Tentando carregar imagem local: " + caminhoImagem);

                    String nomeImagem = caminhoImagem.replace("/", "_").replace(".", "_");
                    int imageResId = getResources().getIdentifier(
                            nomeImagem,
                            "drawable",
                            getPackageName()
                    );

                    if (imageResId != 0) {
                        imagemProduto.setImageResource(imageResId);
                        Log.d(TAG, "Imagem local carregada com sucesso");
                    } else {
                        Log.w(TAG, "Imagem local não encontrada, usando placeholder");
                        imagemProduto.setImageResource(R.drawable.sem_imagem);
                    }
                }
            } else {
                Log.d(TAG, "Produto sem imagem definida");
                imagemProduto.setImageResource(R.drawable.sem_imagem);
            }
        }

        Log.d(TAG, "Informações do produto carregadas com sucesso");
    }
}