package com.example.myapplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MeusPedidosActivity extends AppCompatActivity {
    private static final String TAG = "MeusPedidosActivity";

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

        Log.d(TAG, "onCreate - Iniciando MeusPedidosActivity");

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
            Log.e(TAG, "Usuário não está logado");
            Toast.makeText(this, "Faça login para ver seus pedidos", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        studentId = sessionManager.getUserId();
        accessToken = sessionManager.getAccessToken();

        Log.d(TAG, "Student ID: " + studentId);
        Log.d(TAG, "Token disponível: " + (accessToken != null ? "SIM" : "NÃO"));

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
        Log.d(TAG, "Carregando pedidos do estudante: " + studentId);

        if (!swipeRefresh.isRefreshing()) {
            progressBarCarregando.setVisibility(View.VISIBLE);
        }
        recyclerViewPedidos.setVisibility(View.GONE);
        layoutPedidosVazio.setVisibility(View.GONE);

        orderManager.getStudentOrders(studentId, accessToken,
                new SupabaseOrderManager.OrdersCallback() {
                    @Override
                    public void onSuccess(List<Order> orders) {
                        Log.d(TAG, "Pedidos carregados com sucesso: " + orders.size());

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
                        Log.e(TAG, "Erro ao carregar pedidos: " + error);

                        runOnUiThread(() -> {
                            progressBarCarregando.setVisibility(View.GONE);
                            swipeRefresh.setRefreshing(false);
                            Toast.makeText(MeusPedidosActivity.this,
                                    "Erro ao carregar pedidos: " + error,
                                    Toast.LENGTH_LONG).show();
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
            try {
                Order pedido = pedidos.get(position);

                Log.d(TAG, "Binding pedido position " + position + ": " + pedido.getId());

                // Código do pedido
                holder.tvCodigoPedido.setText(pedido.getCode() != null ? pedido.getCode() : "N/A");

                // CORRIGIDO: Usar getCreatedAtDate() em vez de getCreatedAt()
                Date dataPedido = pedido.getCreatedAtDate();
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale.getDefault());
                holder.tvDataPedido.setText(formatter.format(dataPedido));

                // Valor total
                holder.tvValorTotal.setText(String.format(Locale.getDefault(), "R$ %.2f", pedido.getTotal()));

                // Status
                String status = pedido.getStatus();
                holder.tvStatus.setText(PedidoUtils.getStatusText(status));
                holder.cardStatus.setCardBackgroundColor(PedidoUtils.getStatusColor(status));
                holder.tvStatusIcon.setText(PedidoUtils.getStatusIcon(status));

                // Click listener
                holder.itemView.setOnClickListener(v -> mostrarDetalhesPedido(pedido));

            } catch (Exception e) {
                Log.e(TAG, "Erro ao fazer bind do pedido na posição " + position, e);
            }
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
        try {
            StringBuilder detalhes = new StringBuilder();
            detalhes.append("📋 Código: ").append(pedido.getCode() != null ? pedido.getCode() : "N/A").append("\n\n");

            // CORRIGIDO: Usar getCreatedAtDate()
            Date dataPedido = pedido.getCreatedAtDate();
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale.getDefault());
            detalhes.append("📅 Data: ").append(formatter.format(dataPedido)).append("\n\n");

            detalhes.append("💰 Total: R$ ").append(String.format(Locale.getDefault(), "%.2f", pedido.getTotal())).append("\n\n");
            detalhes.append("📊 Status: ").append(PedidoUtils.getStatusText(pedido.getStatus()));

            new AlertDialog.Builder(this)
                    .setTitle("Detalhes do Pedido")
                    .setMessage(detalhes.toString())
                    .setPositiveButton("OK", null)
                    .show();

        } catch (Exception e) {
            Log.e(TAG, "Erro ao mostrar detalhes do pedido", e);
            Toast.makeText(this, "Erro ao exibir detalhes", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume - Recarregando pedidos");
        carregarPedidos();
    }
}


