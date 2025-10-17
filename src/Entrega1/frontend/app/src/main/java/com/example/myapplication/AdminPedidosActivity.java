package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AdminPedidosActivity extends AppCompatActivity {

    private static final String TAG = "AdminPedidos";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private RecyclerView recyclerPedidos;
    private AdminPedidosAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private TabLayout tabLayout;
    private List<OrderResponse> todosPedidos = new ArrayList<>();
    private String filtroAtual = "PENDENTE";

    private SupabaseClient supabaseClient;
    private SessionManager sessionManager;
    private Gson gson;
    private String accessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_pedidos);

        // Inicializar
        supabaseClient = SupabaseClient.getInstance(this);
        sessionManager = SessionManager.getInstance(this);
        gson = new Gson();
        accessToken = sessionManager.getAccessToken();

        // Verificar se é admin
        if (!AdminManager.getInstance(this).isAdmin()) {
            Toast.makeText(this, "Acesso negado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupRecyclerView();
        setupTabs();
        carregarPedidos();
    }

    private void initViews() {
        recyclerPedidos = findViewById(R.id.recyclerPedidos);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        tabLayout = findViewById(R.id.tabLayout);

        swipeRefresh.setOnRefreshListener(this::carregarPedidos);
    }

    private void setupRecyclerView() {
        adapter = new AdminPedidosAdapter(this, new ArrayList<>(), new AdminPedidosAdapter.OnPedidoClickListener() {
            @Override
            public void onConfirmarRetirada(OrderResponse pedido) {
                confirmarRetirada(pedido);
            }

            @Override
            public void onCancelarPedido(OrderResponse pedido) {
                cancelarPedido(pedido);
            }

            @Override
            public void onVerDetalhes(OrderResponse pedido) {
                carregarDetalhesPedido(pedido);
            }
        });

        recyclerPedidos.setLayoutManager(new LinearLayoutManager(this));
        recyclerPedidos.setAdapter(adapter);
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Pendentes"));
        tabLayout.addTab(tabLayout.newTab().setText("Confirmados"));
        tabLayout.addTab(tabLayout.newTab().setText("Prontos"));
        tabLayout.addTab(tabLayout.newTab().setText("Retirados"));
        tabLayout.addTab(tabLayout.newTab().setText("Cancelados"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        filtroAtual = "PENDENTE";
                        break;
                    case 1:
                        filtroAtual = "CONFIRMADO";
                        break;
                    case 2:
                        filtroAtual = "PRONTO";
                        break;
                    case 3:
                        filtroAtual = "RETIRADO";
                        break;
                    case 4:
                        filtroAtual = "CANCELADO";
                        break;
                }
                filtrarPedidos();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                carregarPedidos();
            }
        });
    }

    private void carregarPedidos() {
        Request request = new Request.Builder()
                .url(BuildConfig.SUPABASE_URL + "/rest/v1/pedidos?select=*&order=created_at.desc")
                .get()
                .addHeader("apikey", BuildConfig.SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Content-Type", "application/json")
                .build();

        Log.d(TAG, "Carregando pedidos...");

        supabaseClient.client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (!call.isCanceled()) {
                    Log.e(TAG, "Erro ao carregar pedidos", e);
                    runOnUiThread(() -> {
                        Toast.makeText(AdminPedidosActivity.this, "Erro ao carregar pedidos", Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (call.isCanceled()) return;

                String responseBody = response.body() != null ? response.body().string() : "";
                Log.d(TAG, "Resposta pedidos - Código: " + response.code());

                if (response.isSuccessful()) {
                    try {
                        Type listType = new TypeToken<List<OrderResponse>>(){}.getType();
                        List<OrderResponse> pedidos = gson.fromJson(responseBody, listType);

                        todosPedidos.clear();
                        if (pedidos != null) {
                            todosPedidos.addAll(pedidos);
                        }

                        runOnUiThread(() -> {
                            filtrarPedidos();
                            swipeRefresh.setRefreshing(false);
                        });
                    } catch (Exception e) {
                        Log.e(TAG, "Erro ao processar pedidos", e);
                        runOnUiThread(() -> {
                            Toast.makeText(AdminPedidosActivity.this, "Erro ao processar dados", Toast.LENGTH_SHORT).show();
                            swipeRefresh.setRefreshing(false);
                        });
                    }
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(AdminPedidosActivity.this, "Erro: " + response.code(), Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);
                    });
                }
            }
        });
    }

    private void filtrarPedidos() {
        List<OrderResponse> pedidosFiltrados = new ArrayList<>();
        for (OrderResponse pedido : todosPedidos) {
            if (pedido.status.equals(filtroAtual)) {
                pedidosFiltrados.add(pedido);
            }
        }
        adapter.atualizarLista(pedidosFiltrados);
    }

    private void carregarDetalhesPedido(OrderResponse pedido) {
        Request request = new Request.Builder()
                .url(BuildConfig.SUPABASE_URL + "/rest/v1/itens_pedido?pedido_id=eq." + pedido.id + "&select=*")
                .get()
                .addHeader("apikey", BuildConfig.SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        supabaseClient.client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Erro ao carregar itens", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body() != null ? response.body().string() : "";
                    Type listType = new TypeToken<List<OrderItemResponse>>(){}.getType();
                    List<OrderItemResponse> itens = gson.fromJson(responseBody, listType);

                    runOnUiThread(() -> {
                        mostrarDialogDetalhes(pedido, itens);
                    });
                }
            }
        });
    }

    private void mostrarDialogDetalhes(OrderResponse pedido, List<OrderItemResponse> itens) {
        StringBuilder detalhes = new StringBuilder();
        detalhes.append("Código: ").append(pedido.code).append("\n");
        detalhes.append("Cliente: ").append(pedido.student_name).append("\n\n");
        detalhes.append("Itens:\n");

        for (OrderItemResponse item : itens) {
            detalhes.append("• ").append(item.product_name)
                    .append(" - ").append(item.quantity).append("x")
                    .append(" - R$ ").append(String.format("%.2f", item.subtotal))
                    .append("\n");
        }

        detalhes.append("\nTotal: R$ ").append(String.format("%.2f", pedido.total_amount));

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Detalhes do Pedido")
                .setMessage(detalhes.toString())
                .setPositiveButton("OK", null)
                .show();
    }

    private void confirmarRetirada(OrderResponse pedido) {
        String novoStatus = "RETIRADO";
        atualizarStatusPedido(pedido.id, novoStatus, "Pedido marcado como retirado");
    }

    private void cancelarPedido(OrderResponse pedido) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Cancelar Pedido")
                .setMessage("Tem certeza que deseja cancelar este pedido?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    atualizarStatusPedido(pedido.id, "CANCELADO", "Pedido cancelado");
                })
                .setNegativeButton("Não", null)
                .show();
    }

    private void atualizarStatusPedido(int pedidoId, String novoStatus, String mensagemSucesso) {
        String json = "{\"status\":\"" + novoStatus + "\"}";
        RequestBody body = RequestBody.create(json, JSON);

        Request request = new Request.Builder()
                .url(BuildConfig.SUPABASE_URL + "/rest/v1/pedidos?id=eq." + pedidoId)
                .patch(body)
                .addHeader("apikey", BuildConfig.SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Content-Type", "application/json")
                .build();

        supabaseClient.client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (!call.isCanceled()) {
                    Log.e(TAG, "Erro ao atualizar pedido", e);
                    runOnUiThread(() ->
                            Toast.makeText(AdminPedidosActivity.this, "Erro ao atualizar", Toast.LENGTH_SHORT).show()
                    );
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (call.isCanceled()) return;

                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(AdminPedidosActivity.this, mensagemSucesso, Toast.LENGTH_SHORT).show();
                        carregarPedidos();
                    } else {
                        Toast.makeText(AdminPedidosActivity.this, "Erro: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    // Classes de resposta do Supabase
    public static class OrderResponse {
        public int id;
        public String student_id;
        public String student_name;
        public String status;
        public double total_amount;
        public String code;
        public String created_at;

        // Getters para compatibilidade
        public String getStudentName() { return student_name; }
        public double getTotalAmount() { return total_amount; }
        public String getCode() { return code; }
        public String getCreatedAt() { return created_at; }
    }

    public static class OrderItemResponse {
        public int id;
        public int pedido_id;
        public int product_id;
        public String product_name;
        public int quantity;
        public double price;
        public double subtotal;

        // Getters
        public String getProductName() { return product_name; }
        public int getQuantity() { return quantity; }
        public double getSubtotal() { return subtotal; }
    }
}