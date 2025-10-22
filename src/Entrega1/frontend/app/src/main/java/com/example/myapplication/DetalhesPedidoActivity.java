package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DetalhesPedidoActivity extends AppCompatActivity {
    private static final String TAG = "DetalhesPedido";

    private TextView tvPedidoId, tvDataPedido, tvStatus, tvClienteNome, tvTotalPedido;
    private Button btnCancelarPedido, btnVoltar;
    private RecyclerView recyclerViewItens;
    private OrderItemAdapter adapter;
    private Order pedidoAtual;
    private SupabaseOrderManager orderManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes_pedido);

        inicializarComponentes();
        carregarDadosPedido();
    }

    private void inicializarComponentes() {
        // Inicializar Views de acordo com o XML
        tvPedidoId = findViewById(R.id.tv_pedido_id);
        tvDataPedido = findViewById(R.id.tv_data_pedido);
        tvStatus = findViewById(R.id.tv_status);
        tvClienteNome = findViewById(R.id.tv_cliente_nome);
        tvTotalPedido = findViewById(R.id.tv_total_pedido);
        btnCancelarPedido = findViewById(R.id.btn_cancelar_pedido);
        btnVoltar = findViewById(R.id.btn_voltar);
        recyclerViewItens = findViewById(R.id.recycler_itens_pedido);

        // Inicializar OrderManager
        orderManager = SupabaseOrderManager.getInstance(this);

        // Configurar RecyclerView
        recyclerViewItens.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrderItemAdapter(getApplicationContext());
        recyclerViewItens.setAdapter(adapter);

        // Configurar listeners
        btnCancelarPedido.setOnClickListener(v -> cancelarPedido());
        btnVoltar.setOnClickListener(v -> finish());
    }

    private void carregarDadosPedido() {
        // Receber o ID do pedido e access token da intent
        String pedidoId = getIntent().getStringExtra("pedido_id");
        String accessToken = getIntent().getStringExtra("access_token");

        Log.d(TAG, "Carregando pedido ID: " + pedidoId);

        if (pedidoId == null || pedidoId.isEmpty()) {
            Toast.makeText(this, "Erro: ID do pedido não encontrado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (accessToken == null || accessToken.isEmpty()) {
            Toast.makeText(this, "Erro: Token de acesso não encontrado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Buscar pedido no Supabase
        orderManager.getOrderById(pedidoId, accessToken, new SupabaseOrderManager.OrderCallback() {
            @Override
            public void onSuccess(Order order) {
                Log.d(TAG, "Pedido carregado com sucesso: " + order.toString());
                pedidoAtual = order;
                runOnUiThread(() -> exibirDadosPedido(order));
            }

            @Override
            public void onError(String erro) {
                Log.e(TAG, "Erro ao carregar pedido: " + erro);
                runOnUiThread(() -> {
                    Toast.makeText(DetalhesPedidoActivity.this,
                            "Erro ao carregar pedido: " + erro,
                            Toast.LENGTH_LONG).show();
                    finish();
                });
            }
        });
    }

    private void exibirDadosPedido(Order order) {
        Log.d(TAG, "=== EXIBINDO DADOS DO PEDIDO ===");

        // ID do pedido (sem o prefixo "Pedido #" conforme XML)
        String pedidoIdText = order.getId();
        tvPedidoId.setText(pedidoIdText);
        Log.d(TAG, "ID: " + pedidoIdText);

        // Data formatada do pedido
        Date dataPedido = order.getCreatedAtDate();
        String dataFormatada = formatarData(dataPedido.getTime());
        tvDataPedido.setText(dataFormatada);
        Log.d(TAG, "Created At (ISO): " + order.getCreatedAt());
        Log.d(TAG, "Created At (Date): " + dataPedido);
        Log.d(TAG, "Data Formatada: " + dataFormatada);

        // Status com cor
        String statusText = order.getStatus();
        tvStatus.setText(statusText);
        definirCorStatus(statusText);
        Log.d(TAG, "Status: " + statusText);

        // Nome do cliente
        String clienteNome = order.getStudentName() != null ? order.getStudentName() : "N/A";
        tvClienteNome.setText(clienteNome);
        Log.d(TAG, "Cliente: " + clienteNome);

        // Total do pedido
        String totalText = "R$ " + String.format(Locale.getDefault(), "%.2f", order.getTotal());
        tvTotalPedido.setText(totalText);
        Log.d(TAG, "Total: " + totalText);

        // Itens do pedido
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            adapter.atualizarItens(order.getItems());
            Log.d(TAG, "Itens carregados: " + order.getItems().size());
        } else {
            Log.w(TAG, "Pedido sem itens!");
            Toast.makeText(this, "Este pedido não possui itens", Toast.LENGTH_SHORT).show();
        }

        // Controlar botão de cancelar baseado no status
        String statusLower = statusText.toLowerCase();
        if (statusLower.equals("entregue") || statusLower.equals("cancelado")) {
            btnCancelarPedido.setEnabled(false);
            btnCancelarPedido.setAlpha(0.5f);
            btnCancelarPedido.setText("Pedido " + statusText);
        } else {
            btnCancelarPedido.setEnabled(true);
            btnCancelarPedido.setAlpha(1.0f);
            btnCancelarPedido.setText("Cancelar Pedido");
        }
    }

    private void cancelarPedido() {
        if (pedidoAtual == null) {
            Toast.makeText(this, "Erro: Pedido não carregado", Toast.LENGTH_SHORT).show();
            return;
        }

        String accessToken = getIntent().getStringExtra("access_token");
        if (accessToken == null || accessToken.isEmpty()) {
            Toast.makeText(this, "Erro: Token de acesso não encontrado", Toast.LENGTH_SHORT).show();
            return;
        }

        // Desabilitar botão temporariamente
        btnCancelarPedido.setEnabled(false);
        btnCancelarPedido.setText("Cancelando...");

        Log.d(TAG, "Cancelando pedido ID: " + pedidoAtual.getId());

        orderManager.cancelOrder(pedidoAtual.getId(), accessToken, new SupabaseOrderManager.OrderCallback() {
            @Override
            public void onSuccess(Order order) {
                Log.d(TAG, "Pedido cancelado com sucesso");
                runOnUiThread(() -> {
                    Toast.makeText(DetalhesPedidoActivity.this,
                            "Pedido cancelado com sucesso",
                            Toast.LENGTH_SHORT).show();
                    pedidoAtual = order;
                    exibirDadosPedido(order);
                });
            }

            @Override
            public void onError(String erro) {
                Log.e(TAG, "Erro ao cancelar pedido: " + erro);
                runOnUiThread(() -> {
                    Toast.makeText(DetalhesPedidoActivity.this,
                            "Erro ao cancelar: " + erro,
                            Toast.LENGTH_LONG).show();
                    // Reabilitar botão em caso de erro
                    btnCancelarPedido.setEnabled(true);
                    btnCancelarPedido.setText("Cancelar Pedido");
                });
            }
        });
    }

    /**
     * Formata timestamp para string legível
     */
    private String formatarData(long timestamp) {
        try {
            Date date = new Date(timestamp);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            return sdf.format(date);
        } catch (Exception e) {
            Log.e(TAG, "Erro ao formatar data", e);
            return "Data inválida";
        }
    }

    /**
     * Define a cor do texto do status baseado no valor
     */
    private void definirCorStatus(String status) {
        if (status == null || status.isEmpty()) {
            tvStatus.setTextColor(getResources().getColor(R.color.black));
            return;
        }

        int cor;
        switch (status.toLowerCase()) {
            case "pendente":
                cor = getResources().getColor(R.color.status_pendente);
                break;
            case "preparando":
                cor = getResources().getColor(R.color.status_preparando);
                break;
            case "pronto":
                cor = getResources().getColor(R.color.status_pronto);
                break;
            case "entregue":
                cor = getResources().getColor(R.color.status_entregue);
                break;
            case "cancelado":
                cor = getResources().getColor(R.color.status_cancelado);
                break;
            default:
                cor = getResources().getColor(R.color.black);
        }
        tvStatus.setTextColor(cor);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Limpar recursos se necessário
        if (adapter != null) {
            adapter.atualizarItens(new java.util.ArrayList<>());
        }
    }
}