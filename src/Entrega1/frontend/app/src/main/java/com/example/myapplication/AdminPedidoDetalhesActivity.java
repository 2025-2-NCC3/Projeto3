package com.example.myapplication;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class AdminPedidoDetalhesActivity extends AppCompatActivity {

    private static final String TAG = "AdminPedidoDetalhes";

    private SessionManager sessionManager;
    private SupabaseOrderManager orderManager;

    private TextView tvOrderId, tvStudentName, tvOrderDate, tvOrderStatus, tvOrderCode, tvOrderTotal;
    private RecyclerView recyclerViewItems;
    private Button btnConfirmarRetirada, btnCancelarPedido;
    private ProgressBar progressBar;
    private LinearLayout layoutContent;
    private androidx.swiperefreshlayout.widget.SwipeRefreshLayout swipeRefreshDetalhes;

    private Order currentOrder;
    private String orderId;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("pt", "BR"));
    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_pedido_detalhes);

        sessionManager = SessionManager.getInstance(this);
        orderManager = SupabaseOrderManager.getInstance(this);

        initializeViews();
        loadOrderDetails();
    }

    private void initializeViews() {
        ImageButton btnVoltar = findViewById(R.id.btnVoltar);
        btnVoltar.setOnClickListener(v -> finish());

        tvOrderId = findViewById(R.id.tvOrderId);
        tvStudentName = findViewById(R.id.tvStudentName);
        tvOrderDate = findViewById(R.id.tvOrderDate);
        tvOrderStatus = findViewById(R.id.tvOrderStatus);
        tvOrderCode = findViewById(R.id.tvOrderCode);
        tvOrderTotal = findViewById(R.id.tvOrderTotal);

        recyclerViewItems = findViewById(R.id.recyclerViewItems);
        recyclerViewItems.setLayoutManager(new LinearLayoutManager(this));

        btnConfirmarRetirada = findViewById(R.id.btnConfirmarRetirada);
        btnCancelarPedido = findViewById(R.id.btnCancelarPedido);
        progressBar = findViewById(R.id.progressBar);
        layoutContent = findViewById(R.id.layoutContent);
        swipeRefreshDetalhes = findViewById(R.id.swipeRefreshDetalhes);

        btnConfirmarRetirada.setOnClickListener(v -> confirmarRetirada());
        btnCancelarPedido.setOnClickListener(v -> cancelarPedido());
        swipeRefreshDetalhes.setOnRefreshListener(this::loadOrderDetails);
    }

    private void loadOrderDetails() {
        orderId = getIntent().getStringExtra("ORDER_ID");
        if (orderId == null) {
            Toast.makeText(this, "ID do pedido inválido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (!swipeRefreshDetalhes.isRefreshing()) {
            showLoading();
        }

        String token = sessionManager.getAccessToken();
        orderManager.getOrderById(orderId, token, new SupabaseOrderManager.OrderCallback() {
            @Override
            public void onSuccess(Order order) {
                runOnUiThread(() -> {
                    hideLoading();
                    swipeRefreshDetalhes.setRefreshing(false);
                    currentOrder = order;
                    displayOrderDetails(order);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    hideLoading();
                    swipeRefreshDetalhes.setRefreshing(false);
                    Toast.makeText(AdminPedidoDetalhesActivity.this,
                            "Erro ao carregar pedido: " + error, Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }

    private void displayOrderDetails(Order order) {
        tvOrderId.setText("Pedido #" + order.getId());
        tvStudentName.setText(order.getStudentName() != null ? order.getStudentName() : "Aluno ID: " + order.getStudentId());
        tvOrderDate.setText("Data: " + dateFormat.format(order.getCreatedAt()));

        // Status com emoji e cor
        String statusText = getStatusText(order.getStatus());
        tvOrderStatus.setText(statusText);
        tvOrderStatus.setTextColor(getStatusColor(order.getStatus()));

        tvOrderCode.setText("Código de Retirada: " + (order.getCode() != null ? order.getCode() : "N/A"));
        tvOrderTotal.setText("Total: " + currencyFormat.format(order.getTotal()));

        // Configurar adapter dos itens
        OrderItemAdapter itemAdapter = new OrderItemAdapter(this);
        itemAdapter.atualizarItens(order.getItems());
        recyclerViewItems.setAdapter(itemAdapter);

        // Configurar botões baseado no status
        updateButtonsVisibility(order.getStatus());
    }

    private String getStatusText(String status) {
        String statusUpper = status.toUpperCase();
        switch (statusUpper) {
            case "PENDENTE": return "⏳ Pendente";
            case "PREPARANDO": return "👨‍🍳 Preparando";
            case "PRONTO": return "✅ Pronto";
            case "CONFIRMADO": return "✅ Confirmado";
            case "ENTREGUE": return "🎉 Entregue";
            case "RETIRADO": return "🎉 Retirado";
            case "CANCELADO": return "❌ Cancelado";
            default: return status;
        }
    }

    private int getStatusColor(String status) {
        String statusUpper = status.toUpperCase();
        switch (statusUpper) {
            case "PENDENTE": return 0xFFFF9800; // Laranja
            case "PREPARANDO": return 0xFF2196F3; // Azul
            case "PRONTO":
            case "CONFIRMADO": return 0xFF4CAF50; // Verde
            case "ENTREGUE":
            case "RETIRADO": return 0xFF009688; // Verde-azulado
            case "CANCELADO": return 0xFFF44336; // Vermelho
            default: return 0xFF757575; // Cinza
        }
    }

    private void updateButtonsVisibility(String status) {
        String statusUpper = status.toUpperCase();
        switch (statusUpper) {
            case "PENDENTE":
            case "PREPARANDO":
            case "PRONTO":
            case "CONFIRMADO":
                btnConfirmarRetirada.setVisibility(View.VISIBLE);
                btnCancelarPedido.setVisibility(View.VISIBLE);
                btnConfirmarRetirada.setEnabled(true);
                btnCancelarPedido.setEnabled(true);
                break;
            case "RETIRADO":
            case "ENTREGUE":
                btnConfirmarRetirada.setVisibility(View.GONE);
                btnCancelarPedido.setVisibility(View.GONE);
                break;
            case "CANCELADO":
                btnConfirmarRetirada.setVisibility(View.GONE);
                btnCancelarPedido.setVisibility(View.GONE);
                break;
        }
    }

    private void confirmarRetirada() {
        new AlertDialog.Builder(this)
                .setTitle("Confirmar Retirada")
                .setMessage("Confirma que o aluno retirou o pedido?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    realizarConfirmacaoRetirada();
                })
                .setNegativeButton("Não", null)
                .show();
    }

    private void realizarConfirmacaoRetirada() {
        showLoading();
        btnConfirmarRetirada.setEnabled(false);

        String token = sessionManager.getAccessToken();
        orderManager.confirmOrderPickup(currentOrder.getId(), token, new SupabaseOrderManager.OrderCallback() {
            @Override
            public void onSuccess(Order order) {
                runOnUiThread(() -> {
                    hideLoading();
                    Toast.makeText(AdminPedidoDetalhesActivity.this,
                            "Retirada confirmada com sucesso!", Toast.LENGTH_SHORT).show();
                    currentOrder = order;
                    displayOrderDetails(order);

                    // Recarregar dados atualizados do servidor
                    loadOrderDetails();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    hideLoading();
                    btnConfirmarRetirada.setEnabled(true);
                    Toast.makeText(AdminPedidoDetalhesActivity.this,
                            "Erro ao confirmar retirada: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void cancelarPedido() {
        new AlertDialog.Builder(this)
                .setTitle("Cancelar Pedido")
                .setMessage("Tem certeza que deseja cancelar este pedido?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    realizarCancelamento();
                })
                .setNegativeButton("Não", null)
                .show();
    }

    private void realizarCancelamento() {
        showLoading();
        btnCancelarPedido.setEnabled(false);

        String token = sessionManager.getAccessToken();
        orderManager.cancelOrder(currentOrder.getId(), token, new SupabaseOrderManager.OrderCallback() {
            @Override
            public void onSuccess(Order order) {
                runOnUiThread(() -> {
                    hideLoading();
                    Toast.makeText(AdminPedidoDetalhesActivity.this,
                            "Pedido cancelado com sucesso!", Toast.LENGTH_SHORT).show();
                    currentOrder = order;
                    displayOrderDetails(order);

                    // Recarregar dados atualizados do servidor
                    loadOrderDetails();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    hideLoading();
                    btnCancelarPedido.setEnabled(true);
                    Toast.makeText(AdminPedidoDetalhesActivity.this,
                            "Erro ao cancelar pedido: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        layoutContent.setVisibility(View.GONE);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
        layoutContent.setVisibility(View.VISIBLE);
    }
}