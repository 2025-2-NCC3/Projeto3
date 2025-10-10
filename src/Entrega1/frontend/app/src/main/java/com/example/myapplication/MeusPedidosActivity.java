// MeusPedidosActivity.java
package com.example.myapplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MeusPedidosActivity extends AppCompatActivity {

    // Views
    private ImageButton btnVoltar;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recyclerViewPedidos;
    private TextView tvPedidosVazio;
    private ProgressBar progressBarCarregando;

    // Adapter
    private PedidosAdapter adapter;
    private List<Order> pedidos;

    // Manager
    private SupabaseOrderManager orderManager;

    // Dados do usuário
    private String studentId;
    private String accessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meus_pedidos);

        inicializarViews();
        inicializarDados();
        configurarRecyclerView();
        configurarListeners();

        // Carregar pedidos
        carregarPedidos();
    }

    private void inicializarViews() {
        btnVoltar = findViewById(R.id.btnVoltar);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        recyclerViewPedidos = findViewById(R.id.recyclerViewPedidos);
        tvPedidosVazio = findViewById(R.id.tvPedidosVazio);
        progressBarCarregando = findViewById(R.id.progressBarCarregando);
    }

    private void inicializarDados() {
        // Inicializar manager
        orderManager = SupabaseOrderManager.getInstance(this);

        // Obter dados do usuário
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        studentId = prefs.getString("student_id", "2023001");
        accessToken = prefs.getString("access_token", "");

        // Lista de pedidos
        pedidos = new ArrayList<>();
    }

    private void configurarRecyclerView() {
        recyclerViewPedidos.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PedidosAdapter(pedidos);
        recyclerViewPedidos.setAdapter(adapter);
    }

    private void configurarListeners() {
        btnVoltar.setOnClickListener(v -> finish());

        // Pull to refresh
        swipeRefresh.setOnRefreshListener(() -> {
            carregarPedidos();
        });
    }

    private void carregarPedidos() {
        // Mostrar loading
        if (!swipeRefresh.isRefreshing()) {
            progressBarCarregando.setVisibility(View.VISIBLE);
        }
        recyclerViewPedidos.setVisibility(View.GONE);
        tvPedidosVazio.setVisibility(View.GONE);

        // Buscar pedidos do Supabase
        orderManager.getStudentOrders(studentId, accessToken,
                new SupabaseOrderManager.OrdersCallback() {
                    @Override
                    public void onSuccess(List<Order> orders) {
                        runOnUiThread(() -> {
                            // Esconder loading
                            progressBarCarregando.setVisibility(View.GONE);
                            swipeRefresh.setRefreshing(false);

                            // Atualizar lista
                            pedidos.clear();
                            pedidos.addAll(orders);
                            adapter.notifyDataSetChanged();

                            // Mostrar/esconder mensagem de vazio
                            if (pedidos.isEmpty()) {
                                tvPedidosVazio.setVisibility(View.VISIBLE);
                                recyclerViewPedidos.setVisibility(View.GONE);
                            } else {
                                tvPedidosVazio.setVisibility(View.GONE);
                                recyclerViewPedidos.setVisibility(View.VISIBLE);
                            }
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            // Esconder loading
                            progressBarCarregando.setVisibility(View.GONE);
                            swipeRefresh.setRefreshing(false);

                            // Mostrar erro
                            Toast.makeText(MeusPedidosActivity.this,
                                    "Erro ao carregar pedidos: " + error,
                                    Toast.LENGTH_LONG).show();

                            // Mostrar mensagem de vazio
                            tvPedidosVazio.setVisibility(View.VISIBLE);
                            tvPedidosVazio.setText("Erro ao carregar pedidos.\nPuxe para baixo para tentar novamente.");
                        });
                    }
                });
    }

    // Adapter do RecyclerView
    private class PedidosAdapter extends RecyclerView.Adapter<PedidosAdapter.ViewHolder> {
        private List<Order> pedidos;

        public PedidosAdapter(List<Order> pedidos) {
            this.pedidos = pedidos;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_pedido, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Order pedido = pedidos.get(position);

            // Código do pedido
            holder.tvCodigoPedido.setText(pedido.getCode());

            // Data
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale.getDefault());
            String dataFormatada = formatter.format(pedido.getCreatedAt());
            holder.tvDataPedido.setText(dataFormatada);

            // Valor total
            holder.tvValorTotal.setText(String.format(Locale.getDefault(),
                    "R$ %.2f", pedido.getTotal()));

            // Status
            String status = pedido.getStatus();
            holder.tvStatus.setText(getStatusTexto(status));
            holder.cardStatus.setCardBackgroundColor(getStatusCor(status));

            // Ícone do status
            holder.tvStatusIcon.setText(getStatusIcon(status));

            // Click para ver detalhes
            holder.itemView.setOnClickListener(v -> {
                // Mostrar detalhes do pedido
                mostrarDetalhesPedido(pedido);
            });
        }

        @Override
        public int getItemCount() {
            return pedidos.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvCodigoPedido;
            TextView tvDataPedido;
            TextView tvValorTotal;
            TextView tvStatus;
            TextView tvStatusIcon;
            CardView cardStatus;

            public ViewHolder(View itemView) {
                super(itemView);
                tvCodigoPedido = itemView.findViewById(R.id.tvCodigoPedido);
                tvDataPedido = itemView.findViewById(R.id.tvDataPedido);
                tvValorTotal = itemView.findViewById(R.id.tvValorTotal);
                tvStatus = itemView.findViewById(R.id.tvStatus);
                tvStatusIcon = itemView.findViewById(R.id.tvStatusIcon);
                cardStatus = itemView.findViewById(R.id.cardStatus);
            }
        }
    }

    // Métodos auxiliares para status
    private String getStatusTexto(String status) {
        switch (status) {
            case "PENDENTE":
                return "Pendente";
            case "CONFIRMADO":
                return "Confirmado";
            case "PREPARANDO":
                return "Preparando";
            case "PRONTO":
                return "Pronto";
            case "ENTREGUE":
                return "Entregue";
            case "CANCELADO":
                return "Cancelado";
            default:
                return status;
        }
    }

    private String getStatusIcon(String status) {
        switch (status) {
            case "PENDENTE":
                return "⏳";
            case "CONFIRMADO":
                return "✅";
            case "PREPARANDO":
                return "👨‍🍳";
            case "PRONTO":
                return "🔔";
            case "ENTREGUE":
                return "✨";
            case "CANCELADO":
                return "❌";
            default:
                return "📦";
        }
    }

    private int getStatusCor(String status) {
        switch (status) {
            case "PENDENTE":
                return 0xFFFFF9C4; // Amarelo claro
            case "CONFIRMADO":
                return 0xFFC8E6C9; // Verde claro
            case "PREPARANDO":
                return 0xFFFFE0B2; // Laranja claro
            case "PRONTO":
                return 0xFF81C784; // Verde
            case "ENTREGUE":
                return 0xFFB2DFDB; // Azul claro
            case "CANCELADO":
                return 0xFFFFCDD2; // Vermelho claro
            default:
                return 0xFFE0E0E0; // Cinza
        }
    }

    private void mostrarDetalhesPedido(Order pedido) {
        // Criar mensagem com detalhes
        StringBuilder detalhes = new StringBuilder();
        detalhes.append("📋 Código: ").append(pedido.getCode()).append("\n\n");

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale.getDefault());
        detalhes.append("📅 Data: ").append(formatter.format(pedido.getCreatedAt())).append("\n\n");

        detalhes.append("💰 Valor Total: R$ ")
                .append(String.format(Locale.getDefault(), "%.2f", pedido.getTotal()))
                .append("\n\n");

        detalhes.append("📊 Status: ").append(getStatusTexto(pedido.getStatus())).append("\n\n");

        // Itens do pedido (se disponível)
        if (pedido.getItems() != null && !pedido.getItems().isEmpty()) {
            detalhes.append("🛒 Itens:\n");
            for (OrderItem item : pedido.getItems()) {
                detalhes.append("  • ")
                        .append(item.getProductName())
                        .append(" x")
                        .append(item.getQuantity())
                        .append(" - R$ ")
                        .append(String.format(Locale.getDefault(), "%.2f", item.getSubtotal()))
                        .append("\n");
            }
        }

        // Mostrar diálogo
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Detalhes do Pedido")
                .setMessage(detalhes.toString())
                .setPositiveButton("OK", null)
                .setNeutralButton("Cancelar Pedido", (dialog, which) -> {
                    // TODO: Implementar cancelamento
                    Toast.makeText(this, "Funcionalidade em desenvolvimento", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recarregar pedidos ao voltar para a tela
        carregarPedidos();
    }
}