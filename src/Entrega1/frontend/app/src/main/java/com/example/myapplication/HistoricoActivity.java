package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HistoricoActivity extends AppCompatActivity {

    private static final String TAG = "CardapioAlunosActivity";

    Button botaoVoltar;
    LinearLayout boxLista;
    private List<Order> compras;
    private SupabaseClient supabaseClient;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historico);

        // Inicializar componentes
        botaoVoltar = findViewById(R.id.botaoVoltar);
        boxLista = findViewById(R.id.boxLista);
        compras = new ArrayList<>();
        sessionManager = SessionManager.getInstance(getApplicationContext());

        // Inicializar SupabaseClient
        supabaseClient = SupabaseClient.getInstance(this);

        // Retornar ao perfil
        botaoVoltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HistoricoActivity.this, PerfilActivity.class);
                startActivity(intent);
            }
        });

        // Carregar produtos do banco de dados
        carregarProdutosDoSupabase();
    }

    private void carregarProdutosDoSupabase() {
        // Mostrar mensagem de carregamento
        Toast.makeText(this, "Carregando histórico...", Toast.LENGTH_SHORT).show();

        supabaseClient.getAllPurchases(new SupabaseClient.SupabaseCallback<List<Order>>() {
            @Override
            public void onSuccess(List<Order> historicoDoBank) {
                runOnUiThread(() -> {
                    Log.d(TAG, "Compras carregados do Supabase: " + historicoDoBank.size());

                    // Atualizar lista de produtos
                    compras.clear();
                    compras.addAll(historicoDoBank);

                    // Exibir produtos na tela
                    exibirCompras(compras);

                    Toast.makeText(HistoricoActivity.this,
                            "Histórico carregado: " + compras.size() + " itens",
                            Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Log.e(TAG, "Erro ao carregar produtos: " + error);

                    // Em caso de erro, usar produtos de exemplo
                    Toast.makeText(HistoricoActivity.this,
                            "Erro ao carregar do servidor. Usando dados locais.",
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void exibirCompras(List<Order> comprasParaExibir) {
        // Limpar compras anteriores
        boxLista.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(this);

        for (Order order : comprasParaExibir) {

            // Cria a visualização da compra que será adicionada no layout
            View compraView = inflater.inflate(R.layout.historico, boxLista, false);

            // Pega referencias para cada elemento
            LinearLayout boxHistorico = compraView.findViewById(R.id.boxHistorico);
            TextView valorCompra = compraView.findViewById(R.id.valorCompra);
            TextView dataCompra = compraView.findViewById(R.id.dataCompra);

            // Altera a informação de cada elemento
            dataCompra.setText((CharSequence) order.getCreatedAt());

            // Formatar preço
            valorCompra.setText(String.format(Locale.getDefault(), "R$ %.2f", order.getTotal()));

            // Adiciona a visualização configurada no activity_historico
            boxLista.addView(compraView);

            // Adiciona função para abrir a página de informações ao clicar no produto
            boxHistorico.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(HistoricoActivity.this, InfoCompraActivity.class);
                    intent.putExtra("compraInfo", order);
                    startActivity(intent);
                }
            });
        }

        Log.d(TAG, "Exibidas " + comprasParaExibir.size() + " compras na tela");
    }
}
