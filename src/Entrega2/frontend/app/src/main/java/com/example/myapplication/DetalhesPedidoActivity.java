package com.example.myapplication;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class DetalhesPedidoActivity extends AppCompatActivity {

    private TextView tvPedidoCodigo, tvPedidoId, tvDataPedido, tvStatus, tvClienteNome;
    private TextView tvDesconto, tvTotalPedido;
    private MaterialButton btnCancelarPedido;
    private ImageButton btnVoltar;
    private RecyclerView recyclerViewItens;
    private PedidoItemAdapter adapter;
    private Pedido pedidoAtual;
    private SupabasePedidoManager pedidoManager;
    private String accessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes_pedido);

        inicializarComponentes();
        carregarDadosPedido();
    }

    private void inicializarComponentes() {
        // Views do layout
        tvPedidoCodigo = findViewById(R.id.tv_pedido_codigo);
        tvPedidoId = findViewById(R.id.tv_pedido_id);
        tvDataPedido = findViewById(R.id.tv_data_pedido);
        tvStatus = findViewById(R.id.tv_status);
        tvClienteNome = findViewById(R.id.tv_cliente_nome);
        tvDesconto = findViewById(R.id.tv_desconto);
        tvTotalPedido = findViewById(R.id.tv_total_pedido);
        btnCancelarPedido = findViewById(R.id.btn_cancelar_pedido);
        btnVoltar = findViewById(R.id.btn_voltar);
        recyclerViewItens = findViewById(R.id.recycler_itens_pedido);

        pedidoManager = SupabasePedidoManager.getInstance(this);

        // Configurar RecyclerView
        recyclerViewItens.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PedidoItemAdapter(this);
        recyclerViewItens.setAdapter(adapter);

        // Listeners
        btnCancelarPedido.setOnClickListener(v -> confirmarCancelamento());
        btnVoltar.setOnClickListener(v -> finish());
    }

    private void carregarDadosPedido() {
        // Receber dados da Intent
        String pedidoId = getIntent().getStringExtra("pedido_id");
        accessToken = getIntent().getStringExtra("access_token");

        if (pedidoId == null || pedidoId.isEmpty() || accessToken == null) {
            Toast.makeText(this, "Erro ao carregar pedido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Buscar pedido no Supabase
        pedidoManager.getPedidoById(pedidoId, accessToken, new SupabasePedidoManager.PedidoCallback() {
            @Override
            public void onSuccess(Pedido pedido) {
                runOnUiThread(() -> {
                    pedidoAtual = pedido;
                    exibirDadosPedido(pedido);
                });
            }

            @Override
            public void onError(String erro) {
                runOnUiThread(() -> {
                    Toast.makeText(DetalhesPedidoActivity.this,
                            "Erro ao carregar: " + erro, Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }

    private void exibirDadosPedido(Pedido pedido) {
        // Formatar e exibir dados
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault());

        // Código do pedido no header e na lista
        String codigo = pedido.getCode() != null ? "#" + pedido.getCode() :
                "#PED-" + (pedido.getId().length() > 8 ? pedido.getId().substring(0, 8) : pedido.getId());

        tvPedidoCodigo.setText(codigo);
        tvPedidoId.setText(codigo);

        // Data e hora
        tvDataPedido.setText(dateFormat.format(pedido.getCreatedAt()));

        // Usando StatusConfig
        PedidoUtils.StatusConfig statusConfig = PedidoUtils.getStatusConfig(this, pedido.getStatus());
        tvStatus.setText(statusConfig.texto);
        tvStatus.setTextColor(statusConfig.corTexto);

        // Cliente
        tvClienteNome.setText(pedido.getStudentName());

        // Desconto (por enquanto sempre 0)
        tvDesconto.setText("R$ 0,00");

        // Total
        tvTotalPedido.setText(PedidoUtils.formatarPreco(pedido.getTotal()));

        // Itens do pedido
        adapter.atualizarItens(pedido.getItems());

        // Controlar visibilidade do botão cancelar
        configurarBotaoCancelar(pedido);
    }

    private void configurarBotaoCancelar(Pedido pedido) {
        if (PedidoUtils.podeCancelarPedido(pedido)) {
            btnCancelarPedido.setEnabled(true);
            btnCancelarPedido.setAlpha(1.0f);
            btnCancelarPedido.setText("CANCELAR PEDIDO");
        } else {
            btnCancelarPedido.setEnabled(false);
            btnCancelarPedido.setAlpha(0.5f);
            btnCancelarPedido.setText("PEDIDO " + PedidoUtils.getStatusText(pedido.getStatus()));
        }
    }

    private void confirmarCancelamento() {
        if (pedidoAtual == null) return;

        String codigo = pedidoAtual.getCode() != null ? pedidoAtual.getCode() : "N/A";

        new AlertDialog.Builder(this)
                .setTitle("Cancelar Pedido")
                .setMessage("Tem certeza que deseja cancelar este pedido?\n\nCódigo: " + codigo)
                .setPositiveButton("Sim, Cancelar", (dialog, which) -> cancelarPedido())
                .setNegativeButton("Não", null)
                .show();
    }

    private void cancelarPedido() {
        if (pedidoAtual == null || accessToken == null) return;

        // Desabilitar botão durante o processo
        btnCancelarPedido.setEnabled(false);
        btnCancelarPedido.setText("CANCELANDO...");

        pedidoManager.cancelPedido(pedidoAtual.getId(), accessToken,
                new SupabasePedidoManager.PedidoCallback() {
                    @Override
                    public void onSuccess(Pedido pedido) {
                        runOnUiThread(() -> {
                            Toast.makeText(DetalhesPedidoActivity.this,
                                    "✅ Pedido cancelado com sucesso", Toast.LENGTH_SHORT).show();
                            pedidoAtual = pedido;
                            exibirDadosPedido(pedido);
                        });
                    }

                    @Override
                    public void onError(String erro) {
                        runOnUiThread(() -> {
                            Toast.makeText(DetalhesPedidoActivity.this,
                                    "❌ Erro ao cancelar: " + erro, Toast.LENGTH_SHORT).show();

                            // Reabilitar botão em caso de erro
                            btnCancelarPedido.setEnabled(true);
                            btnCancelarPedido.setText("CANCELAR PEDIDO");
                        });
                    }
                });
    }
}