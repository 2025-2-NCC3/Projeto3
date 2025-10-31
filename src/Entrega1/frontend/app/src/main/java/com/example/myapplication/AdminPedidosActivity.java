package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AdminPedidosActivity extends AppCompatActivity {

    private static final String TAG = "AdminPedidosActivity";

    private SessionManager sessionManager;
    private AdminManager adminManager;
    private SupabasePedidoManager orderManager;

    private RecyclerView recyclerViewPedidos;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private LinearLayout layoutVazio;
    private EditText editBusca;
    private ImageButton btnLimparBusca;
    private PedidoAdminAdapter adapter;

    private Button btnFiltroStatus, btnFiltroData, btnOrdenacao;

    private String filtroAtual = "TODOS";
    private String filtroData = "TODOS";
    private String ordenacao = "RECENTE";

    private List<Pedido> todosOsPedidos = new ArrayList<>();
    private List<Pedido> pedidosFiltrados = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = SessionManager.getInstance(this);
        adminManager = AdminManager.getInstance(this);
        orderManager = SupabasePedidoManager.getInstance(this);

        if (!adminManager.isAdmin()) {
            Toast.makeText(this, "Acesso negado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_admin_pedidos);

        initializeViews();
        setupListeners();
        loadOrders();
    }

    private void initializeViews() {
        ImageButton btnVoltar = findViewById(R.id.btnVoltar);
        btnVoltar.setOnClickListener(v -> finish());

        recyclerViewPedidos = findViewById(R.id.recyclerViewPedidos);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        progressBar = findViewById(R.id.progressBarCarregando);
        layoutVazio = findViewById(R.id.layoutPedidosVazio);

        editBusca = findViewById(R.id.editBusca);
        btnLimparBusca = findViewById(R.id.btnLimparBusca);
        btnFiltroStatus = findViewById(R.id.btnFiltroStatus);
        btnFiltroData = findViewById(R.id.btnFiltroData);
        btnOrdenacao = findViewById(R.id.btnOrdenacao);

        recyclerViewPedidos.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PedidoAdminAdapter(new ArrayList<>(), order -> {
            Intent intent = new Intent(AdminPedidosActivity.this, AdminPedidoDetalhesActivity.class);
            intent.putExtra("ORDER_ID", order.getId());
            startActivity(intent);
        });
        recyclerViewPedidos.setAdapter(adapter);
    }

    private void setupListeners() {
        swipeRefresh.setOnRefreshListener(this::loadOrders);

        btnFiltroStatus.setOnClickListener(v -> mostrarDialogoFiltroStatus());
        btnFiltroData.setOnClickListener(v -> mostrarDialogoFiltroData());
        btnOrdenacao.setOnClickListener(v -> mostrarDialogoOrdenacao());

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

        btnLimparBusca.setOnClickListener(v -> editBusca.setText(""));
    }

    private void mostrarDialogoFiltroStatus() {
        String[] opcoes = {
                "Todos os Status",
                "⏳ Pendente",
                "👨‍🍳 Preparando",
                "✅ Pronto",
                "🎉 Entregue/Retirado",
                "❌ Cancelado"
        };

        int selecionado = 0;
        switch (filtroAtual.toUpperCase()) {
            case "PENDENTE": selecionado = 1; break;
            case "PREPARANDO": selecionado = 2; break;
            case "PRONTO": selecionado = 3; break;
            case "ENTREGUE":
            case "RETIRADO": selecionado = 4; break;
            case "CANCELADO": selecionado = 5; break;
        }

        new AlertDialog.Builder(this)
                .setTitle("Filtrar por Status")
                .setSingleChoiceItems(opcoes, selecionado, (dialog, which) -> {
                    switch (which) {
                        case 0: filtroAtual = "TODOS"; break;
                        case 1: filtroAtual = "PENDENTE"; break;
                        case 2: filtroAtual = "PREPARANDO"; break;
                        case 3: filtroAtual = "PRONTO"; break;
                        case 4: filtroAtual = "ENTREGUE"; break;
                        case 5: filtroAtual = "CANCELADO"; break;
                    }
                    btnFiltroStatus.setText(opcoes[which]);
                    aplicarFiltros();
                    dialog.dismiss();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void mostrarDialogoFiltroData() {
        String[] opcoes = {"Todos os Períodos", "Hoje", "Ontem", "Última Semana"};
        int selecionado = 0;

        switch (filtroData) {
            case "HOJE": selecionado = 1; break;
            case "ONTEM": selecionado = 2; break;
            case "SEMANA": selecionado = 3; break;
        }

        new AlertDialog.Builder(this)
                .setTitle("Filtrar por Data")
                .setSingleChoiceItems(opcoes, selecionado, (dialog, which) -> {
                    switch (which) {
                        case 0: filtroData = "TODOS"; break;
                        case 1: filtroData = "HOJE"; break;
                        case 2: filtroData = "ONTEM"; break;
                        case 3: filtroData = "SEMANA"; break;
                    }
                    btnFiltroData.setText("📅 " + opcoes[which]);
                    aplicarFiltros();
                    dialog.dismiss();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void mostrarDialogoOrdenacao() {
        String[] opcoes = {"Mais Recentes", "Mais Antigos", "Maior Valor", "Menor Valor"};
        int selecionado = 0;

        switch (ordenacao) {
            case "ANTIGO": selecionado = 1; break;
            case "VALOR_DESC": selecionado = 2; break;
            case "VALOR_ASC": selecionado = 3; break;
        }

        new AlertDialog.Builder(this)
                .setTitle("Ordenar por")
                .setSingleChoiceItems(opcoes, selecionado, (dialog, which) -> {
                    switch (which) {
                        case 0: ordenacao = "RECENTE"; break;
                        case 1: ordenacao = "ANTIGO"; break;
                        case 2: ordenacao = "VALOR_DESC"; break;
                        case 3: ordenacao = "VALOR_ASC"; break;
                    }
                    btnOrdenacao.setText("📊 " + opcoes[which]);
                    aplicarFiltros();
                    dialog.dismiss();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void loadOrders() {
        showLoading();

        String token = sessionManager.getAccessToken();
        if (token == null) {
            Toast.makeText(this, "Sessão expirada", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        orderManager.getAllOrders(token, new SupabasePedidoManager.OrdersCallback() {
            @Override
            public void onSuccess(List<Pedido> pedidos) {
                runOnUiThread(() -> {
                    hideLoading();
                    todosOsPedidos = pedidos;
                    aplicarFiltros();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    hideLoading();
                    Toast.makeText(AdminPedidosActivity.this, "Erro: " + error, Toast.LENGTH_SHORT).show();
                    showEmptyState();
                });
            }
        });
    }

    private void aplicarFiltros() {
        pedidosFiltrados = new ArrayList<>(todosOsPedidos);
        String textoBusca = editBusca.getText().toString().toLowerCase().trim();

        // 1. Filtrar por Status
        if (!filtroAtual.equals("TODOS")) {
            List<Pedido> temp = new ArrayList<>();
            for (Pedido pedido : pedidosFiltrados) {
                String status = pedido.getStatus().toUpperCase();
                if (filtroAtual.equals("ENTREGUE") && (status.equals("ENTREGUE") || status.equals("RETIRADO"))) {
                    temp.add(pedido);
                } else if (status.equals(filtroAtual)) {
                    temp.add(pedido);
                }
            }
            pedidosFiltrados = temp;
        }

        // 2. Filtrar por Data
        if (!filtroData.equals("TODOS")) {
            List<Pedido> temp = new ArrayList<>();
            Calendar cal = Calendar.getInstance();

            for (Pedido pedido : pedidosFiltrados) {
                Calendar orderCal = Calendar.getInstance();
                orderCal.setTime(pedido.getCreatedAt());

                boolean incluir = false;

                switch (filtroData) {
                    case "HOJE":
                        incluir = isSameDay(cal, orderCal);
                        break;
                    case "ONTEM":
                        cal.add(Calendar.DAY_OF_MONTH, -1);
                        incluir = isSameDay(cal, orderCal);
                        break;
                    case "SEMANA":
                        cal.add(Calendar.DAY_OF_MONTH, -7);
                        incluir = orderCal.after(cal);
                        break;
                }

                if (incluir) temp.add(pedido);
            }
            pedidosFiltrados = temp;
        }

        // 3. Filtrar por Busca
        if (!textoBusca.isEmpty()) {
            List<Pedido> temp = new ArrayList<>();
            for (Pedido pedido : pedidosFiltrados) {
                String codigo = pedido.getCode() != null ? pedido.getCode().toLowerCase() : "";
                String nome = pedido.getStudentName() != null ? pedido.getStudentName().toLowerCase() : "";
                String id = pedido.getId().toLowerCase();

                if (codigo.contains(textoBusca) ||
                        nome.contains(textoBusca) ||
                        id.contains(textoBusca)) {
                    temp.add(pedido);
                }
            }
            pedidosFiltrados = temp;
        }

        // 4. Ordenar
        Collections.sort(pedidosFiltrados, new Comparator<Pedido>() {
            @Override
            public int compare(Pedido o1, Pedido o2) {
                switch (ordenacao) {
                    case "RECENTE":
                        return o2.getCreatedAt().compareTo(o1.getCreatedAt());
                    case "ANTIGO":
                        return o1.getCreatedAt().compareTo(o2.getCreatedAt());
                    case "VALOR_DESC":
                        return Double.compare(o2.getTotal(), o1.getTotal());
                    case "VALOR_ASC":
                        return Double.compare(o1.getTotal(), o2.getTotal());
                    default:
                        return 0;
                }
            }
        });

        // 5. Atualizar UI
        if (pedidosFiltrados.isEmpty()) {
            showEmptyState();
        } else {
            showOrders(pedidosFiltrados);
        }
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerViewPedidos.setVisibility(View.GONE);
        layoutVazio.setVisibility(View.GONE);
        swipeRefresh.setRefreshing(false);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
        swipeRefresh.setRefreshing(false);
    }

    private void showOrders(List<Pedido> pedidos) {
        recyclerViewPedidos.setVisibility(View.VISIBLE);
        layoutVazio.setVisibility(View.GONE);
        adapter.updateOrders(pedidos);
    }

    private void showEmptyState() {
        recyclerViewPedidos.setVisibility(View.GONE);
        layoutVazio.setVisibility(View.VISIBLE);
        adapter.updateOrders(new ArrayList<>());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrders();
    }
}