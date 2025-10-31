package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.List;
import java.util.Locale;

public class CriarPedidoActivity extends AppCompatActivity {

    private ImageButton btnVoltar;
    private TextView tvNomeCliente, tvIdCliente, tvQuantidadeItens;
    private TextView tvListaProdutos, tvValorTotal;
    private EditText etObservacoes;
    private MaterialCardView cardStatus;
    private ProgressBar progressBar;
    private TextView tvStatus;
    private MaterialButton btnCriarPedido;

    private CarrinhoHelper carrinhoHelper;
    private SupabasePedidoManager orderManager;
    private String studentId, studentName, accessToken;
    private AlertDialog currentDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_criar_pedido);

        inicializarViews();

        // Verificar condições antes de inicializar
        if (!inicializarDados()) {
            return; // Sai se houver erro
        }

        if (!carregarDadosCarrinho()) {
            return; // Sai se carrinho estiver vazio
        }

        configurarListeners();
    }

    private void inicializarViews() {
        btnVoltar = findViewById(R.id.btnVoltar);
        tvNomeCliente = findViewById(R.id.tvNomeCliente);
        tvIdCliente = findViewById(R.id.tvIdCliente);
        tvQuantidadeItens = findViewById(R.id.tvQuantidadeItens);
        tvListaProdutos = findViewById(R.id.tvListaProdutos);
        tvValorTotal = findViewById(R.id.tvValorTotal);
        etObservacoes = findViewById(R.id.etObservacoes);
        cardStatus = findViewById(R.id.cardStatus);
        progressBar = findViewById(R.id.progressBar);
        tvStatus = findViewById(R.id.tvStatus);
        btnCriarPedido = findViewById(R.id.btnCriarPedido);
    }

    private boolean inicializarDados() {
        carrinhoHelper = CarrinhoHelper.getInstance(this);
        orderManager = SupabasePedidoManager.getInstance(this);

        // Usar SessionManager em vez de SharedPreferences direto
        SessionManager sessionManager = SessionManager.getInstance(this);

        if (!sessionManager.isLoggedIn()) {
            showDialogAndFinish("Não Autenticado", "Você precisa fazer login.");
            return false;
        }

        studentId = sessionManager.getUserId();
        studentName = sessionManager.getUserEmail(); // ou criar um método getUserName() no SessionManager
        accessToken = sessionManager.getAccessToken();

        tvNomeCliente.setText(studentName);
        tvIdCliente.setText(studentId);

        if (accessToken == null || accessToken.isEmpty()) {
            showDialogAndFinish("Token Inválido", "Sessão expirada. Faça login novamente.");
            return false;
        }

        return true;
    }

    private boolean carregarDadosCarrinho() {
        if (carrinhoHelper.isEmpty()) {
            showDialogAndFinish("Carrinho Vazio", "Adicione produtos ao carrinho.");
            return false;
        }

        List<ItemCarrinho> itens = carrinhoHelper.getItens();
        double valorTotal = carrinhoHelper.getSubtotal();

        tvQuantidadeItens.setText(String.valueOf(itens.size()));
        tvValorTotal.setText(String.format(Locale.getDefault(), "R$ %.2f", valorTotal));

        StringBuilder listaProdutos = new StringBuilder();
        for (ItemCarrinho item : itens) {
            listaProdutos.append("• ")
                    .append(item.getProduto().getNome())
                    .append(" x")
                    .append(item.getQuantidade())
                    .append("\n");
        }
        tvListaProdutos.setText(listaProdutos.toString().trim());

        return true;
    }

    private void configurarListeners() {
        btnVoltar.setOnClickListener(v -> finish());
        btnCriarPedido.setOnClickListener(v -> mostrarDialogoConfirmacao());
    }

    private void showDialogAndFinish(String title, String message) {
        if (!isFinishing() && !isDestroyed()) {
            currentDialog = new AlertDialog.Builder(this)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton("OK", (dialog, which) -> finish())
                    .setCancelable(false)
                    .create();
            currentDialog.show();
        } else {
            finish();
        }
    }

    private void mostrarDialogoConfirmacao() {
        if (isFinishing() || isDestroyed()) return;

        String mensagem = "Deseja confirmar o pedido?\n\n" +
                "Valor Total: " + tvValorTotal.getText() + "\n" +
                "Quantidade: " + tvQuantidadeItens.getText() + " itens";

        currentDialog = new AlertDialog.Builder(this)
                .setTitle("Confirmar Pedido")
                .setMessage(mensagem)
                .setPositiveButton("Sim, Confirmar", (dialog, which) -> criarPedido())
                .setNegativeButton("Cancelar", null)
                .create();
        currentDialog.show();
    }

    private void criarPedido() {
        String erroEstoque = carrinhoHelper.validarEstoque();
        if (erroEstoque != null) {
            if (isFinishing() || isDestroyed()) return;

            currentDialog = new AlertDialog.Builder(this)
                    .setTitle("Estoque Insuficiente")
                    .setMessage(erroEstoque)
                    .setPositiveButton("OK", null)
                    .create();
            currentDialog.show();
            return;
        }

        mostrarLoading(true, "Criando pedido...");

        PedidoRequest request = carrinhoHelper.criarOrderRequest(studentId, studentName);

        orderManager.createOrder(request, accessToken, new SupabasePedidoManager.OrderCallback() {
            @Override
            public void onSuccess(Pedido pedido) {
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;

                    mostrarLoading(false, "");
                    carrinhoHelper.limparCarrinho();
                    mostrarDialogoSucesso(pedido);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;

                    mostrarLoading(false, "");
                    mostrarDialogoErro(error);
                });
            }
        });
    }

    private void mostrarLoading(boolean mostrar, String mensagem) {
        if (mostrar) {
            cardStatus.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            tvStatus.setText(mensagem);
            btnCriarPedido.setEnabled(false);
            btnCriarPedido.setText("Processando...");
        } else {
            cardStatus.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            btnCriarPedido.setEnabled(true);
            btnCriarPedido.setText("✅ CONFIRMAR PEDIDO");
        }
    }

    private void mostrarDialogoSucesso(Pedido pedido) {
        if (isFinishing() || isDestroyed()) return;

        String mensagem = "🎉 Pedido realizado com sucesso!\n\n" +
                "━━━━━━━━━━━━━━━━━━━━\n" +
                "📝 Código: " + pedido.getCode() + "\n" +
                "💰 Total: R$ " + String.format(Locale.getDefault(), "%.2f", pedido.getTotal()) + "\n" +
                "📊 Status: " + pedido.getStatus() + "\n" +
                "━━━━━━━━━━━━━━━━━━━━\n\n" +
                "Acompanhe em 'Meus Pedidos'.";

        currentDialog = new AlertDialog.Builder(this)
                .setTitle("✅ Pedido Criado!")
                .setMessage(mensagem)
                .setPositiveButton("Ver Meus Pedidos", (dialog, which) -> {
                    Intent intent = new Intent(this, MeusPedidosActivity.class);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Voltar", (dialog, which) -> finish())
                .setCancelable(false)
                .create();
        currentDialog.show();
    }

    private void mostrarDialogoErro(String erro) {
        if (isFinishing() || isDestroyed()) return;

        String mensagem, titulo;

        if (erro.contains("Estoque insuficiente")) {
            titulo = "❌ Produto Esgotado";
            mensagem = erro + "\n\nAjuste as quantidades no carrinho.";
        } else if (erro.contains("conexão")) {
            titulo = "🌐 Sem Conexão";
            mensagem = "Não foi possível conectar.\n\nVerifique sua internet.";
        } else {
            titulo = "❌ Erro ao Criar Pedido";
            mensagem = erro;
        }

        currentDialog = new AlertDialog.Builder(this)
                .setTitle(titulo)
                .setMessage(mensagem)
                .setPositiveButton("Tentar Novamente", (dialog, which) -> criarPedido())
                .setNegativeButton("Cancelar", null)
                .create();
        currentDialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (carrinhoHelper != null && !carrinhoHelper.isEmpty()) {
            carregarDadosCarrinho();
        }
    }

    @Override
    public void onBackPressed() {
        if (isFinishing() || isDestroyed()) {
            super.onBackPressed();
            return;
        }

        currentDialog = new AlertDialog.Builder(this)
                .setTitle("Cancelar Pedido?")
                .setMessage("Deseja cancelar a criação do pedido?")
                .setPositiveButton("Sim", (dialog, which) -> super.onBackPressed())
                .setNegativeButton("Não", null)
                .create();
        currentDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Fechar qualquer diálogo aberto para evitar window leak
        if (currentDialog != null && currentDialog.isShowing()) {
            currentDialog.dismiss();
        }
    }
}