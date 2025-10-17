package com.example.myapplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
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

    private ImageButton btnVoltar;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recyclerViewPedidos;
    private LinearLayout layoutPedidosVazio;
    private ProgressBar progressBarCarregando;
    private PedidosAdapter adapter;
    private List<Order> pedidos;
    private SupabaseOrderManager orderManager;
    private String studentId, accessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meus_pedidos);

        inicializarViews();
        inicializarDados();
        configurarRecyclerView();
        configurarListeners();
        carregarPedidos();
    }

    private void inicializarViews() {
        btnVoltar = findViewById(R.id.btnVoltar);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        recyclerViewPedidos = findViewById(R.id.recyclerViewPedidos);
        layoutPedidosVazio = findViewById(R.id.layoutPedidosVazio);
        progressBarCarregando = findViewById(R.id.progressBarCarregando);
    }

    private void inicializarDados() {
        orderManager = SupabaseOrderManager.getInstance(this);

        // Usar SessionManager
        SessionManager sessionManager = SessionManager.getInstance(this);

        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Faça login para ver seus pedidos", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        studentId = sessionManager.getUserId();
        accessToken = sessionManager.getAccessToken();
        pedidos = new ArrayList<>();
    }

    private void configurarRecyclerView() {
        recyclerViewPedidos.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PedidosAdapter(pedidos);
        recyclerViewPedidos.setAdapter(adapter);
    }

    private void configurarListeners() {
        btnVoltar.setOnClickListener(v -> finish());
        swipeRefresh.setOnRefreshListener(() -> carregarPedidos());
    }

    private void carregarPedidos() {
        if (!swipeRefresh.isRefreshing()) {
            progressBarCarregando.setVisibility(View.VISIBLE);
        }
        recyclerViewPedidos.setVisibility(View.GONE);
        layoutPedidosVazio.setVisibility(View.GONE);

        orderManager.getStudentOrders(studentId, accessToken,
                new SupabaseOrderManager.OrdersCallback() {
                    @Override
                    public void onSuccess(List<Order> orders) {
                        runOnUiThread(() -> {
                            progressBarCarregando.setVisibility(View.GONE);
                            swipeRefresh.setRefreshing(false);

                            pedidos.clear();
                            pedidos.addAll(orders);
                            adapter.notifyDataSetChanged();

                            if (pedidos.isEmpty()) {
                                layoutPedidosVazio.setVisibility(View.VISIBLE);
                                recyclerViewPedidos.setVisibility(View.GONE);
                            } else {
                                layoutPedidosVazio.setVisibility(View.GONE);
                                recyclerViewPedidos.setVisibility(View.VISIBLE);
                            }
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            progressBarCarregando.setVisibility(View.GONE);
                            swipeRefresh.setRefreshing(false);
                            Toast.makeText(MeusPedidosActivity.this, "Erro: " + error, Toast.LENGTH_LONG).show();
                            layoutPedidosVazio.setVisibility(View.VISIBLE);
                        });
                    }
                });
    }

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

            holder.tvCodigoPedido.setText(pedido.getCode());

            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale.getDefault());
            holder.tvDataPedido.setText(formatter.format(pedido.getCreatedAt()));

            holder.tvValorTotal.setText(String.format(Locale.getDefault(), "R$ %.2f", pedido.getTotal()));

            String status = pedido.getStatus();
            holder.tvStatus.setText(PedidoUtils.getStatusText(status));
            holder.cardStatus.setCardBackgroundColor(PedidoUtils.getStatusColor(status));
            holder.tvStatusIcon.setText(PedidoUtils.getStatusIcon(status));

            holder.itemView.setOnClickListener(v -> mostrarDetalhesPedido(pedido));
        }

        @Override
        public int getItemCount() {
            return pedidos.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvCodigoPedido, tvDataPedido, tvValorTotal, tvStatus, tvStatusIcon;
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

    private void mostrarDetalhesPedido(Order pedido) {
        StringBuilder detalhes = new StringBuilder();
        detalhes.append("📋 Código: ").append(pedido.getCode()).append("\n\n");

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale.getDefault());
        detalhes.append("📅 Data: ").append(formatter.format(pedido.getCreatedAt())).append("\n\n");
        detalhes.append("💰 Total: R$ ").append(String.format(Locale.getDefault(), "%.2f", pedido.getTotal())).append("\n\n");
        detalhes.append("📊 Status: ").append(PedidoUtils.getStatusText(pedido.getStatus()));

        new AlertDialog.Builder(this)
                .setTitle("Detalhes do Pedido")
                .setMessage(detalhes.toString())
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarPedidos();
    }
}