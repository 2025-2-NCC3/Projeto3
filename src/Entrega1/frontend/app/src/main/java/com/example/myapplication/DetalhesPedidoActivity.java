package com.example.myapplication;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class DetalhesPedidoActivity extends AppCompatActivity {

    private TextView tvPedidoId, tvDataPedido, tvStatus, tvClienteNome, tvTotalPedido;
    private Button btnCancelarPedido, btnVoltar;
    private RecyclerView recyclerViewItens;
    private OrderItemAdapter adapter;
    private Order pedidoAtual;
    private SupabaseOrderManager orderManager;
    private String accessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes_pedido);

        inicializarComponentes();
        carregarDadosPedido();
    }

    private void inicializarComponentes() {
        // Views do layout
        tvPedidoId = findViewById(R.id.tv_pedido_id);
        tvDataPedido = findViewById(R.id.tv_data_pedido);
        tvStatus = findViewById(R.id.tv_status);
        tvClienteNome = findViewById(R.id.tv_cliente_nome);
        tvTotalPedido = findViewById(R.id.tv_total_pedido);
        btnCancelarPedido = findViewById(R.id.btn_cancelar_pedido);
        btnVoltar = findViewById(R.id.btn_voltar);
        recyclerViewItens = findViewById(R.id.recycler_itens_pedido);

        orderManager = SupabaseOrderManager.getInstance(this);

        // Configurar RecyclerView
        recyclerViewItens.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrderItemAdapter(this);
        recyclerViewItens.setAdapter(adapter);

        // Listeners
        btnCancelarPedido.setOnClickListener(v -> confirmarCancelamento());
        btnVoltar.setOnClickListener(v -> finish());
    }

    private void carregarDadosPedido() {
        // Receber dados da Intent
        String pedidoId = getIntent().getStringExtra("pedido_id");
        accessToken = getIntent().getStringExtra("access_token");

        if (pedidoId == null || pedidoId.isEmpty() || accessToken == null) {
            Toast.makeText(this, "Erro ao carregar pedido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Buscar pedido no Supabase
        orderManager.getOrderById(pedidoId, accessToken, new SupabaseOrderManager.OrderCallback() {
            @Override
            public void onSuccess(Order order) {
                runOnUiThread(() -> {
                    pedidoAtual = order;
                    exibirDadosPedido(order);
                });
            }

            @Override
            public void onError(String erro) {
                runOnUiThread(() -> {
                    Toast.makeText(DetalhesPedidoActivity.this,
                            "Erro ao carregar: " + erro, Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }

    private void exibirDadosPedido(Order order) {
        // Formatar e exibir dados
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale.getDefault());

        // ID do pedido (mostra os primeiros 8 caracteres)
        String idCurto = order.getId().length() > 8
                ? order.getId().substring(0, 8)
                : order.getId();
        tvPedidoId.setText("#" + idCurto);

        // Data
        tvDataPedido.setText(dateFormat.format(order.getCreatedAt()));

        // Status com emoji
        tvStatus.setText(PedidoUtils.getStatusIcon(order.getStatus()) + " " +
                PedidoUtils.getStatusText(order.getStatus()));
        tvStatus.setTextColor(PedidoUtils.getStatusColor(order.getStatus()));

        // Cliente
        tvClienteNome.setText(order.getStudentName());

        // Total
        tvTotalPedido.setText(String.format(Locale.getDefault(), "R$ %.2f", order.getTotal()));

        // Itens do pedido
        adapter.atualizarItens(order.getItems());

        // Controlar visibilidade do botão cancelar
        String statusUpper = order.getStatus().toUpperCase();
        if (statusUpper.equals("ENTREGUE") ||
                statusUpper.equals("RETIRADO") ||
                statusUpper.equals("CANCELADO")) {
            btnCancelarPedido.setEnabled(false);
            btnCancelarPedido.setAlpha(0.5f);
            btnCancelarPedido.setText("Pedido " + order.getStatus());
        } else {
            btnCancelarPedido.setEnabled(true);
            btnCancelarPedido.setAlpha(1.0f);
            btnCancelarPedido.setText("Cancelar Pedido");
        }
    }

    private void confirmarCancelamento() {
        if (pedidoAtual == null) return;

        new AlertDialog.Builder(this)
                .setTitle("Cancelar Pedido")
                .setMessage("Tem certeza que deseja cancelar este pedido?\n\nCódigo: " +
                        (pedidoAtual.getCode() != null ? pedidoAtual.getCode() : "N/A"))
                .setPositiveButton("Sim, Cancelar", (dialog, which) -> cancelarPedido())
                .setNegativeButton("Não", null)
                .show();
    }

    private void cancelarPedido() {
        if (pedidoAtual == null || accessToken == null) return;

        // Desabilitar botão durante o processo
        btnCancelarPedido.setEnabled(false);
        btnCancelarPedido.setText("Cancelando...");

        orderManager.cancelOrder(pedidoAtual.getId(), accessToken,
                new SupabaseOrderManager.OrderCallback() {
                    @Override
                    public void onSuccess(Order order) {
                        runOnUiThread(() -> {
                            Toast.makeText(DetalhesPedidoActivity.this,
                                    "✅ Pedido cancelado com sucesso", Toast.LENGTH_SHORT).show();
                            pedidoAtual = order;
                            exibirDadosPedido(order);
                        });
                    }

                    @Override
                    public void onError(String erro) {
                        runOnUiThread(() -> {
                            Toast.makeText(DetalhesPedidoActivity.this,
                                    "❌ Erro ao cancelar: " + erro, Toast.LENGTH_SHORT).show();

                            // Reabilitar botão em caso de erro
                            btnCancelarPedido.setEnabled(true);
                            btnCancelarPedido.setText("Cancelar Pedido");
                        });
                    }
                });
    }
}