package com.example.myapplication;

import static android.content.ContentValues.TAG;

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

import java.util.List;
import java.util.Locale;

public class InfoCompraActivity extends AppCompatActivity {

    private TextView precoTotal, nomeProduto, descricaoProduto;
    private Button botaoVoltar;
    private Order order;
    LinearLayout boxListaItems;
    private List<OrderItem> produtos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_compra);

        precoTotal = findViewById(R.id.precoTotal);
        botaoVoltar = findViewById(R.id.botaoVoltar);
        boxListaItems = findViewById(R.id.boxListaItems);

        // Configurar botão voltar
        botaoVoltar.setOnClickListener(v -> finish());

        // Receber o intent com as informações do pedido clicado
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            order = bundle.getSerializable("compraInfo", Order.class);

            if (order != null) {
                Log.d(TAG, "Compra recebida: " + order.getId());
                carregarInformacoesPedido(order);
            } else {
                Toast.makeText(this, "Erro ao carregar compra", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(this, "Nenhuma compra selecionada", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void carregarInformacoesPedido(Order order) {
        produtos = order.getItems();
        precoTotal.setText(String.format(Locale.getDefault(), "R$ %.2f", order.getTotal()));

        // Limpar produtos anteriores
        boxListaItems.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (OrderItem item : produtos) {
            // Cria a visualização do item que será adicionado no layout
            View productView = inflater.inflate(R.layout.info_compra, boxListaItems, false);

            // Pega referencias para cada elemento
            TextView nomeProduto = productView.findViewById(R.id.nomeProduto);
            TextView precoProduto = productView.findViewById(R.id.precoProduto);
            TextView quantidadeProduto = productView.findViewById(R.id.quantidadeProduto);

            // Altera a informação de cada elemento
            nomeProduto.setText(item.getProductName());

            // Formatar preço
            precoProduto.setText(String.format(Locale.getDefault(), "R$ %.2f", item.getPrice()));

            // Formatar quantidade
            quantidadeProduto.setText("x " + item.getQuantity());

            // Adiciona a visualização configurada no activity_info_compra
            boxListaItems.addView(productView);

            Log.d(TAG, "Exibidos " + produtos.size() + " produtos na tela");
        }
    }
}
