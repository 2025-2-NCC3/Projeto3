package com.example.myapplication;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class CarrinhoActivity extends AppCompatActivity implements CarrinhoManager.CarrinhoListener {

    private TextView tvResumoCarrinho;
    private ListView lvItensCarrinho;
    private Button btnFinalizarPedido;

    private CarrinhoManager carrinhoManager;
    private SupabaseOrderManager supabaseOrderManager;

    // Dados do usuário
    private String userAccessToken = "seu_access_token_aqui";
    private String studentId = "2023001";
    private String studentName = "Cliente Cantina";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_carrinho); // Você precisa criar este layout

        carrinhoManager = CarrinhoManager.getInstance(this);
        supabaseOrderManager = SupabaseOrderManager.getInstance(this);

        carrinhoManager.setCarrinhoListener(this);

        // Inicializar views
        // tvResumoCarrinho = findViewById(R.id.tv_resumo_carrinho);
        // lvItensCarrinho = findViewById(R.id.lv_itens_carrinho);
        // btnFinalizarPedido = findViewById(R.id.btn_finalizar_pedido);

        // btnFinalizarPedido.setOnClickListener(v -> finalizarPedido());

        atualizarInterface();
    }

    private void finalizarPedido() {
        if (carrinhoManager.isEmpty()) {
            Toast.makeText(this, "Carrinho está vazio!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Criar OrderRequest a partir do carrinho
        OrderRequest request = carrinhoManager.criarOrderRequest(studentId, studentName);

        Toast.makeText(this, "Finalizando pedido...", Toast.LENGTH_SHORT).show();

        // Enviar para Supabase
        supabaseOrderManager.createOrder(request, userAccessToken, new SupabaseOrderManager.OrderCallback() {
            @Override
            public void onSuccess(Order order) {
                runOnUiThread(() -> {
                    String message = "✅ Pedido finalizado!\n" +
                            "Código: " + order.getCode() + "\n" +
                            "Total: R$ " + String.format("%.2f", order.getTotal());

                    Toast.makeText(CarrinhoActivity.this, message, Toast.LENGTH_LONG).show();

                    // Limpar carrinho após sucesso
                    carrinhoManager.limparCarrinho();
                    finish();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(CarrinhoActivity.this,
                            "Erro ao finalizar pedido: " + error,
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    @Override
    public void onCarrinhoAtualizado() {
        runOnUiThread(this::atualizarInterface);
    }

    private void atualizarInterface() {
        // Atualizar resumo do carrinho
        if (tvResumoCarrinho != null) {
            tvResumoCarrinho.setText(carrinhoManager.getResumoCarrinho());
        }

        // Habilitar/desabilitar botão
        if (btnFinalizarPedido != null) {
            btnFinalizarPedido.setEnabled(!carrinhoManager.isEmpty());
        }

        // Atualizar lista de itens ( precisará criar um adapter)
        // Implementar adapter para ListView com os itens do carrinho
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (carrinhoManager != null) {
            carrinhoManager.setCarrinhoListener(null);
        }
    }
}