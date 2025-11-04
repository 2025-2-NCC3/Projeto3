package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.button.MaterialButton;

import java.util.Locale;

public class InfoProdutoActivity extends AppCompatActivity {
    private static final String TAG = "InfoProdutoActivity";

    private ImageView imagemProduto;
    private TextView nomeProduto;
    private TextView descricaoProduto;
    private TextView precoProduto;
    private TextView categoriaProduto;
    private TextView txtQuantidade;
    private TextView txtPrecoTotal;
    private ImageButton botaoVoltar;
    private ImageButton btnDiminuir;
    private ImageButton btnAumentar;
    private MaterialButton btnAdicionarCarrinho;

    private Produto produto;
    private CarrinhoHelper carrinhoHelper;
    private int quantidade = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_produto);

        carrinhoHelper = CarrinhoHelper.getInstance(this);
        inicializarComponentes();

        // Receber o intent com as informações do produto clicado
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            produto = (Produto) bundle.getSerializable("produtoInfo", Produto.class);

            if (produto != null) {
                Log.d(TAG, "Produto recebido: " + produto.getNome());
                carregarInformacoesProduto(produto);
                configurarListeners();
                atualizarPrecoTotal();
            } else {
                Toast.makeText(this, "Erro ao carregar produto", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(this, "Nenhum produto selecionado", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void inicializarComponentes() {
        imagemProduto = findViewById(R.id.imagemProduto);
        nomeProduto = findViewById(R.id.nomeProduto);
        descricaoProduto = findViewById(R.id.descricaoProduto);
        precoProduto = findViewById(R.id.precoProduto);
        categoriaProduto = findViewById(R.id.categoriaProduto);
        txtQuantidade = findViewById(R.id.txtQuantidade);
        txtPrecoTotal = findViewById(R.id.txtPrecoTotal);
        botaoVoltar = findViewById(R.id.botaoVoltar);
        btnDiminuir = findViewById(R.id.btnDiminuir);
        btnAumentar = findViewById(R.id.btnAumentar);
        btnAdicionarCarrinho = findViewById(R.id.btnAdicionarCarrinho);

        Log.d(TAG, "Componentes inicializados");
    }

    private void configurarListeners() {
        // Botão voltar
        botaoVoltar.setOnClickListener(v -> finish());

        // Botão diminuir quantidade
        btnDiminuir.setOnClickListener(v -> {
            if (quantidade > 1) {
                quantidade--;
                atualizarQuantidade();
            } else {
                Toast.makeText(this, "Quantidade mínima é 1", Toast.LENGTH_SHORT).show();
            }
        });

        // Botão aumentar quantidade
        btnAumentar.setOnClickListener(v -> {
            if (produto != null && quantidade < produto.getEstoque()) {
                quantidade++;
                atualizarQuantidade();
            } else {
                Toast.makeText(this, "Estoque máximo atingido", Toast.LENGTH_SHORT).show();
            }
        });

        // Botão adicionar ao carrinho
        btnAdicionarCarrinho.setOnClickListener(v -> {
            if (produto != null && produto.getEstoque() > 0) {
                // Adiciona ao carrinho com a quantidade selecionada
                carrinhoHelper.adicionarProduto(produto, quantidade);

                Toast.makeText(InfoProdutoActivity.this,
                        quantidade + "x " + produto.getNome() + " adicionado ao carrinho!",
                        Toast.LENGTH_SHORT).show();

                // Vai para a tela do carrinho
                Intent intent = new Intent(InfoProdutoActivity.this, CarrinhoActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(InfoProdutoActivity.this,
                        "Produto indisponível no momento",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void atualizarQuantidade() {
        txtQuantidade.setText(String.valueOf(quantidade));
        atualizarPrecoTotal();
    }

    private void atualizarPrecoTotal() {
        if (produto != null && txtPrecoTotal != null) {
            double precoTotal = produto.getPreco() * quantidade;
            txtPrecoTotal.setText(PedidoUtils.formatarPreco(precoTotal));
        }
    }

    private void carregarInformacoesProduto(Produto produto) {
        // Definir nome do produto
        if (nomeProduto != null) {
            nomeProduto.setText(produto.getNome());
        }

        // Definir descrição
        if (descricaoProduto != null) {
            String descricao = produto.getDescricao();
            if (descricao == null || descricao.isEmpty()) {
                descricao = "Sem descrição disponível.";
            }
            descricaoProduto.setText(descricao);
        }

        // Definir preço
        if (precoProduto != null) {
            precoProduto.setText(PedidoUtils.formatarPreco(produto.getPreco()));
        }

        // Definir categoria
        if (categoriaProduto != null) {
            String categoria = getCategoriaTexto(produto.getCategoria());
            categoriaProduto.setText(categoria.toUpperCase());
        }


        // Habilitar/desabilitar botão adicionar baseado no estoque
        if (btnAdicionarCarrinho != null) {
            if (produto.getEstoque() > 0) {
                btnAdicionarCarrinho.setEnabled(true);
            } else {
                btnAdicionarCarrinho.setEnabled(false);
                btnAdicionarCarrinho.setText("INDISPONÍVEL");
            }
        }

        // Desabilitar botões de quantidade se sem estoque
        if (produto.getEstoque() == 0) {
            btnDiminuir.setEnabled(false);
            btnAumentar.setEnabled(false);
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
                            .placeholder(R.drawable.sem_imagem)
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

    private String getCategoriaTexto(int categoriaId) {
        switch (categoriaId) {
            case 1: return "Bebidas";
            case 2: return "Pratos Principais";
            case 3: return "Sobremesas";
            case 4: return "Entradas";
            case 5: return "Lanches";
            default: return "Outros";
        }
    }
}