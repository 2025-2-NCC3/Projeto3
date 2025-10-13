package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class InfoProdutoActivity extends AppCompatActivity {

    Button botaoVoltar, botaoComprar;
    ImageView imagemProduto;
    TextView nomeProduto, precoProduto, descricaoProduto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_produto);

        botaoVoltar = findViewById(R.id.botaoVoltar);
        botaoComprar = findViewById(R.id.botaoComprar);
        imagemProduto = findViewById(R.id.imagemProduto);
        nomeProduto = findViewById(R.id.nomeProduto);
        precoProduto = findViewById(R.id.precoProduto);
        descricaoProduto = findViewById(R.id.descricaoProduto);

        botaoVoltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Melhor usar finish() ao invés de criar novo Intent
            }
        });

        // Recebe o intent com as informações do produto clicado
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            Produto objetoRecebido = (Produto) bundle.getSerializable("produtoInfo", Produto.class);

            if (objetoRecebido != null) {
                // Carregar imagem de forma segura
                String caminhoImagem = objetoRecebido.getCaminhoImagem();

                if (caminhoImagem != null && !caminhoImagem.isEmpty()) {
                    int imageResId = getResources().getIdentifier(caminhoImagem, "drawable", getPackageName());
                    if (imageResId != 0) {
                        imagemProduto.setImageResource(imageResId);
                    } else {
                        // Imagem não encontrada nos recursos
                        imagemProduto.setImageResource(R.drawable.sem_imagem);
                    }
                } else {
                    // Caminho da imagem é nulo ou vazio
                    imagemProduto.setImageResource(R.drawable.sem_imagem);
                }

                // Preencher outros campos
                nomeProduto.setText(objetoRecebido.getNome() != null ? objetoRecebido.getNome() : "Produto");
                precoProduto.setText(String.format(Locale.getDefault(), "R$ %.2f", objetoRecebido.getPreco()));
                descricaoProduto.setText(objetoRecebido.getDescricao() != null ? objetoRecebido.getDescricao() : "Sem descrição");

                // Envia as informações do produto à CreateOrderActivity
                botaoComprar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(InfoProdutoActivity.this, CriarPedidoActivity.class);
                        intent.putExtra("produtoComprado", objetoRecebido);
                        startActivity(intent);
                    }
                });
            }
        }
    }
}