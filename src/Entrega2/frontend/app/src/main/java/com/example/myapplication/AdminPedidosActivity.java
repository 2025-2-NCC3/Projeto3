package com.example.myapplication;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminPedidosActivity extends AppCompatActivity {

    private static final String TAG = "AdminPedidosActivity";

    private ImageButton btnVoltar;
    private EditText editBusca;
    private ImageButton btnLimparBusca;
    private Button btnFiltroStatus, btnFiltroData, btnOrdenacao;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recyclerViewPedidos;
    private ProgressBar progressBarCarregando;
    private LinearLayout layoutPedidosVazio;

    private AdminPedidosAdapter adapter;
    private List<Pedido> pedidosTodos;
    private List<Pedido> pedidosFiltrados;

    private SupabasePedidoManager pedidoManager;
    private SessionManager sessionManager;
    private AdminManager adminManager;
    private String accessToken;

    // Filtros
    private String filtroStatus = "TODOS";
    private Date filtroDataInicio = null;
    private Date filtroDataFim = null;
    private String ordenacao = "DATA_DESC";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Verificar permissões de admin
        sessionManager = SessionManager.getInstance(this);
        adminManager = AdminManager.getInstance(this);

        if (!adminManager.isAdmin()) {
            Toast.makeText(this, "Acesso negado. Você não tem permissões de administrador.",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_admin_pedidos);

        inicializarViews();
        inicializarDados();
        configurarRecyclerView();
        configurarListeners();
        carregarPedidos();
    }

    private void inicializarViews() {
        btnVoltar = findViewById(R.id.btnVoltar);
        editBusca = findViewById(R.id.editBusca);
        btnLimparBusca = findViewById(R.id.btnLimparBusca);
        btnFiltroStatus = findViewById(R.id.btnFiltroStatus);
        btnFiltroData = findViewById(R.id.btnFiltroData);
        btnOrdenacao = findViewById(R.id.btnOrdenacao);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        recyclerViewPedidos = findViewById(R.id.recyclerViewPedidos);
        progressBarCarregando = findViewById(R.id.progressBarCarregando);
        layoutPedidosVazio = findViewById(R.id.layoutPedidosVazio);
    }

    private void inicializarDados() {
        pedidoManager = SupabasePedidoManager.getInstance(this);
        accessToken = sessionManager.getAccessToken();

        pedidosTodos = new ArrayList<>();
        pedidosFiltrados = new ArrayList<>();
    }

    private void configurarRecyclerView() {
        recyclerViewPedidos.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminPedidosAdapter(pedidosFiltrados);
        recyclerViewPedidos.setAdapter(adapter);
    }

    private void configurarListeners() {
        btnVoltar.setOnClickListener(v -> finish());

        // Busca em tempo real
        editBusca.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnLimparBusca.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                aplicarFiltros();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnLimparBusca.setOnClickListener(v -> {
            editBusca.setText("");
            btnLimparBusca.setVisibility(View.GONE);
        });

        btnFiltroStatus.setOnClickListener(v -> mostrarDialogFiltroStatus());
        btnFiltroData.setOnClickListener(v -> mostrarDialogFiltroData());
        btnOrdenacao.setOnClickListener(v -> mostrarDialogOrdenacao());

        swipeRefresh.setOnRefreshListener(() -> carregarPedidos());
        swipeRefresh.setColorSchemeColors(
                getResources().getColor(R.color.dark_green),
                getResources().getColor(R.color.status_confirmado),
                getResources().getColor(R.color.status_concluido)
        );
    }

    private void carregarPedidos() {
        if (!swipeRefresh.isRefreshing()) {
            progressBarCarregando.setVisibility(View.VISIBLE);
        }
        recyclerViewPedidos.setVisibility(View.GONE);
        layoutPedidosVazio.setVisibility(View.GONE);

        pedidoManager.getAllPedidos(accessToken, new SupabasePedidoManager.PedidosCallback() {
            @Override
            public void onSuccess(List<Pedido> pedidos) {
                runOnUiThread(() -> {
                    progressBarCarregando.setVisibility(View.GONE);
                    swipeRefresh.setRefreshing(false);

                    pedidosTodos.clear();
                    pedidosTodos.addAll(pedidos);
                    aplicarFiltros();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    progressBarCarregando.setVisibility(View.GONE);
                    swipeRefresh.setRefreshing(false);
                    Toast.makeText(AdminPedidosActivity.this,
                            "Erro ao carregar pedidos: " + error,
                            Toast.LENGTH_LONG).show();
                    layoutPedidosVazio.setVisibility(View.VISIBLE);
                });
            }
        });
    }

    private void aplicarFiltros() {
        String textoBusca = editBusca.getText().toString().trim().toLowerCase();

        pedidosFiltrados.clear();

        for (Pedido pedido : pedidosTodos) {
            try {
                // Filtro de busca
                boolean passaBusca = textoBusca.isEmpty() ||
                        (pedido.getCode() != null && pedido.getCode().toLowerCase().contains(textoBusca)) ||
                        (pedido.getStudentName() != null && pedido.getStudentName().toLowerCase().contains(textoBusca)) ||
                        (pedido.getStudentId() != null && pedido.getStudentId().toLowerCase().contains(textoBusca));

                // Filtro de status usando normalização
                boolean passaStatus = filtroStatus.equals("TODOS") ||
                        PedidoUtils.normalizarStatus(pedido.getStatus()).equals(filtroStatus);

                // Filtro de data
                boolean passaData = true;
                if (filtroDataInicio != null && filtroDataFim != null && pedido.getCreatedAt() != null) {
                    Date dataPedido = pedido.getCreatedAt();
                    passaData = !dataPedido.before(filtroDataInicio) && !dataPedido.after(filtroDataFim);
                }

                if (passaBusca && passaStatus && passaData) {
                    pedidosFiltrados.add(pedido);
                }
            } catch (Exception e) {
                Log.e(TAG, "Erro ao filtrar pedido: " + e.getMessage());
            }
        }

        // Aplicar ordenação
        ordenarPedidos();

        adapter.notifyDataSetChanged();

        if (pedidosFiltrados.isEmpty()) {
            layoutPedidosVazio.setVisibility(View.VISIBLE);
            recyclerViewPedidos.setVisibility(View.GONE);
        } else {
            layoutPedidosVazio.setVisibility(View.GONE);
            recyclerViewPedidos.setVisibility(View.VISIBLE);
        }
    }

    private void ordenarPedidos() {
        Comparator<Pedido> comparator = null;

        switch (ordenacao) {
            case "DATA_DESC":
                comparator = (o1, o2) -> {
                    if (o1.getCreatedAt() == null) return 1;
                    if (o2.getCreatedAt() == null) return -1;
                    return o2.getCreatedAt().compareTo(o1.getCreatedAt());
                };
                break;
            case "DATA_ASC":
                comparator = (o1, o2) -> {
                    if (o1.getCreatedAt() == null) return 1;
                    if (o2.getCreatedAt() == null) return -1;
                    return o1.getCreatedAt().compareTo(o2.getCreatedAt());
                };
                break;
            case "VALOR_DESC":
                comparator = (o1, o2) -> Double.compare(o2.getTotal(), o1.getTotal());
                break;
            case "VALOR_ASC":
                comparator = (o1, o2) -> Double.compare(o1.getTotal(), o2.getTotal());
                break;
        }

        if (comparator != null) {
            try {
                Collections.sort(pedidosFiltrados, comparator);
            } catch (Exception e) {
                Log.e(TAG, "Erro ao ordenar pedidos: " + e.getMessage());
            }
        }
    }

    private void mostrarDialogFiltroStatus() {
        String[] opcoes = {"Todos", "Pendente", "Confirmado", "Concluído", "Cancelado"};
        String[] valores = {"TODOS", "PENDING", "CONFIRMED", "COMPLETED", "CANCELLED"};

        int itemSelecionado = 0;
        for (int i = 0; i < valores.length; i++) {
            if (valores[i].equals(filtroStatus)) {
                itemSelecionado = i;
                break;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("Filtrar por Status")
                .setSingleChoiceItems(opcoes, itemSelecionado, (dialog, which) -> {
                    filtroStatus = valores[which];
                    btnFiltroStatus.setText("🎯 " + opcoes[which]);
                    aplicarFiltros();
                    dialog.dismiss();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void mostrarDialogFiltroData() {
        String[] opcoes = {"Hoje", "Última semana", "Último mês", "Período personalizado", "Limpar filtro"};

        new AlertDialog.Builder(this)
                .setTitle("Filtrar por Data")
                .setItems(opcoes, (dialog, which) -> {
                    Calendar cal = Calendar.getInstance();
                    Date hoje = cal.getTime();

                    switch (which) {
                        case 0: // Hoje
                            cal.set(Calendar.HOUR_OF_DAY, 0);
                            cal.set(Calendar.MINUTE, 0);
                            cal.set(Calendar.SECOND, 0);
                            filtroDataInicio = cal.getTime();
                            filtroDataFim = hoje;
                            btnFiltroData.setText("📅 Hoje");
                            break;
                        case 1: // Última semana
                            cal.add(Calendar.DAY_OF_MONTH, -7);
                            filtroDataInicio = cal.getTime();
                            filtroDataFim = hoje;
                            btnFiltroData.setText("📅 7 dias");
                            break;
                        case 2: // Último mês
                            cal.add(Calendar.MONTH, -1);
                            filtroDataInicio = cal.getTime();
                            filtroDataFim = hoje;
                            btnFiltroData.setText("📅 30 dias");
                            break;
                        case 3: // Personalizado
                            selecionarPeriodoPersonalizado();
                            return;
                        case 4: // Limpar
                            filtroDataInicio = null;
                            filtroDataFim = null;
                            btnFiltroData.setText("📅 Data");
                            break;
                    }
                    aplicarFiltros();
                })
                .show();
    }

    private void selecionarPeriodoPersonalizado() {
        Calendar cal = Calendar.getInstance();

        DatePickerDialog pickerInicio = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    Calendar inicio = Calendar.getInstance();
                    inicio.set(year, month, dayOfMonth, 0, 0, 0);
                    filtroDataInicio = inicio.getTime();

                    DatePickerDialog pickerFim = new DatePickerDialog(this,
                            (view2, year2, month2, dayOfMonth2) -> {
                                Calendar fim = Calendar.getInstance();
                                fim.set(year2, month2, dayOfMonth2, 23, 59, 59);
                                filtroDataFim = fim.getTime();

                                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());
                                btnFiltroData.setText("📅 " + sdf.format(filtroDataInicio) + " - " + sdf.format(filtroDataFim));
                                aplicarFiltros();
                            },
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH),
                            cal.get(Calendar.DAY_OF_MONTH));
                    pickerFim.setTitle("Data Final");
                    pickerFim.show();
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH));
        pickerInicio.setTitle("Data Inicial");
        pickerInicio.show();
    }

    private void mostrarDialogOrdenacao() {
        String[] opcoes = {"Mais recentes", "Mais antigos", "Maior valor", "Menor valor"};
        String[] valores = {"DATA_DESC", "DATA_ASC", "VALOR_DESC", "VALOR_ASC"};

        int itemSelecionado = 0;
        for (int i = 0; i < valores.length; i++) {
            if (valores[i].equals(ordenacao)) {
                itemSelecionado = i;
                break;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("Ordenar por")
                .setSingleChoiceItems(opcoes, itemSelecionado, (dialog, which) -> {
                    ordenacao = valores[which];
                    btnOrdenacao.setText("📊 " + opcoes[which]);
                    aplicarFiltros();
                    dialog.dismiss();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // ============================================
    // ADAPTER
    // ============================================

    private class AdminPedidosAdapter extends RecyclerView.Adapter<AdminPedidosAdapter.ViewHolder> {
        private List<Pedido> pedidos;

        public AdminPedidosAdapter(List<Pedido> pedidos) {
            this.pedidos = pedidos;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_pedido_admin, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            if (position >= pedidos.size()) return;

            try {
                Pedido pedido = pedidos.get(position);

                // Proteger contra valores null
                holder.tvCodigoPedido.setText(pedido.getCode() != null ? pedido.getCode() : "N/A");

                holder.tvNomeAluno.setText(pedido.getStudentName() != null ?
                        pedido.getStudentName() : "Aluno ID: " + pedido.getStudentId());

                holder.tvDataPedido.setText(pedido.getCreatedAt() != null ?
                        PedidoUtils.formatarData(pedido.getCreatedAt()) : "Data não disponível");

                holder.tvValorTotal.setText(PedidoUtils.formatarPreco(pedido.getTotal()));

                // Configurar status
                String status = pedido.getStatus() != null ? pedido.getStatus() : "PENDING";
                PedidoUtils.StatusConfig statusConfig = PedidoUtils.getStatusConfig(
                        holder.itemView.getContext(),
                        status
                );

                holder.tvStatus.setText(statusConfig.texto);
                holder.tvStatus.setTextColor(statusConfig.corTexto);
                holder.cardStatus.setCardBackgroundColor(statusConfig.corFundo);
                holder.tvStatusIcon.setText(statusConfig.icone);
                holder.tvStatusIcon.setTextColor(statusConfig.corTexto);

                // Click no card abre AdminPedidoDetalhesActivity
                holder.itemView.setOnClickListener(v -> {
                    Intent intent = new Intent(AdminPedidosActivity.this, AdminPedidoDetalhesActivity.class);
                    intent.putExtra("ORDER_ID", pedido.getId());
                    startActivity(intent);
                });

            } catch (Exception e) {
                Log.e(TAG, "Erro ao fazer bind do pedido na posição " + position + ": " + e.getMessage(), e);
            }
        }

        @Override
        public int getItemCount() {
            return pedidos.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvCodigoPedido, tvNomeAluno, tvDataPedido, tvValorTotal, tvStatus, tvStatusIcon;  // ✅ ADICIONE tvNomeAluno AQUI
            CardView cardStatus;

            public ViewHolder(View itemView) {
                super(itemView);
                tvCodigoPedido = itemView.findViewById(R.id.tvCodigoPedido);
                tvNomeAluno = itemView.findViewById(R.id.tvNomeAluno);
                tvDataPedido = itemView.findViewById(R.id.tvDataPedido);
                tvValorTotal = itemView.findViewById(R.id.tvValorTotal);
                tvStatus = itemView.findViewById(R.id.tvStatus);
                tvStatusIcon = itemView.findViewById(R.id.tvStatusIcon);
                cardStatus = itemView.findViewById(R.id.cardStatus);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        new android.os.Handler().postDelayed(() -> {
            carregarPedidos();
        }, 300);
    }
}