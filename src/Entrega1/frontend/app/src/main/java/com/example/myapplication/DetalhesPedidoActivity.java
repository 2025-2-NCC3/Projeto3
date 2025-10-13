package com.example.myapplication;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.myapplication.R;
import com.example.myapplication.Order;
import com.example.myapplication.OrderItem;
import com.example.myapplication.SupabaseOrderManager;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DetalhesPedidoActivity extends AppCompatActivity {

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
        tvPedidoId = findViewById(R.id.tv_pedido_id);
        tvDataPedido = findViewById(R.id.tv_data_pedido);
        tvStatus = findViewById(R.id.tv_status);
        tvClienteNome = findViewById(R.id.tv_cliente_nome);
        tvTotalPedido = findViewById(R.id.tv_total_pedido);
        btnCancelarPedido = findViewById(R.id.btn_cancelar_pedido);
        btnVoltar = findViewById(R.id.btn_voltar);
        recyclerViewItens = findViewById(R.id.recycler_itens_pedido);

        orderManager = SupabaseOrderManager.getInstance(this);

        recyclerViewItens.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrderItemAdapter(getApplicationContext());
        recyclerViewItens.setAdapter(adapter);

        btnCancelarPedido.setOnClickListener(v -> cancelarPedido());
        btnVoltar.setOnClickListener(v -> finish());
    }

    private void carregarDadosPedido() {
        // Receber o ID do pedido e access token da intent
        int pedidoId = getIntent().getIntExtra("pedido_id", -1);
        String accessToken = getIntent().getStringExtra("access_token");

        if (pedidoId == -1 || accessToken == null) {
            Toast.makeText(this, "Erro ao carregar pedido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Buscar pedido no Supabase
        orderManager.getOrderById(pedidoId, accessToken, new SupabaseOrderManager.OrderCallback() {
            @Override
            public void onSuccess(Order order) {
                pedidoAtual = order;
                exibirDadosPedido(order);
            }

            @Override
            public void onError(String erro) {
                Toast.makeText(DetalhesPedidoActivity.this,
                        "Erro ao carregar: " + erro, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void exibirDadosPedido(Order order) {
        // ID do pedido
        tvPedidoId.setText("Pedido #" + order.getId());

        // Data formatada
        String dataFormatada = formatarData(order.getCreatedAt().getTime());
        tvDataPedido.setText("Data: " + dataFormatada);

        // Status com cor
        tvStatus.setText("Status: " + order.getStatus());
        definirCorStatus(order.getStatus());

        // Nome do cliente
        tvClienteNome.setText("Cliente: " + order.getStudentName());

        // Total do pedido
        tvTotalPedido.setText("Total: R$ " + String.format("%.2f", order.getTotal()));

        // Itens do pedido
        adapter.atualizarItens(order.getItems());

        // Controlar botão de cancelar
        if (order.getStatus().equalsIgnoreCase("entregue") ||
                order.getStatus().equalsIgnoreCase("cancelado")) {
            btnCancelarPedido.setEnabled(false);
            btnCancelarPedido.setAlpha(0.5f);
        } else {
            btnCancelarPedido.setEnabled(true);
            btnCancelarPedido.setAlpha(1.0f);
        }
    }

    private void cancelarPedido() {
        if (pedidoAtual == null) return;

        String accessToken = getIntent().getStringExtra("access_token");
        if (accessToken == null) {
            Toast.makeText(this, "Erro: token de acesso não encontrado", Toast.LENGTH_SHORT).show();
            return;
        }

        orderManager.cancelOrder(pedidoAtual.getId(), accessToken, new SupabaseOrderManager.OrderCallback() {
            @Override
            public void onSuccess(Order order) {
                Toast.makeText(DetalhesPedidoActivity.this,
                        "Pedido cancelado com sucesso", Toast.LENGTH_SHORT).show();
                pedidoAtual = order;
                exibirDadosPedido(order);
            }

            @Override
            public void onError(String erro) {
                Toast.makeText(DetalhesPedidoActivity.this,
                        "Erro ao cancelar: " + erro, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String formatarData(long timestamp) {
        Date date = new Date(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(date);
    }



    private void definirCorStatus(String status) {
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
}