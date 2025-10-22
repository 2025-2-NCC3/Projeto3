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

public class HistoricoActivity extends AppCompatActivity {

    private static final String TAG = "CardapioAlunosActivity";

    Button botaoVoltar;
    LinearLayout boxLista;
    private List<Compra> compras;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historico);

        // Inicializar componentes
        botaoVoltar = findViewById(R.id.botaoVoltar);
        boxLista = findViewById(R.id.boxLista);
        compras = new ArrayList<>();

        // Retornar ao perfil
        botaoVoltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HistoricoActivity.this, PerfilActivity.class);
                startActivity(intent);
            }
        });
    }

    private void exibirCompras(List<Compra> comprasParaExibir) {
        // Limpar compras anteriores
        boxLista.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(this);

        for (Compra compra : comprasParaExibir) {

            // Cria a visualização do produto que será adicionado no layout
            View productView = inflater.inflate(R.layout.historico, boxLista, false);

            // Pega referencias para cada elemento
            LinearLayout boxHistorico = productView.findViewById(R.id.boxHistorico);
            TextView valorCompra = productView.findViewById(R.id.valorCompra);
            TextView dataCompra = productView.findViewById(R.id.dataCompra);

            // Altera a informação de cada elemento
            dataCompra.setText(produto.getData());

            // Formatar preço
            valorCompra.setText(String.format(Locale.getDefault(), "R$ %.2f", compra.getPreco()));

            // Adiciona a visualização configurada no activity_cardapio
            boxLista.addView(productView);

            // Adiciona função para abrir a página de informações ao clicar no produto
            boxHistorico.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(HistoricoActivity.this, InfoCompraActivity.class);
                    intent.putExtra("compraInfo", compra);
                    startActivity(intent);
                }
            });
        }

        Log.d(TAG, "Exibidas " + comprasParaExibir.size() + " compras na tela");
    }
}
