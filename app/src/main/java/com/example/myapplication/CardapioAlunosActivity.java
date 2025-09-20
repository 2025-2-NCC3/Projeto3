package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CardapioAlunosActivity extends AppCompatActivity {

    Button botaoVoltar;
    LinearLayout boxLista;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cardapio);

        botaoVoltar = findViewById(R.id.botaoVoltar);
        boxLista = findViewById(R.id.boxLista);

        // Retornar à MainActivity
        botaoVoltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CardapioAlunosActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        // Instanciando produtos exemplo
        List<Produto> produtos = new ArrayList<>();
        produtos.add(new Produto(1, "Coxinha", "Coxinha recheada de frango.", "coxinha_exemplo", 1.99, 10, 1));
        produtos.add(new Produto(2, "Croissant", "Croissant de presunto e queijo.", "croissant_exemplo", 2.99, 12, 1));
        produtos.add(new Produto(3, "Brownie", "Brownie de chocolate.", "brownie_exemplo", 2.49, 20, 2));

        LayoutInflater inflater = LayoutInflater.from(this);

        for (Produto produto : produtos) {
            // Cria a visualização do produto que será adicionado no layout
            View productView = inflater.inflate(R.layout.produto, boxLista, false);

            // Pega referencias para cada elemento
            LinearLayout boxProduto = productView.findViewById(R.id.boxProduto);
            ImageView imagemProduto = productView.findViewById(R.id.imagemProduto);
            TextView tituloProduto = productView.findViewById(R.id.tituloProduto);
            TextView precoProduto = productView.findViewById(R.id.precoProduto);

            // Altera a informação de cada elemento
            tituloProduto.setText(produto.getNome());
            precoProduto.setText(String.format(Locale.getDefault(), "R$%.2f", produto.getPreco()));

            // Altera a imagem do produto
            int imageResId = getResources().getIdentifier(produto.getCaminhoImagem(), "drawable", getPackageName());
            if (imageResId != 0) {
                imagemProduto.setImageResource(imageResId);
            } else {
                // Usa um placeholder caso o produto não tenha imagem
                imagemProduto.setImageResource(R.drawable.sem_imagem);
            }

            // Adiciona a visualização configurada no activity_cardapio
            boxLista.addView(productView);

            // Adiciona função para abrir a página de informações ao clicar no produto
            boxProduto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(CardapioAlunosActivity.this, InfoProdutoActivity.class);
                    intent.putExtra("produtoInfo", produto);
                    startActivity(intent);
                }
            });
        }
    }
}
