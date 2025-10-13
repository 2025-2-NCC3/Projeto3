package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import java.util.List;
import java.util.Locale;

public class CriarPedidoActivity extends AppCompatActivity {

    private ImageButton btnVoltar;
    private TextView tvNomeCliente, tvIdCliente, tvQuantidadeItens;
    private TextView tvListaProdutos, tvValorTotal;
    private EditText etObservacoes;
    private CardView cardStatus;
    private ProgressBar progressBar;
    private TextView tvStatus;
    private Button btnCriarPedido;

    private CarrinhoHelper carrinhoHelper;
    private SupabaseOrderManager orderManager;
    private String studentId, studentName, accessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_criar_pedido);

        inicializarViews();
        inicializarDados();
        carregarDadosCarrinho();
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

    private void inicializarDados() {
        carrinhoHelper = CarrinhoHelper.getInstance(this);
        orderManager = SupabaseOrderManager.getInstance(this);

        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        studentId = prefs.getString("student_id", "2023001");
        studentName = prefs.getString("student_name", "Cliente Cantina");
        accessToken = prefs.getString("access_token", "");

        tvNomeCliente.setText(studentName);
        tvIdCliente.setText(studentId);

        if (accessToken.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle("Não Autenticado")
                    .setMessage("Você precisa fazer login.")
                    .setPositiveButton("OK", (dialog, which) -> finish())
                    .setCancelable(false)
                    .show();
        }
    }

    private void carregarDadosCarrinho() {
        if (carrinhoHelper.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle("Carrinho Vazio")
                    .setMessage("Adicione produtos ao carrinho.")
                    .setPositiveButton("OK", (dialog, which) -> finish())
                    .setCancelable(false)
                    .show();
            return;
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
    }

    private void configurarListeners() {
        btnVoltar.setOnClickListener(v -> finish());
        btnCriarPedido.setOnClickListener(v -> mostrarDialogoConfirmacao());
    }

    private void mostrarDialogoConfirmacao() {
        String mensagem = "Deseja confirmar o pedido?\n\n" +
                "Valor Total: " + tvValorTotal.getText() + "\n" +
                "Quantidade: " + tvQuantidadeItens.getText() + " itens";

        new AlertDialog.Builder(this)
                .setTitle("Confirmar Pedido")
                .setMessage(mensagem)
                .setPositiveButton("Sim, Confirmar", (dialog, which) -> criarPedido())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void criarPedido() {
        String erroEstoque = carrinhoHelper.validarEstoque();
        if (erroEstoque != null) {
            new AlertDialog.Builder(this)
                    .setTitle("Estoque Insuficiente")
                    .setMessage(erroEstoque)
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        mostrarLoading(true, "Criando pedido...");

        OrderRequest request = carrinhoHelper.criarOrderRequest(studentId, studentName);

        orderManager.createOrder(request, accessToken, new SupabaseOrderManager.OrderCallback() {
            @Override
            public void onSuccess(Order order) {
                runOnUiThread(() -> {
                    mostrarLoading(false, "");
                    carrinhoHelper.limparCarrinho();
                    mostrarDialogoSucesso(order);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
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
            btnCriarPedido.setText("✅ Confirmar Pedido");
        }
    }

    private void mostrarDialogoSucesso(Order order) {
        String mensagem = "🎉 Pedido realizado com sucesso!\n\n" +
                "━━━━━━━━━━━━━━━━━━━━\n" +
                "📝 Código: " + order.getCode() + "\n" +
                "💰 Total: R$ " + String.format(Locale.getDefault(), "%.2f", order.getTotal()) + "\n" +
                "📊 Status: " + order.getStatus() + "\n" +
                "━━━━━━━━━━━━━━━━━━━━\n\n" +
                "Acompanhe em 'Meus Pedidos'.";

        new AlertDialog.Builder(this)
                .setTitle("✅ Pedido Criado!")
                .setMessage(mensagem)
                .setPositiveButton("Ver Meus Pedidos", (dialog, which) -> {
                    Intent intent = new Intent(this, MeusPedidosActivity.class);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Voltar", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    private void mostrarDialogoErro(String erro) {
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

        new AlertDialog.Builder(this)
                .setTitle(titulo)
                .setMessage(mensagem)
                .setPositiveButton("Tentar Novamente", (dialog, which) -> criarPedido())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!carrinhoHelper.isEmpty()) {
            carregarDadosCarrinho();
        }
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Cancelar Pedido?")
                .setMessage("Deseja cancelar a criação do pedido?")
                .setPositiveButton("Sim", (dialog, which) -> super.onBackPressed())
                .setNegativeButton("Não", null)
                .show();
    }
}