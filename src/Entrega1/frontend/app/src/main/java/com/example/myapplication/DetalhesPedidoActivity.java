package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class DetalhesPedidoActivity extends AppCompatActivity {

    private TextView tvPedidoCodigo, tvPedidoId, tvDataPedido, tvStatus, tvClienteNome;
    private TextView tvDesconto, tvTotalPedido;
    private MaterialButton btnCancelarPedido;
    private ImageButton btnVoltar;
    private RecyclerView recyclerViewItens;
    private MaterialCardView cardPagamento;
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
        tvPedidoCodigo = findViewById(R.id.tv_pedido_codigo);
        tvPedidoId = findViewById(R.id.tv_pedido_id);
        tvDataPedido = findViewById(R.id.tv_data_pedido);
        tvStatus = findViewById(R.id.tv_status);
        tvClienteNome = findViewById(R.id.tv_cliente_nome);
        tvDesconto = findViewById(R.id.tv_desconto);
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
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault());

        // Código do pedido no header e na lista
        String codigo = order.getCode() != null ? "#" + order.getCode() :
                "#ORD-" + (order.getId().length() > 8 ? order.getId().substring(0, 8) : order.getId());

        tvPedidoCodigo.setText(codigo);
        tvPedidoId.setText(codigo);

        // Data e hora
        tvDataPedido.setText(dateFormat.format(order.getCreatedAt()));

        // Status - mostrar texto legível
        String status = order.getStatus();
        String statusExibir = "";
        int corStatus = 0;

        switch (status.toUpperCase()) {
            case "PENDING":
            case "PENDENTE":
                statusExibir = "PENDENTE";
                corStatus = 0xFFC97B5A; // Laranja/amarelado
                break;
            case "PREPARING":
            case "PREPARANDO":
                statusExibir = "PREPARANDO";
                corStatus = 0xFF3D7A4C; // Verde escuro
                break;
            case "READY":
            case "PRONTO":
                statusExibir = "PRONTO";
                corStatus = 0xFFA8C3A0; // Verde claro
                break;
            case "DELIVERED":
            case "ENTREGUE":
                statusExibir = "ENTREGUE";
                corStatus = 0xFF235135; // Verde muito escuro
                break;
            case "PICKED_UP":
            case "RETIRADO":
                statusExibir = "RETIRADO";
                corStatus = 0xFF235135; // Verde muito escuro
                break;
            case "CANCELLED":
            case "CANCELADO":
                statusExibir = "CANCELADO";
                corStatus = 0xFF8B4A3D; // Vermelho/marrom
                break;
            default:
                statusExibir = status.toUpperCase();
                corStatus = 0xFF6E6B65; // Cinza padrão
                break;
        }

        tvStatus.setText(statusExibir);
        tvStatus.setTextColor(corStatus);

        // Cliente
        tvClienteNome.setText(order.getStudentName());

        // Desconto (por enquanto sempre 0)
        tvDesconto.setText("R$ 0,00");

        // Total
        tvTotalPedido.setText(String.format(Locale.getDefault(), "R$ %.2f", order.getTotal()));

        // Itens do pedido
        adapter.atualizarItens(order.getItems());

        // Controlar visibilidade do botão cancelar
        String statusUpper = order.getStatus().toUpperCase();
        if (statusUpper.equals("ENTREGUE") || statusUpper.equals("DELIVERED") ||
                statusUpper.equals("RETIRADO") || statusUpper.equals("PICKED_UP") ||
                statusUpper.equals("CANCELADO") || statusUpper.equals("CANCELLED")) {
            btnCancelarPedido.setEnabled(false);
            btnCancelarPedido.setAlpha(0.5f);
            btnCancelarPedido.setText("PEDIDO " + statusExibir);
        } else {
            btnCancelarPedido.setEnabled(true);
            btnCancelarPedido.setAlpha(1.0f);
            btnCancelarPedido.setText("CANCELAR PEDIDO");
        }
    }

    private void confirmarCancelamento() {
        if (pedidoAtual == null) return;

        String codigo = pedidoAtual.getCode() != null ? pedidoAtual.getCode() : "N/A";

        new AlertDialog.Builder(this)
                .setTitle("Cancelar Pedido")
                .setMessage("Tem certeza que deseja cancelar este pedido?\n\nCódigo: " + codigo)
                .setPositiveButton("Sim, Cancelar", (dialog, which) -> cancelarPedido())
                .setNegativeButton("Não", null)
                .show();
    }

    private void cancelarPedido() {
        if (pedidoAtual == null || accessToken == null) return;

        // Desabilitar botão durante o processo
        btnCancelarPedido.setEnabled(false);
        btnCancelarPedido.setText("CANCELANDO...");

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
                            btnCancelarPedido.setText("CANCELAR PEDIDO");
                        });
                    }
                });
    }
}