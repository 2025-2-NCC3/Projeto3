package com.example.myapplication;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.ScrollView;
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
    private SupabasePedidoManager pedidoManager;

    private TextView tvOrderId, tvStudentName, tvOrderDate, tvOrderStatus, tvOrderCode, tvOrderTotal;
    private RecyclerView recyclerViewItems;
    private Button btnConfirmarRetirada, btnCancelarPedido;
    private ProgressBar progressBar;
    private ScrollView layoutContent;
    private androidx.swiperefreshlayout.widget.SwipeRefreshLayout swipeRefreshDetalhes;

    private Pedido currentPedido;
    private String pedidoId;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("pt", "BR"));
    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_pedido_detalhes);

        Log.d(TAG, "onCreate: Iniciando");

        sessionManager = SessionManager.getInstance(this);
        pedidoManager = SupabasePedidoManager.getInstance(this);

        initializeViews();
        loadPedidoDetails();
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
        swipeRefreshDetalhes.setOnRefreshListener(this::loadPedidoDetails);

        Log.d(TAG, "Views inicializadas");
    }

    private void loadPedidoDetails() {
        pedidoId = getIntent().getStringExtra("ORDER_ID");
        if (pedidoId == null) {
            Toast.makeText(this, "ID do pedido inválido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d(TAG, "Carregando pedido: " + pedidoId);

        if (!swipeRefreshDetalhes.isRefreshing()) {
            showLoading();
        }

        String token = sessionManager.getAccessToken();
        pedidoManager.getPedidoById(pedidoId, token, new SupabasePedidoManager.PedidoCallback() {
            @Override
            public void onSuccess(Pedido pedido) {
                Log.d(TAG, "Pedido carregado com sucesso");
                runOnUiThread(() -> {
                    hideLoading();
                    swipeRefreshDetalhes.setRefreshing(false);
                    currentPedido = pedido;
                    displayPedidoDetails(pedido);
                });
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Erro ao carregar: " + error);
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

    private void displayPedidoDetails(Pedido pedido) {
        Log.d(TAG, "Exibindo detalhes");

        tvOrderId.setText("Pedido #" + pedido.getId().substring(0, Math.min(8, pedido.getId().length())));
        tvStudentName.setText(pedido.getStudentName() != null ? pedido.getStudentName() : "Aluno ID: " + pedido.getStudentId());
        tvOrderDate.setText("Data: " + dateFormat.format(pedido.getCreatedAt()));

        // Status com emoji e cor
        String statusText = PedidoUtils.getStatusIcon(pedido.getStatus()) + " " + PedidoUtils.getStatusText(pedido.getStatus());
        tvOrderStatus.setText(statusText);
        tvOrderStatus.setTextColor(PedidoUtils.getStatusColor(this, pedido.getStatus()));

        tvOrderCode.setText("Código de Retirada: " + (pedido.getCode() != null ? pedido.getCode() : "N/A"));
        tvOrderTotal.setText("Total: " + currencyFormat.format(pedido.getTotal()));

        // Configurar adapter dos itens
        PedidoItemAdapter itemAdapter = new PedidoItemAdapter(this);
        if (pedido.getItems() != null && !pedido.getItems().isEmpty()) {
            itemAdapter.atualizarItens(pedido.getItems());
        }
        recyclerViewItems.setAdapter(itemAdapter);

        // Configurar botões baseado no status (apenas 3 status: PENDING, COMPLETED, CANCELLED)
        updateButtonsVisibility(pedido.getStatus());
    }

    private void updateButtonsVisibility(String status) {
        String statusNormalizado = PedidoUtils.normalizarStatus(status);

        switch (statusNormalizado) {
            case "PENDING":
                // Pedido pendente: pode confirmar retirada ou cancelar
                btnConfirmarRetirada.setVisibility(View.VISIBLE);
                btnCancelarPedido.setVisibility(View.VISIBLE);
                btnConfirmarRetirada.setEnabled(true);
                btnCancelarPedido.setEnabled(true);
                break;

            case "COMPLETED":
            case "CANCELLED":
                // Pedido já finalizado: esconde botões
                btnConfirmarRetirada.setVisibility(View.GONE);
                btnCancelarPedido.setVisibility(View.GONE);
                break;

            default:
                btnConfirmarRetirada.setVisibility(View.GONE);
                btnCancelarPedido.setVisibility(View.GONE);
                break;
        }

        Log.d(TAG, "Botões atualizados para status: " + statusNormalizado);
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
        pedidoManager.confirmPedidoPickup(currentPedido.getId(), token, new SupabasePedidoManager.PedidoCallback() {
            @Override
            public void onSuccess(Pedido pedido) {
                Log.d(TAG, "Retirada confirmada");
                runOnUiThread(() -> {
                    hideLoading();
                    Toast.makeText(AdminPedidoDetalhesActivity.this,
                            "✓ Retirada confirmada com sucesso!", Toast.LENGTH_SHORT).show();
                    currentPedido = pedido;
                    displayPedidoDetails(pedido);
                    loadPedidoDetails();
                });
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Erro ao confirmar: " + error);
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
                .setMessage("Tem certeza que deseja cancelar este pedido?\n\nEsta ação não pode ser desfeita.")
                .setPositiveButton("Sim, cancelar", (dialog, which) -> {
                    realizarCancelamento();
                })
                .setNegativeButton("Não", null)
                .show();
    }

    private void realizarCancelamento() {
        showLoading();
        btnCancelarPedido.setEnabled(false);

        String token = sessionManager.getAccessToken();
        pedidoManager.cancelPedido(currentPedido.getId(), token, new SupabasePedidoManager.PedidoCallback() {
            @Override
            public void onSuccess(Pedido pedido) {
                Log.d(TAG, "Pedido cancelado");
                runOnUiThread(() -> {
                    hideLoading();
                    Toast.makeText(AdminPedidoDetalhesActivity.this,
                            "✓ Pedido cancelado com sucesso!", Toast.LENGTH_SHORT).show();
                    currentPedido = pedido;
                    displayPedidoDetails(pedido);
                    loadPedidoDetails();
                });
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Erro ao cancelar: " + error);
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