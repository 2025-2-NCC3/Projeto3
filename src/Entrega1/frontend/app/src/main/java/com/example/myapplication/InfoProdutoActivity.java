package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class InfoProdutoActivity extends AppCompatActivity {

    Button botaoVoltar, botaoComprar;
    ImageView imagemProduto;
    TextView nomeProduto, precoProduto, descricaoProduto;
    private CarrinhoHelper carrinhoHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_produto);

        carrinhoHelper = CarrinhoHelper.getInstance(this);

        botaoVoltar = findViewById(R.id.botaoVoltar);
        botaoComprar = findViewById(R.id.botaoComprar);
        imagemProduto = findViewById(R.id.imagemProduto);
        nomeProduto = findViewById(R.id.nomeProduto);
        precoProduto = findViewById(R.id.precoProduto);
        descricaoProduto = findViewById(R.id.descricaoProduto);

        botaoVoltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            Produto objetoRecebido = (Produto) bundle.getSerializable("produtoInfo", Produto.class);

            if (objetoRecebido != null) {
                String caminhoImagem = objetoRecebido.getCaminhoImagem();

                if (caminhoImagem != null && !caminhoImagem.isEmpty()) {
                    int imageResId = getResources().getIdentifier(caminhoImagem, "drawable", getPackageName());
                    if (imageResId != 0) {
                        imagemProduto.setImageResource(imageResId);
                    } else {
                        imagemProduto.setImageResource(R.drawable.sem_imagem);
                    }
                } else {
                    imagemProduto.setImageResource(R.drawable.sem_imagem);
                }

                nomeProduto.setText(objetoRecebido.getNome() != null ? objetoRecebido.getNome() : "Produto");
                precoProduto.setText(String.format(Locale.getDefault(), "R$ %.2f", objetoRecebido.getPreco()));
                descricaoProduto.setText(objetoRecebido.getDescricao() != null ? objetoRecebido.getDescricao() : "Sem descrição");

                botaoComprar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // ADICIONA O PRODUTO AO CARRINHO
                        carrinhoHelper.adicionarProduto(objetoRecebido, 1);
                        Toast.makeText(InfoProdutoActivity.this, "Produto adicionado ao carrinho!", Toast.LENGTH_SHORT).show();

                        // Vai para a tela do carrinho (ou CriarPedidoActivity)
                        Intent intent = new Intent(InfoProdutoActivity.this, CarrinhoActivity.class);
                        startActivity(intent);
                    }
                });
            }
        }
    }
}