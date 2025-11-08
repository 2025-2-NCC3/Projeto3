package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import java.util.ArrayList;
import java.util.List;

public class MeusPedidosActivity extends AppCompatActivity {

    private ImageButton btnVoltar;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recyclerViewPedidos;
    private LinearLayout layoutPedidosVazio;
    private ProgressBar progressBarCarregando;
    private PedidosAdapter adapter;
    private List<Pedido> pedidos;

    private SupabasePedidoManager pedidoManager;
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
        pedidoManager = SupabasePedidoManager.getInstance(this);

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

        // Configurar cores do SwipeRefreshLayout (usando apenas cores existentes)
        swipeRefresh.setColorSchemeColors(
                getResources().getColor(R.color.dark_green),
                getResources().getColor(R.color.status_pendente),
                getResources().getColor(R.color.status_confirmado)
        );

        swipeRefresh.setOnRefreshListener(() -> carregarPedidos());
    }

    private void carregarPedidos() {
        if (!swipeRefresh.isRefreshing()) {
            progressBarCarregando.setVisibility(View.VISIBLE);
        }
        recyclerViewPedidos.setVisibility(View.GONE);
        layoutPedidosVazio.setVisibility(View.GONE);

        new Thread(() -> {
            pedidoManager.getStudentPedidos(studentId, accessToken,
                    new SupabasePedidoManager.PedidosCallback() {
                        @Override
                        public void onSuccess(List<Pedido> listaPedidos) {
                            runOnUiThread(() -> {
                                progressBarCarregando.setVisibility(View.GONE);
                                swipeRefresh.setRefreshing(false);

                                pedidos.clear();
                                pedidos.addAll(listaPedidos);
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
                                Toast.makeText(MeusPedidosActivity.this,
                                        "Erro: " + error,
                                        Toast.LENGTH_LONG).show();
                                layoutPedidosVazio.setVisibility(View.VISIBLE);
                            });
                        }
                    });
        }).start();
    }

    private class PedidosAdapter extends RecyclerView.Adapter<PedidosAdapter.ViewHolder> {
        private List<Pedido> pedidos;

        public PedidosAdapter(List<Pedido> pedidos) {
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
            Pedido pedido = pedidos.get(position);

            // Código do pedido
            holder.tvCodigoPedido.setText(pedido.getCode() != null ? pedido.getCode() : "N/A");

            // Data
            holder.tvDataPedido.setText(PedidoUtils.formatarData(pedido.getCreatedAt()));

            // Valor total
            holder.tvValorTotal.setText(PedidoUtils.formatarPreco(pedido.getTotal()));

            // Obter configuração do status (apenas 3 status: PENDING, COMPLETED, CANCELLED)
            PedidoUtils.StatusConfig statusConfig = PedidoUtils.getStatusConfig(
                    holder.itemView.getContext(),
                    pedido.getStatus()
            );

            holder.tvStatus.setText(statusConfig.texto);
            holder.tvStatus.setTextColor(statusConfig.corTexto);
            holder.tvStatusIcon.setText(statusConfig.icone);
            holder.tvStatusIcon.setTextColor(statusConfig.corTexto);
            holder.cardStatus.setCardBackgroundColor(statusConfig.corFundo);

            // Click listener para abrir detalhes
            holder.itemView.setOnClickListener(v -> abrirDetalhesPedido(pedido));
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

    private void abrirDetalhesPedido(Pedido pedido) {
        Intent intent = new Intent(this, DetalhesPedidoActivity.class);
        intent.putExtra("pedido_id", pedido.getId());
        intent.putExtra("access_token", accessToken);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Adicionar delay para não sobrecarregar
        new android.os.Handler().postDelayed(() -> {
            carregarPedidos();
        }, 300); // 300ms de delay
    }
}